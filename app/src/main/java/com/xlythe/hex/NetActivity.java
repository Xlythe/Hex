package com.xlythe.hex;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Timer;
import com.xlythe.hex.compat.Game;
import com.xlythe.hex.compat.GameOptions;
import com.xlythe.hex.server.IgGameCenterClient;
import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.LobbyBoard;
import com.xlythe.hex.server.IgGameCenterModels.Member;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;
import com.xlythe.hex.server.IgGameCenterProtocol;
import com.xlythe.hex.server.ServerAuthDialog;
import com.xlythe.hex.server.ServerCredentialStore;
import com.xlythe.hex.server.ServerCredentials;
import com.xlythe.hex.server.ServerNetworkPlayer;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Activity-level account, lobby, matchmaking, and rematch orchestration. */
public abstract class NetActivity extends BaseGameActivity {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ScheduledExecutorService serverExecutor =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "Hex-server-session");
                thread.setDaemon(true);
                return thread;
            });

    private IgGameCenterClient serverClient;
    private ServerCredentialStore credentialStore;
    private ScheduledFuture<?> roomPoll;
    private Dialog waitingDialog;
    private BoardRef waitingBoard;
    private UserSession waitingUser;
    private long waitingEventId;
    private boolean destroyed;

    public abstract void switchToGame(Game game);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        credentialStore = new ServerCredentialStore(this);
        serverClient = IgGameCenterClient.production(getStableNetworkUid());
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        cancelWaiting(false);
        serverExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void startQuickGame() {
        authenticate(user -> runBusy("Finding a game…", () -> {
            BoardRef board = serverClient.joinRandomBoard(user, null);
            HandlerResponse response = serverClient.handle(board, user, 0, null, null);
            prepareRoom(board, user, response, false);
        }));
    }

    /** The former invite action now creates a discoverable server room. */
    @Override
    public void inviteFriends() {
        authenticate(user -> runBusy("Creating game…", () -> {
            BoardRef board = serverClient.createBoard(user, "1");
            HandlerResponse response = serverClient.handle(board, user, 0, null, null);

            LinkedHashMap<String, String> setup = new LinkedHashMap<>();
            setup.put("boardSize", Integer.toString(Settings.getGridSize(this)));
            setup.put("timerTotal", Integer.toString(Settings.getTimeAmount(this) * 60));
            setup.put("timerInc", "0");
            setup.put("scored", "0");
            response = serverClient.handle(
                    board,
                    user,
                    response.latestEventId(0),
                    "SETUP",
                    setup);
            prepareRoom(board, user, response, true);
        }));
    }

    /** The former inbox action now browses open igGameCenter Hex rooms. */
    @Override
    public void checkInvites() {
        authenticate(user -> runBusy("Loading games…", () -> {
            List<LobbyBoard> available = new ArrayList<>();
            for (LobbyBoard board : serverClient.fetchLobby(user)) {
                if (!board.hidden
                        && board.firstAvailablePlace() != null
                        && !"FINISHED".equalsIgnoreCase(board.status)) {
                    available.add(board);
                }
            }
            mainHandler.post(() -> showLobby(user, available));
        }));
    }

    @Override
    public void openAchievements() {
        if (!isSignedIn()) {
            signIn();
            return;
        }
        if (getAchievementsClient() != null) {
            getAchievementsClient().getAchievementsIntent().addOnSuccessListener(intent -> {
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    public void signOutServerAccount() {
        credentialStore.clear();
        toast("Signed out of igGameCenter");
    }

    private void showLobby(UserSession user, List<LobbyBoard> boards) {
        if (destroyed) return;
        if (boards.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Open Games")
                    .setMessage("No open Hex games were found.")
                    .setPositiveButton("Create Game", (dialog, which) -> inviteFriends())
                    .setNeutralButton("Sign Out", (dialog, which) -> signOutServerAccount())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return;
        }

        String[] labels = new String[boards.size()];
        for (int i = 0; i < boards.size(); i++) {
            LobbyBoard board = boards.get(i);
            String owner = board.members.isEmpty() ? "Open game" : board.members.get(0).name;
            labels[i] = owner + " · game " + board.sid;
        }
        new AlertDialog.Builder(this)
                .setTitle("Open Games")
                .setItems(labels, (dialog, index) -> joinLobbyBoard(user, boards.get(index)))
                .setNeutralButton("Sign Out", (dialog, which) -> signOutServerAccount())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void joinLobbyBoard(UserSession user, LobbyBoard lobbyBoard) {
        runBusy("Joining game…", () -> {
            BoardRef board = new BoardRef(lobbyBoard.sid, lobbyBoard.server);
            HandlerResponse response = serverClient.handle(board, user, 0, null, null);
            prepareRoom(board, user, response, false);
        });
    }

    private void prepareRoom(
            BoardRef board,
            UserSession user,
            HandlerResponse initial,
            boolean created) throws Exception {
        HandlerResponse response = initial;
        long eventId = response.latestEventId(0);
        if ("0".equals(response.localPlace) || response.localPlace.isEmpty()) {
            String place = firstAvailablePlace(response.players);
            if (place == null) throw new IllegalStateException("This game is already full.");
            response = serverClient.command(board, user, eventId, "PLACE", "place", place);
            eventId = response.latestEventId(eventId);
        }
        response = serverClient.command(board, user, eventId, "START");
        eventId = response.latestEventId(eventId);

        waitingBoard = board;
        waitingUser = user;
        waitingEventId = eventId;
        HandlerResponse current = response;
        mainHandler.post(() -> {
            showWaiting(created ? "Game " + board.sid + " is ready to join." : "Waiting for opponent…");
            if ("ACTIVE".equalsIgnoreCase(current.status)) launchGame(board, user, current);
            else scheduleRoomPoll();
        });
    }

    private void showWaiting(String message) {
        if (destroyed) return;
        if (waitingDialog != null) waitingDialog.dismiss();
        waitingDialog = new AlertDialog.Builder(this)
                .setTitle("Online Hex")
                .setMessage(message + "\n\nChecks occur at most once every 15 seconds.")
                .setNegativeButton("Cancel", (dialog, which) -> cancelWaiting(true))
                .setCancelable(false)
                .create();
        waitingDialog.show();
    }

    private void scheduleRoomPoll() {
        if (destroyed || waitingBoard == null || waitingUser == null) return;
        if (roomPoll != null) roomPoll.cancel(false);
        roomPoll = serverExecutor.schedule(() -> {
            try {
                HandlerResponse response =
                        serverClient.refresh(waitingBoard, waitingUser, waitingEventId);
                waitingEventId = response.latestEventId(waitingEventId);
                if ("ACTIVE".equalsIgnoreCase(response.status)) {
                    mainHandler.post(() -> launchGame(waitingBoard, waitingUser, response));
                } else {
                    scheduleRoomPoll();
                }
            } catch (Exception e) {
                mainHandler.post(() -> toast(safeMessage(e)));
                scheduleRoomPoll();
            }
        }, IgGameCenterProtocol.POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void launchGame(BoardRef board, UserSession user, HandlerResponse response) {
        if (destroyed) return;
        cancelWaiting(false);
        Member local = member(response.players, user.uid);
        Member remote = opponent(response.players, user.uid);
        if (local == null || remote == null) {
            toast("The server did not return both players.");
            return;
        }

        int localTeam = parseTeam(local.place);
        int remoteTeam = parseTeam(remote.place);
        int boardSize = response.options == null
                ? Settings.getGridSize(this)
                : response.options.boardSize;
        PlayingEntity localPlayer = new PlayerObject(localTeam);
        ServerNetworkPlayer remotePlayer = new ServerNetworkPlayer(
                remoteTeam,
                serverClient,
                user,
                board,
                remote.uid,
                boardSize,
                response.latestEventId(waitingEventId),
                serverPlayerListener(user),
                serverExecutor);

        localPlayer.setName(local.name);
        remotePlayer.setName(remote.name);
        localPlayer.setColor(localTeam == 1
                ? Settings.getPlayer1Color(this)
                : Settings.getPlayer2Color(this));
        remotePlayer.setColor(remoteTeam == 1
                ? Settings.getPlayer1Color(this)
                : Settings.getPlayer2Color(this));

        GameOptions.Builder options = new GameOptions.Builder()
                .setGridSize(boardSize)
                .setSwapEnabled(Settings.getSwap(this));
        if (response.options != null && response.options.timerTotalSeconds > 0) {
            long minutes = Math.max(1, (response.options.timerTotalSeconds + 59) / 60);
            options.setTimer(new Timer(
                    minutes,
                    response.options.timerIncrementSeconds,
                    response.options.timerIncrementSeconds > 0
                            ? Timer.PER_MOVE
                            : Timer.ENTIRE_MATCH));
        } else {
            options.setNoTimer();
        }

        Game game = localTeam == 1
                ? new Game(options.build(), localPlayer, remotePlayer)
                : new Game(options.build(), remotePlayer, localPlayer);
        switchToGame(game);
    }

    private ServerNetworkPlayer.Listener serverPlayerListener(UserSession user) {
        return new ServerNetworkPlayer.Listener() {
            @Override
            public void onNetworkError(String message) {
                mainHandler.post(() -> toast(message));
            }

            @Override
            public void onRestartCreated(BoardRef board) {
                joinRematch(user, board);
            }

            @Override
            public void onRestartOffered(BoardRef board) {
                mainHandler.post(() -> new AlertDialog.Builder(NetActivity.this)
                        .setTitle("Rematch")
                        .setMessage("Your opponent requested another game.")
                        .setPositiveButton("Join", (dialog, which) -> joinRematch(user, board))
                        .setNegativeButton("Decline", null)
                        .show());
            }

            @Override
            public void onUndoUnavailable() {
                mainHandler.post(() -> toast(
                        "This Android game engine cannot safely apply asynchronous undo."));
            }
        };
    }

    private void joinRematch(UserSession user, BoardRef board) {
        runBusy("Joining rematch…", () -> {
            HandlerResponse response = serverClient.handle(board, user, 0, null, null);
            prepareRoom(board, user, response, false);
        });
    }

    private void cancelWaiting(boolean leaveBoard) {
        ScheduledFuture<?> poll = roomPoll;
        roomPoll = null;
        if (poll != null) poll.cancel(true);
        if (waitingDialog != null) {
            waitingDialog.dismiss();
            waitingDialog = null;
        }
        BoardRef board = waitingBoard;
        UserSession user = waitingUser;
        long eventId = waitingEventId;
        waitingBoard = null;
        waitingUser = null;
        if (leaveBoard && board != null && user != null && !serverExecutor.isShutdown()) {
            serverExecutor.execute(() -> {
                try {
                    serverClient.command(board, user, eventId, "LEAVE");
                } catch (Exception ignored) {
                    // The local room is already closed; there is nothing useful to retry.
                }
            });
        }
    }

    private void authenticate(SessionAction action) {
        ServerCredentials saved = credentialStore.load();
        if (saved == null) {
            showAuth(action);
            return;
        }
        toast("Signing in…");
        serverExecutor.execute(() -> {
            try {
                UserSession session =
                        serverClient.loginWithDigest(saved.username, saved.passwordDigest);
                remember(new ServerCredentials(saved.username, saved.passwordDigest, session));
                action.run(session);
            } catch (Exception e) {
                mainHandler.post(() -> {
                    toast(safeMessage(e));
                    showAuth(action);
                });
            }
        });
    }

    private void showAuth(SessionAction action) {
        ServerAuthDialog.showSignIn(this, new ServerAuthDialog.Listener() {
            @Override
            public void onSignIn(String username, String plaintextPassword) {
                String digest = IgGameCenterProtocol.md5(plaintextPassword);
                runAuthAction("Signing in…", action, () -> {
                    UserSession session = serverClient.loginWithDigest(username, digest);
                    remember(new ServerCredentials(username, digest, session));
                    action.run(session);
                });
            }

            @Override
            public void onSignUp(String username, String plaintextPassword, String email) {
                String digest = IgGameCenterProtocol.md5(plaintextPassword);
                runAuthAction("Creating account…", action, () -> {
                    serverClient.register(username, plaintextPassword, email);
                    UserSession session = serverClient.loginWithDigest(username, digest);
                    remember(new ServerCredentials(username, digest, session));
                    action.run(session);
                });
            }
        });
    }

    private void runAuthAction(
            String message,
            SessionAction retryAction,
            NetworkAction action) {
        if (destroyed || serverExecutor.isShutdown()) return;
        toast(message);
        serverExecutor.execute(() -> {
            try {
                action.run();
            } catch (Exception e) {
                mainHandler.post(() -> {
                    toast(safeMessage(e));
                    showAuth(retryAction);
                });
            }
        });
    }

    private void remember(ServerCredentials credentials) {
        try {
            credentialStore.save(credentials);
        } catch (GeneralSecurityException e) {
            mainHandler.post(() -> toast(
                    "Signed in, but this device could not securely remember the account."));
        }
    }

    private void runBusy(String message, NetworkAction action) {
        if (destroyed || serverExecutor.isShutdown()) return;
        mainHandler.post(() -> toast(message));
        serverExecutor.execute(() -> {
            try {
                action.run();
            } catch (Exception e) {
                mainHandler.post(() -> toast(safeMessage(e)));
            }
        });
    }

    private String getStableNetworkUid() {
        android.content.SharedPreferences prefs =
                getSharedPreferences("iggamecenter_device", MODE_PRIVATE);
        String generated = prefs.getString("network_uid", null);
        if (generated == null) {
            generated = UUID.randomUUID().toString();
            prefs.edit().putString("network_uid", generated).apply();
        }
        return generated;
    }

    private static String firstAvailablePlace(List<Member> players) {
        boolean first = false;
        boolean second = false;
        for (Member player : players) {
            first |= "1".equals(player.place);
            second |= "2".equals(player.place);
        }
        if (!first) return "1";
        if (!second) return "2";
        return null;
    }

    private static Member member(List<Member> players, String uid) {
        for (Member player : players) if (uid.equals(player.uid)) return player;
        return null;
    }

    private static Member opponent(List<Member> players, String uid) {
        for (Member player : players) {
            if (!uid.equals(player.uid)
                    && ("1".equals(player.place) || "2".equals(player.place))) return player;
        }
        return null;
    }

    private static int parseTeam(String place) {
        if ("1".equals(place)) return 1;
        if ("2".equals(place)) return 2;
        throw new IllegalStateException("The server returned an invalid player seat.");
    }

    private static String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? "Could not connect to igGameCenter."
                : message;
    }

    private void toast(String message) {
        if (!destroyed) Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private interface SessionAction {
        void run(UserSession user) throws Exception;
    }

    private interface NetworkAction {
        void run() throws Exception;
    }
}
