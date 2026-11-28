package com.xlythe.hex.legacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hex.core.GameAction;
import com.hex.core.PlayerObject;
import com.hex.core.Point;
import com.xlythe.hex.compat.Game;
import com.xlythe.hex.server.IgGameCenterClient;
import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.Event;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.Member;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;
import com.xlythe.hex.server.IgGameCenterProtocol;
import com.xlythe.hex.view.BoardView;

import javax.net.ssl.SSLException;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Foreground matchmaking and chat using the same authenticated server as the modern clients. */
public final class LegacyOnlineActivity extends Activity {
    private final Handler ui = new Handler();
    private final ExecutorService network = Executors.newSingleThreadExecutor();
    private final StringBuilder messages = new StringBuilder();
    private IgGameCenterClient client;
    private UserSession user;
    private BoardRef room;
    private HandlerResponse position;
    private long eventId;
    private boolean foreground, busy, destroyed;
    private LinearLayout layout;
    private TextView status;
    private BoardView board;
    private Game game;
    private final Runnable poll = () -> request(() -> update(client.refresh(room, user, eventId)));

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        String id = getPreferences(0).getString("device", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            getPreferences(0).edit().putString("device", id).commit();
        }
        client = LegacyServer.connect(id);
        screen();
        status.setText("Xlythe account");
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setHint("Username");
        layout.addView(name);
        EditText password = new EditText(this);
        password.setSingleLine(true);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint("Password");
        layout.addView(password);
        LinearLayout actions = new LinearLayout(this);
        layout.addView(actions);
        LegacyActivity.addButton(actions, "Sign in", () -> login(name, password, false));
        LegacyActivity.addButton(actions, "Create account", () -> login(name, password, true));
    }

    private void login(EditText name, EditText password, boolean register) {
        String username = name.getText().toString().trim();
        String secret = password.getText().toString();
        if (username.length() == 0 || secret.length() == 0) {
            status.setText("Enter a username and password.");
            return;
        }
        request(() -> {
            if (register) client.register(username, secret, null);
            UserSession account = client.login(username, secret);
            return () -> {
                user = account;
                password.setText("");
                screen();
                status.setText("Signed in as " + user.name);
                LegacyActivity.addButton(layout, "Find a game", () -> enter(false));
                LegacyActivity.addButton(layout, "Create a game", () -> enter(true));
            };
        });
    }

    private void enter(boolean create) {
        request(() -> {
            BoardRef next = create ? client.createBoard(user, "1") : client.joinRandomBoard(user, null);
            HandlerResponse initial = client.handle(next, user, 0, null, null);
            HandlerResponse start = "INIT".equals(initial.status)
                    ? client.command(next, user, 0, "START") : initial;
            return () -> {
                room = next;
                screen();
                board = new BoardView(this);
                layout.addView(board, new LinearLayout.LayoutParams(-1, 0, 1));
                LinearLayout controls = new LinearLayout(this);
                layout.addView(controls);
                LegacyActivity.addButton(controls, "Exit", this::onBackPressed);
                LegacyActivity.addButton(controls, "Swap", () -> sendMove("SWAP"));
                LegacyActivity.addButton(controls, "Chat", this::chat);
                render(start);
            };
        });
    }

    private void screen() {
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        status = new TextView(this);
        status.setTextSize(18);
        layout.addView(status);
        setContentView(layout);
    }

    private Runnable update(HandlerResponse response) { return () -> render(response); }

    private void render(HandlerResponse response) {
        String cells = response.board;
        int size = response.options == null ? 11 : response.options.boardSize;
        if (size < 3 || size > 19 || cells == null || cells.length() != size * size || !cells.matches("[012]+")) {
            status.setText("The server returned an invalid board.");
            return;
        }
        position = response;
        if (game == null || game.getGridSize() != size) {
            game = LegacyActivity.newGame(size, new PlayerObject(1), new PlayerObject(2));
            game.replayRunning = true;
            board.setGame(game);
            board.setOnCellClickListener((x, y) -> sendMove(IgGameCenterProtocol.encodeMove(new Point(x, y), size)));
        }
        for (int i = 0; i < cells.length(); i++)
            game.gamePieces[i / size][i % size].setTeam((byte) (cells.charAt(i) - '0'), game);
        for (Member member : response.players) {
            if ("1".equals(member.place)) game.getPlayer1().setName(member.name);
            if ("2".equals(member.place)) game.getPlayer2().setName(member.name);
        }
        for (Event event : response.events) {
            if (event.eid <= eventId || !"MSG".equals(event.type)) continue;
            String sender = event.uid;
            for (Member member : response.players) if (member.uid.equals(event.uid)) sender = member.name;
            messages.append(sender).append(": ").append(event.data).append('\n');
            while (messages.length() > 16384) messages.delete(0, messages.indexOf("\n") + 1);
        }
        eventId = response.latestEventId(eventId);
        if ("FINISHED".equals(response.status)) {
            if (!GameAction.checkWinPlayer(1, game)) GameAction.checkWinPlayer(2, game);
            status.setText("Game finished");
        } else if ("ACTIVE".equals(response.status)) {
            status.setText(response.localActive ? "Your turn" : "Opponent’s turn");
        } else {
            status.setText("Waiting for an opponent");
        }
        board.invalidate();
    }

    private void sendMove(String move) {
        if (position != null && position.localActive) {
            request(() -> update(client.command(room, user, eventId, "MOVE", "move", move)));
        }
    }

    private void chat() {
        EditText field = new EditText(this);
        field.setHint("Message");
        new AlertDialog.Builder(this).setTitle("Game chat")
                .setMessage(messages.length() == 0 ? "No messages yet." : messages.toString())
                .setView(field).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Send", (dialog, which) -> {
                    String text = field.getText().toString().trim();
                    if (text.length() > 0) request(() -> update(client.command(room, user, eventId, "MSG", "message", text)));
                }).show();
    }

    private interface Request { Runnable run() throws Exception; }

    private void request(Request action) {
        if (destroyed) return;
        if (busy) {
            Toast.makeText(this, "Finishing the previous request. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        busy = true;
        ui.removeCallbacks(poll);
        network.execute(() -> {
            Runnable result;
            try {
                result = action.run();
            } catch (Exception error) {
                String message = (error.getCause() instanceof SSLException || (error.getCause() != null
                        && String.valueOf(error.getCause().getMessage()).contains("SSL handshake")))
                        ? "This device could not establish a trusted HTTPS connection to the game server."
                        : error.getMessage();
                result = () -> status.setText(message);
            }
            Runnable completed = result;
            ui.post(() -> {
                if (destroyed) return;
                busy = false;
                completed.run();
                schedulePoll();
            });
        });
    }

    private void schedulePoll() {
        ui.removeCallbacks(poll);
        if (foreground && room != null && (position == null || !"FINISHED".equals(position.status)))
            ui.postDelayed(poll, 3500);
    }

    @Override protected void onResume() { super.onResume(); foreground = true; schedulePoll(); }
    @Override protected void onPause() { foreground = false; ui.removeCallbacks(poll); super.onPause(); }

    @Override public void onBackPressed() {
        if (room == null || (position != null && "FINISHED".equals(position.status))) {
            super.onBackPressed();
            return;
        }
        new AlertDialog.Builder(this).setMessage("Leave this game?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Leave", (dialog, which) -> {
                    network.execute(() -> {
                        try { client.command(room, user, eventId, "LEAVE"); }
                        catch (Exception ignored) { /* The room also expires when disconnected. */ }
                    });
                    finish();
                }).show();
    }

    @Override protected void onDestroy() {
        destroyed = true;
        ui.removeCallbacksAndMessages(null);
        network.shutdown();
        super.onDestroy();
    }
}
