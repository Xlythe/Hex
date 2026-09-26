package com.xlythe.hex.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.JsonSyntaxException;
import com.hex.core.Game.GameListener;
import com.hex.core.GameAction;
import com.hex.core.Player;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Timer;
import com.xlythe.hex.FileUtil;
import com.xlythe.hex.MainActivity;
import com.xlythe.hex.AppExecutors;
import com.xlythe.hex.R;
import com.xlythe.hex.Settings;
import com.xlythe.hex.Stats;
import com.xlythe.hex.compat.Game;
import com.xlythe.hex.compat.AndroidBotFactory;
import com.xlythe.hex.server.ServerNetworkPlayer;
import com.xlythe.hex.compat.GameOptions;
import com.xlythe.hex.view.BoardView;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.xlythe.hex.Settings.TAG;

/**
 * @author Will Harmon
 **/
public class GameFragment extends HexFragment {
    public static final String GAME = "game";
    public static final String PLAYER1 = "player1";
    public static final String PLAYER2 = "player2";
    public static final String PLAYER1_TYPE = "player1_type";
    public static final String PLAYER2_TYPE = "player2_type";
    public static final String REPLAY = "replay";
    public static final String PRELOADED_GAME = "preloaded_game";
    private Game game;
    private Player player1Type;
    private Player player2Type;
    private boolean replay;
    private int replayDuration;
    private long timeGamePaused;
    private long whenGamePaused;

    private boolean goHome = false;

    /**
     * Set at the end of onWin, or when a game is loaded. Use this to avoid auto-saving replayed games or unlocking achievements that weren't earned.
     */
    private boolean gameHasEnded = false;

    private BoardView board;
    private Button exit;
    private Button newGame;
    private Button undo;
    private Button chat;
    private final Runnable chatObserver = this::updateChatButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        keepScreenOn(true);
        getParentFragmentManager().setFragmentResultListener(
                GameOverDialogFragment.REQUEST_KEY,
                this,
                (requestKey, result) -> {
                    String action = result.getString(GameOverDialogFragment.RESULT_ACTION);
                    if (GameOverDialogFragment.ACTION_PLAY_AGAIN.equals(action)) {
                        startNewGame();
                    } else if (GameOverDialogFragment.ACTION_HOME.equals(action)) {
                        setGoHome(true);
                    }
                });

        loadGame(savedInstanceState);
        return applyBoard(inflater, container);
    }

    @Override
    public void onPause() {
        super.onPause();
        whenGamePaused = System.currentTimeMillis();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopGame();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isNetGame()) {
            getMainActivity().addChatObserver(chatObserver);
            updateChatButton();
        }
    }

    @Override
    public void onStop() {
        MainActivity activity = getMainActivity();
        if (activity != null) activity.removeChatObserver(chatObserver);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (game != null && game.getPlayer1().supportsSave() && game.getPlayer2().supportsSave()) {
            savedInstanceState.putString(GAME, game.save());
            savedInstanceState.putSerializable(PLAYER1, game.getPlayer1().getSaveState());
            savedInstanceState.putSerializable(PLAYER2, game.getPlayer2().getSaveState());
            savedInstanceState.putSerializable(PLAYER1_TYPE, player1Type);
            savedInstanceState.putSerializable(PLAYER2_TYPE, player2Type);
        }
    }

    private void loadGame(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(GAME)) {
            String gameState = savedInstanceState.getString(GAME);

            // Resume a game if one exists
            boolean keys = savedInstanceState.containsKey(PLAYER1_TYPE);
            keys &= savedInstanceState.containsKey(PLAYER2_TYPE);
            keys &= savedInstanceState.containsKey(PLAYER1);
            keys &= savedInstanceState.containsKey(PLAYER2);
            if (keys) {
                // We have additional information about the player's state
                int gridSize = Settings.getGridSize(getMainActivity());
                player1Type = getSerializable(savedInstanceState, PLAYER1_TYPE, Player.class);
                player2Type = getSerializable(savedInstanceState, PLAYER2_TYPE, Player.class);
                game = Game.load(gameState, createPlayer(1, gridSize), createPlayer(2, gridSize));
                game.getPlayer1().setSaveState(
                        getSerializable(savedInstanceState, PLAYER1, Serializable.class));
                game.getPlayer2().setSaveState(
                        getSerializable(savedInstanceState, PLAYER2, Serializable.class));
            } else {
                // Load a game with 2 humans
                game = Game.load(gameState);
            }
            game.setGameListener(createGameListener());
            replay = true;
            replayDuration = 0;

            if (savedInstanceState.containsKey(REPLAY) && savedInstanceState.getBoolean(REPLAY)) {
                replayDuration = 900;
            }
        } else if (getArguments() != null && getArguments().containsKey(GAME)) {
            String gameState = getArguments().getString(GAME);

            // Load a game
            player1Type = Player.Human;
            player2Type = Player.Human;
            try {
                game = Game.load(gameState);
                game.setGameListener(createGameListener());
                replay = true;
                replayDuration = 0;
                gameHasEnded = true;

                if (getArguments().containsKey(REPLAY) && getArguments().getBoolean(REPLAY)) {
                    replayDuration = 900;
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                // Create a new game
                initializeNewGame();
            }
        } else if (getArguments() != null && getArguments().containsKey(PRELOADED_GAME) && getArguments().getBoolean(PRELOADED_GAME)) {
            // Net game (game should have already been passed in)
            if (game == null) {
                returnHome();
                initializeNewGame();
            } else {
                game.setGameListener(createGameListener());
            }
            replay = true;
            replayDuration = 0;
        } else {
            // Create a new game
            initializeNewGame();
        }
    }

    private void startGame() {
        if (game.hasTimer()) {
            game.startTimer();
        }
        game.start();
    }

    private View applyBoard(@NonNull LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        board = view.findViewById(R.id.board);
        board.setGame(game);
        board.setTitleText(getString(R.string.game_turn_title));
        board.setActionText(getString(R.string.game_turn_msg));
        if (game.hasTimer())
            board.setTimerText(getString(R.string.game_timer_msg));
        if (game.isGameOver() && game.getGameListener() != null)
            game.getGameListener().onWin(game.getCurrentPlayer());

        exit = view.findViewById(R.id.exit);
        exit.setOnClickListener(v -> quit());
        newGame = view.findViewById(R.id.reload);
        newGame.setOnClickListener(v -> newGame());
        undo = view.findViewById(R.id.undo);
        undo.setOnClickListener(v -> undo());
        chat = view.findViewById(R.id.chat);
        chat.setVisibility(isNetGame() ? View.VISIBLE : View.GONE);
        chat.setOnClickListener(v -> {
            getMainActivity().markChatRead();
            new GameChatDialogFragment().show(
                    getParentFragmentManager(), GameChatDialogFragment.TAG);
        });

        undo.setNextFocusRightId(R.id.board);
        board.setNextFocusLeftId(R.id.undo);

        newGame.setVisibility(supportsNewGame() ? View.VISIBLE : View.GONE);
        undo.setVisibility(supportsUndo() ? View.VISIBLE : View.GONE);

        return view;
    }

    @Nullable
    private static <T extends Serializable> T getSerializable(
            Bundle bundle,
            String key,
            Class<T> type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getSerializable(key, type);
        }
        return getLegacySerializable(bundle, key, type);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    private static <T extends Serializable> T getLegacySerializable(
            Bundle bundle,
            String key,
            Class<T> type) {
        Serializable value = bundle.getSerializable(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    private void updateChatButton() {
        if (chat == null || !isAdded()) return;
        int unread = getMainActivity().getGameChatStore().unreadCount();
        chat.setText(unread == 0
                ? getString(R.string.game_chat_button)
                : getString(R.string.game_chat_unread, unread));
    }

    private void recordCompletedGame(PlayingEntity winner) {
        Context applicationContext = requireContext().getApplicationContext();
        boolean autosave = Settings.getAutosave(applicationContext);
        long playedMillis = Math.max(0, game.getGameLength() - timeGamePaused);
        boolean humanWin = winner.getType() == Player.Human;
        String replayState = autosave ? game.save() : null;
        String replayName = autosave
                ? getString(
                        R.string.auto_saved_file_name,
                        new SimpleDateFormat("yyyy-MM-dd HH-mm", Locale.getDefault())
                                .format(new Date()),
                        safeFileComponent(game.getPlayer1().getName()),
                        safeFileComponent(game.getPlayer2().getName()))
                : null;

        AppExecutors.io().execute(() -> {
            if (replayState != null) {
                try {
                    FileUtil.autoSaveGame(applicationContext, replayName, replayState);
                } catch (IOException error) {
                    Log.w(TAG, "Could not autosave completed game", error);
                }
            }
            Stats.incrementTimePlayed(applicationContext, playedMillis);
            Stats.incrementGamesPlayed(applicationContext);
            if (humanWin) Stats.incrementGamesWon(applicationContext);
        });

        recordAchievements(winner);
    }

    private void recordAchievements(PlayingEntity winner) {
        if (!isSignedIn() || getAchievementsClient() == null) return;

        if (game.getGameLength() < 30 * 1000) {
            getAchievementsClient().unlock(getString(R.string.achievement_30_seconds));
        }
        if (game.getGameLength() < 10 * 1000) {
            getAchievementsClient().unlock(getString(R.string.achievement_10_seconds));
        }

        boolean boardFilled = true;
        for (int x = 0; x < game.getGridSize() && boardFilled; x++) {
            for (int y = 0; y < game.getGridSize(); y++) {
                if (game.gamePieces[x][y].getTeam() == 0) {
                    boardFilled = false;
                    break;
                }
            }
        }
        if (boardFilled) {
            getAchievementsClient().unlock(getString(R.string.achievement_fill_the_board));
        }
        if (isVsAi() && winner.getType() == Player.Human) {
            getAchievementsClient().unlock(getString(R.string.achievement_monitor_smasher));
        }
        if (game.hasTimer()) {
            getAchievementsClient().unlock(getString(R.string.achievement_speed_demon));
        }
        getAchievementsClient().increment(getString(R.string.achievement_novice), 1);
        getAchievementsClient().increment(getString(R.string.achievement_intermediate), 1);
        if (winner.getType() == Player.Human) {
            getAchievementsClient().increment(getString(R.string.achievement_expert), 1);
            getAchievementsClient().increment(getString(R.string.achievement_insane), 1);
        }
        if (isNetGame()) {
            getAchievementsClient().unlock(getString(R.string.achievement_net));
        }
    }

    private static String safeFileComponent(String value) {
        if (value == null) return "";
        return value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    protected void initializeNewGame() {
        // Stop the old game
        stopGame();
        timeGamePaused = 0;
        gameHasEnded = false;

        // Create a new game object
        GameOptions gameOptions = new GameOptions.Builder()
                .setGridSize(Settings.getGridSize(getMainActivity()))
                .setSwapEnabled(Settings.getSwap(getMainActivity()))
                .setTimer(new Timer(Settings.getTimeAmount(getMainActivity()), 0, Settings.getTimerType(getMainActivity())))
                .build();

        GameListener gameListener = createGameListener();

        game = new Game(gameOptions, createPlayer(1, gameOptions.gridSize), createPlayer(2, gameOptions.gridSize));
        game.setGameListener(gameListener);

        setName(game.getPlayer1());
        setName(game.getPlayer2());
        setColor(game.getPlayer1());
        setColor(game.getPlayer2());
    }

    @NonNull
    private GameListener createGameListener() {
        return new GameListener() {
            @Override
            public void onWin(@NonNull final PlayingEntity player) {
                runOnUiThread(() -> {
                    board.invalidate();

                    Log.v(TAG, player.getName() + " won!");

                    if (isAdded() && getParentFragmentManager()
                            .findFragmentByTag("game-over") == null) {
                        GameOverDialogFragment.create(game, player)
                                .show(getParentFragmentManager(), "game-over");
                    }

                    if (gameHasEnded) return;
                    else gameHasEnded = true;

                    recordCompletedGame(player);
                });
            }

            @Override
            public void onClear() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onStart() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onStop() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onTurn(PlayingEntity player) {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onReplayStart() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onReplayEnd() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onUndo() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void startTimer() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void displayTime(int minutes, int seconds) {
                runOnUiThread(() -> board.postInvalidate());
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (whenGamePaused != 0) {
            timeGamePaused += System.currentTimeMillis() - whenGamePaused;
            whenGamePaused = 0;
        }

        if (goHome) {
            returnHome();
            return;
        }

        // Note: Calling replay will start the game for us.
        if (replay) {
            replay = false;
            replay(replayDuration);
            return;
        }

        if (!game.hasStarted() && !game.isGameOver()) {
            startGame();
            return;
        }
    }

    /**
     * Terminates the game
     */
    private void stopGame() {
        if (game != null) {
            game.stop();
        }
    }

    /**
     * Refreshes both player's names Does not invalidate the board
     */
    protected void setName(@NonNull PlayingEntity player) {
        if (isPassToPlay()) {
            if (player.getTeam() == 1) {
                player.setName(Settings.getPlayer1Name(getMainActivity(), getPlayGamesPlayerName()));
            } else {
                player.setName(Settings.getPlayer2Name(getMainActivity()));
            }
        } else if (player.getType() == Player.Human) {
            player.setName(Settings.getPlayer1Name(getMainActivity(), getPlayGamesPlayerName()));
        }
    }

    /**
     * Refreshes both player's colors Does not invalidate the board
     */
    protected void setColor(@NonNull PlayingEntity player) {
        if (player.getTeam() == 1) {
            player.setColor(Settings.getPlayer1Color(getMainActivity()));
        } else {
            player.setColor(Settings.getPlayer2Color(getMainActivity()));
        }
    }

    private PlayingEntity createPlayer(int team, int gridSize) {
        Player p = (team == 1) ? player1Type : player2Type;
        switch (p) {
            case AI:
                int difficulty = Settings.getComputerDifficulty(getMainActivity());
                return AndroidBotFactory.create(difficulty, team, gridSize);
            case Human:
                return new PlayerObject(team);
            default:
                return new PlayerObject(team);
        }
    }

    protected void undo() {
        if (isNetGame() && getNetPlayer() instanceof ServerNetworkPlayer) {
            ((ServerNetworkPlayer) getNetPlayer()).requestUndo(game.getMoveNumber() - 1);
            return;
        }
        GameAction.undo(GameAction.LOCAL_GAME, game);
    }

    public void applyServerUndo() {
        if (game != null && game.getMoveList().size() > 0) {
            GameAction.undo(GameAction.LOCAL_GAME, game);
        }
    }

    protected void newGame() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    startNewGame();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // Do nothing
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        builder.setMessage(getString(R.string.confirmNewgame)).setPositiveButton(getString(R.string.yes), dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    public void startNewGame() {
        stopGame();

        // Net games are handled differently, because we need to inform the remote device.
        if (isNetGame()) {
            getNetPlayer().newgameCalled();
            return;
        }

        // Local games can just recreate both players (to get them into a clean state) before
        // starting a new game.
        PlayingEntity p1 = createPlayer(1, game.getGridSize());
        p1.setName(game.getPlayer1().getName());
        p1.setColor(game.getPlayer1().getColor());
        PlayingEntity p2 = createPlayer(2, game.getGridSize());
        p2.setName(game.getPlayer2().getName());
        p2.setColor(game.getPlayer2().getColor());

        switchToGame(new Game(game.gameOptions, p1, p2));
    }

    private void replay(int time) {
        game.replay(time);
    }

    private void quit() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    if (isNetGame() && getNetPlayer() instanceof ServerNetworkPlayer) {
                        ((ServerNetworkPlayer) getNetPlayer()).forfeit();
                        com.xlythe.hex.server.OnlineGameService.stop(getMainActivity());
                        com.xlythe.hex.server.OnlineNotificationState.clear(getMainActivity());
                    }
                    stopGame();
                    returnHome();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    // Do nothing
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        builder.setMessage(getString(R.string.confirmExit)).setPositiveButton(getString(R.string.yes), dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    public void setPlayer1Type(Player player1Type) {
        this.player1Type = player1Type;
    }

    public void setPlayer2Type(Player player2Type) {
        this.player2Type = player2Type;
    }

    public void setGoHome(boolean goHome) {
        this.goHome = goHome;
    }

    @Nullable
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;

        if (newGame != null) {
            newGame.setVisibility(supportsNewGame() ? View.VISIBLE : View.GONE);
        }
        if (undo != null) {
            undo.setVisibility(supportsUndo() ? View.VISIBLE : View.GONE);
        }
    }

    private boolean supportsNewGame() {
        if (game == null) {
            return true;
        }
        return game.getPlayer1().supportsNewgame() && game.getPlayer2().supportsNewgame();
    }

    private boolean supportsUndo() {
        if (game == null) {
            return true;
        }
        return game.getPlayer1().supportsUndo(game) && game.getPlayer2().supportsUndo(game);
    }

    private boolean isVsAi() {
        return player1Type.equals(Player.AI) || player2Type.equals(Player.AI);
    }

    private boolean isPassToPlay() {
        return player1Type.equals(Player.Human) && player2Type.equals(Player.Human);
    }

    private boolean isNetGame() {
        return player1Type.equals(Player.Net) || player2Type.equals(Player.Net);
    }

    private PlayingEntity getNetPlayer() {
        if (player1Type.equals(Player.Net)) {
            return game.getPlayer1();
        }

        if (player2Type.equals(Player.Net)) {
            return game.getPlayer2();
        }

        throw new IllegalStateException("Cannot get a net player in a non-net game");
    }
}
