package com.xlythe.hex.legacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.hex.core.Game.GameListener;
import com.hex.core.GameAction;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Timer;
import com.xlythe.hex.FileUtil;
import android.graphics.drawable.Drawable;
import com.xlythe.hex.ReplayDocument;
import com.xlythe.hex.compat.AndroidBotFactory;
import com.xlythe.hex.compat.Game;
import com.xlythe.hex.view.BoardView;
import com.xlythe.hex.view.HexagonLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Native Hex for devices older than the modern Android dependencies. */
public final class LegacyActivity extends Activity {
    private final Handler ui = new Handler();
    private final ExecutorService files = Executors.newSingleThreadExecutor();
    private Game game;
    private BoardView board;
    private int difficulty = -1;
    private int size;
    private String replayFile;
    private boolean destroyed;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        size = getPreferences(0).getInt("size", 11);
        if (state != null && state.containsKey("match")) {
            difficulty = state.getInt("difficulty");
            play(Game.load(state.getString("match"), new PlayerObject(1), opponent()));
        } else {
            menu();
        }
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (game != null && replayFile == null && !game.isGameOver()) {
            state.putString("match", game.save());
            state.putInt("difficulty", difficulty);
        }
    }

    static Game newGame(int size, PlayingEntity first, PlayingEntity second) {
        Game.GameOptions options = new Game.GameOptions();
        options.gridSize = size;
        options.swap = true;
        options.timer = new Timer(0, 0, Timer.NO_TIMER);
        first.setColor(0xffc84442);
        second.setColor(0xff376dc2);
        return new Game(options, first, second);
    }

    private PlayingEntity opponent() {
        switch (difficulty) {
            case 0: return AndroidBotFactory.create(AndroidBotFactory.EASY, 2, size);
            case 1: return AndroidBotFactory.create(AndroidBotFactory.MEDIUM, 2, size);
            case 2: return AndroidBotFactory.create(AndroidBotFactory.HARD, 2, size);
            default: return new PlayerObject(2);
        }
    }

    private void stopGame() {
        Game previous = game;
        game = null;
        replayFile = null;
        if (previous != null && previous.hasStarted()) previous.stop();
    }

    private void menu() {
        stopGame();
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(0xffd2d2d2);
        HexagonLayout wheel = new HexagonLayout(this);
        wheel.setText("Hex");
        String[] labels = {"Board size", "Online", "History", "How to play", "Computer", "Play"};
        String[] icons = {"settings", "store", "history", "howtoplay", "achievements", "play"};
        int[] colors = {0xffcc5c57, 0xff5f6ec2, 0xfff9db00, 0xffb7cf47, 0xfff48935, 0xff4ba5e2};
        Runnable[] actions = {this::chooseSize, () -> startActivity(new Intent(this, LegacyOnlineActivity.class)),
                this::history, this::instructions, this::chooseBot, () -> { difficulty = -1; startMatch(); }};
        for (int i = 0; i < labels.length; i++) {
            HexagonLayout.Button button = wheel.getButtons()[i];
            button.setText(labels[i]);
            button.setColor(colors[i]);
            try (InputStream icon = getAssets().open(icons[i] + ".png")) {
                button.setDrawable(Drawable.createFromStream(icon, icons[i]));
            } catch (IOException missing) {
                throw new IllegalStateException("Missing wheel icon", missing);
            }
            button.setOnClickListener(actions[i]::run);
        }
        frame.addView(wheel, new FrameLayout.LayoutParams(-2, -1, Gravity.CENTER));
        setContentView(frame);
    }

    private void instructions() {
        new AlertDialog.Builder(this).setTitle("How to play")
                .setMessage("Connect your two colored sides with a chain of stones. Stones in adjacent hexagons touch. On the second turn you can tap the opening stone to swap.")
                .setPositiveButton(android.R.string.ok, null).show();
    }

    private void chooseSize() {
        int[] sizes = {7, 9, 11, 13, 15, 19};
        new AlertDialog.Builder(this).setTitle("Board size").setSingleChoiceItems(
                new String[]{"7 × 7", "9 × 9", "11 × 11", "13 × 13", "15 × 15", "19 × 19"},
                Arrays.binarySearch(sizes, size), (dialog, which) -> {
                    size = sizes[which];
                    getPreferences(0).edit().putInt("size", size).commit();
                    dialog.dismiss();
                }).show();
    }

    private void chooseBot() {
        new AlertDialog.Builder(this).setTitle("Play the computer")
                .setItems(new String[]{"Easy", "Medium", "Hard"}, (dialog, which) -> {
                    difficulty = which;
                    startMatch();
                }).show();
    }

    private void startMatch() {
        stopGame();
        Game next = newGame(size, new PlayerObject(1), opponent());
        next.getPlayer1().setName("Red");
        next.getPlayer2().setName(difficulty < 0 ? "Blue" : "Computer");
        play(next);
    }

    private void play(Game next) {
        game = next;
        next.getMoveList().replay(0, next);
        showBoard();
        next.setGameListener(new GameListener() {
            public void onTurn(PlayingEntity player) { refresh(next); }
            public void onUndo() { refresh(next); }
            public void onWin(PlayingEntity winner) {
                save(next.saveCompleted(winner), "Hex-" + next.getGameStart());
                refresh(next);
            }
            public void onStart() {}
            public void onStop() {}
            public void onClear() {}
            public void onReplayStart() {}
            public void onReplayEnd() {}
            public void displayTime(int minutes, int seconds) {}
            public void startTimer() {}
        });
        next.start();
    }

    private void refresh(Game source) {
        ui.post(() -> {
            if (source != game) return;
            board.setActionText(source.isGameOver() ? "wins" : "to move");
            board.invalidate();
        });
    }

    private void showBoard() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xffd2d2d2);
        board = new BoardView(this);
        board.setGame(game);
        board.setTitleText("%s");
        layout.addView(board, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        addButton(controls, "Exit", this::menu);
        if (replayFile == null) {
            addButton(controls, "Restart", this::startMatch);
            addButton(controls, "Undo", () -> GameAction.undo(GameAction.LOCAL_GAME, game));
        } else {
            board.setEnabled(false);
            addButton(controls, "Replay", () -> openReplay(replayFile));
            addButton(controls, "Share", this::share);
        }
        layout.addView(controls);
        setContentView(layout);
    }

    static void addButton(LinearLayout row, String label, Runnable action) {
        Button button = new Button(row.getContext());
        button.setText(label);
        button.setOnClickListener(v -> action.run());
        row.addView(button);
    }

    private void history() {
        files.execute(() -> {
            String[] names = FileUtil.historyDirectory(this).list((dir, name) -> name.endsWith(".rhex"));
            String[] history = names == null ? new String[0] : names;
            Arrays.sort(history, Collections.reverseOrder());
            ui.post(() -> {
                if (destroyed) return;
                AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle("History");
                if (history.length == 0) dialog.setMessage("Completed games appear here. You can also import a .rhex replay.");
                else dialog.setItems(history, (d, index) -> openReplay(history[index]));
                dialog.setPositiveButton("Import replay", (d, index) -> pickReplay())
                        .setNegativeButton(android.R.string.cancel, null).show();
            });
        });
    }

    private void pickReplay() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*");
        try {
            startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException e) {
            toast("Install a file picker to open .rhex files.");
        }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != 1 || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        files.execute(() -> {
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                String text = ReplayDocument.read(input);
                String name = "Imported-" + System.currentTimeMillis() + ".rhex";
                FileUtil.autoSaveGame(this, name, text);
                ui.post(() -> { if (!destroyed) openReplay(name); });
            } catch (IOException | SecurityException invalid) {
                toast("Could not open replay: " + invalid.getMessage());
            }
        });
    }

    private void save(String replay, String name) {
        files.execute(() -> {
            try {
                FileUtil.autoSaveGame(this, name, replay);
            } catch (IOException error) {
                toast("Could not save replay: " + error.getMessage());
            }
        });
    }

    private void openReplay(String name) {
        stopGame();
        files.execute(() -> {
            try {
                Game loaded = Game.load(FileUtil.loadGameAsString(this, name));
                ui.post(() -> { if (!destroyed) showReplay(name, loaded); });
            } catch (IOException | RuntimeException invalid) {
                toast("Could not open replay: " + invalid.getMessage());
            }
        });
    }

    private void showReplay(String name, Game loaded) {
        replayFile = name;
        game = loaded;
        loaded.replayRunning = true;
        showBoard();
        // Play on the UI queue so navigation can cancel a replay between any two moves.
        JsonArray moves = new JsonParser().parse(loaded.save()).getAsJsonObject()
                .getAsJsonObject("moveList").getAsJsonArray("moveList");
        ui.post(new Runnable() {
            int index;
            int previousX, previousY;
            public void run() {
                if (game != loaded) return;
                if (index == moves.size()) {
                    if (!GameAction.checkWinPlayer(1, loaded))
                        GameAction.checkWinPlayer(2, loaded);
                } else {
                    JsonObject move = moves.get(index++).getAsJsonObject();
                    if (move.has("swap") && move.get("swap").getAsBoolean())
                        loaded.gamePieces[previousX][previousY].setTeam((byte) 0, loaded);
                    previousX = move.get("x").getAsInt();
                    previousY = move.get("y").getAsInt();
                    loaded.gamePieces[previousX][previousY].setTeam(move.get("team").getAsByte(), loaded);
                    ui.postDelayed(this, 350);
                }
                board.invalidate();
            }
        });
    }

    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND).setType("application/octet-stream")
                .putExtra(Intent.EXTRA_STREAM, ReplayProvider.uri(replayFile))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share replay"));
    }

    private void toast(String message) {
        ui.post(() -> { if (!destroyed) Toast.makeText(this, message, Toast.LENGTH_LONG).show(); });
    }

    @Override public void onBackPressed() {
        if (game == null) super.onBackPressed();
        else menu();
    }

    @Override protected void onDestroy() {
        destroyed = true;
        stopGame();
        ui.removeCallbacksAndMessages(null);
        files.shutdown();
        super.onDestroy();
    }
}
