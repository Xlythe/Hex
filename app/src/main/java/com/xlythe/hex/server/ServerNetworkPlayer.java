package com.xlythe.hex.server;

import android.util.Log;

import com.hex.core.Game;
import com.hex.core.GameAction;
import com.hex.core.Move;
import com.hex.core.MoveList;
import com.hex.core.Player;
import com.hex.core.PlayingEntity;
import com.hex.core.Point;
import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.Event;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.Member;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * igGameCenter-backed opponent.
 *
 * <p>All gameplay HTTP calls share one executor. The next refresh is scheduled
 * only after the previous call has completed, so a slow or unavailable server
 * can never produce overlapping polls or a retry storm.</p>
 */
public final class ServerNetworkPlayer implements PlayingEntity {
    private static final String TAG = "HexServer";
    private static final Point END_MOVE = new Point(-2, -2);

    private final int team;
    private final IgGameCenterClient client;
    private final UserSession user;
    private final BoardRef board;
    private final String remoteUid;
    private final int boardSize;
    private final Listener listener;
    private final Executor outOfGameExecutor;
    private final ScheduledExecutorService ioExecutor;
    private final LinkedBlockingQueue<Point> pendingMoves = new LinkedBlockingQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    private volatile long lastEventId;
    private volatile boolean hasForfeited;
    private volatile long timeLeft;
    private volatile String name;
    private volatile int color;
    private volatile int sentMoveCount;
    private volatile Point openingMove;
    private volatile Game activeGame;
    private volatile ScheduledFuture<?> refreshFuture;
    private boolean errorReported;

    public ServerNetworkPlayer(
            int team,
            IgGameCenterClient client,
            UserSession user,
            BoardRef board,
            String remoteUid,
            int boardSize,
            long lastEventId,
            Listener listener,
            Executor outOfGameExecutor) {
        this(
                team,
                client,
                user,
                board,
                remoteUid,
                boardSize,
                lastEventId,
                listener,
                outOfGameExecutor,
                Executors.newSingleThreadScheduledExecutor(runnable -> {
                    Thread thread = new Thread(runnable, "Hex-server-player");
                    thread.setDaemon(true);
                    return thread;
                }));
    }

    ServerNetworkPlayer(
            int team,
            IgGameCenterClient client,
            UserSession user,
            BoardRef board,
            String remoteUid,
            int boardSize,
            long lastEventId,
            Listener listener,
            Executor outOfGameExecutor,
            ScheduledExecutorService ioExecutor) {
        if (team != 1 && team != 2) throw new IllegalArgumentException("Invalid team");
        this.team = team;
        this.client = client;
        this.user = user;
        this.board = board;
        this.remoteUid = remoteUid;
        this.boardSize = boardSize;
        this.lastEventId = Math.max(0, lastEventId);
        this.listener = listener;
        this.outOfGameExecutor = outOfGameExecutor;
        this.ioExecutor = ioExecutor;
    }

    @Override
    public void startGame() {
        scheduleRefresh();
    }

    @Override
    public void getPlayerTurn(Game game) {
        if (game.replayRunning || closed.get()) return;
        activeGame = game;

        submitLatestLocalMove(game);
        while (!closed.get()) {
            Point point;
            try {
                point = pendingMoves.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (point.equals(END_MOVE)) return;
            if (GameAction.makeMove(this, point, game)) return;
        }
    }

    private void submitLatestLocalMove(Game game) {
        MoveList moves = game.getMoveList();
        if (moves.size() == 0) return;
        Move move = moves.getMove();
        if (move.getTeam() == team || moves.size() <= sentMoveCount) return;

        final int moveCount = moves.size();
        final String encodedMove;
        Point current = new Point(move.getX(), move.getY());
        if (moveCount == 1) openingMove = current;
        if (moveCount == 2) {
            Point first = openingMove != null ? openingMove : firstMove(moves);
            encodedMove = first != null
                    && first.x == move.getX()
                    && first.y == move.getY()
                    ? "SWAP"
                    : IgGameCenterProtocol.encodeMove(current, boardSize);
        } else {
            encodedMove = IgGameCenterProtocol.encodeMove(current, boardSize);
        }

        executeGameplayRequest(() -> {
            HandlerResponse response = client.command(
                    board, user, lastEventId, "MOVE", "move", encodedMove);
            sentMoveCount = Math.max(sentMoveCount, moveCount);
            process(response, game);
        });
    }

    private void scheduleRefresh() {
        if (closed.get()) return;
        refreshFuture = ioExecutor.schedule(() -> {
            if (closed.get()) return;
            try {
                process(client.refresh(board, user, lastEventId), null);
                reportRecovered();
            } catch (Exception e) {
                reportError(e);
            } finally {
                scheduleRefresh();
            }
        }, IgGameCenterProtocol.POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void executeGameplayRequest(ThrowingRunnable request) {
        if (closed.get()) return;
        ioExecutor.execute(() -> {
            try {
                request.run();
                reportRecovered();
            } catch (Exception e) {
                reportError(e);
            }
        });
    }

    void process(HandlerResponse response, Game game) {
        long priorEventId = lastEventId;
        for (Event event : response.events) {
            if (event.eid <= priorEventId) continue;
            lastEventId = Math.max(lastEventId, event.eid);

            if ("MOVE".equals(event.type) && !user.uid.equals(event.uid)) {
                Point move = IgGameCenterProtocol.decodeMove(event.data, boardSize);
                if (IgGameCenterProtocol.isSwap(move)) {
                    Game current = game != null ? game : activeGame;
                    Point first = openingMove != null
                            ? openingMove
                            : current == null ? null : firstMove(current.getMoveList());
                    if (first != null) move = first;
                }
                if (move != null && !IgGameCenterProtocol.isSwap(move)) {
                    if (game != null && game.getMoveList().size() == 0) {
                        openingMove = new Point(move.x, move.y);
                    }
                    pendingMoves.offer(move);
                }
            } else if ("MSG".equals(event.type) && event.data != null) {
                Member sender = member(response, event.uid);
                String senderName = sender == null ? "" : sender.name;
                listener.onChatMessage(
                        event.eid,
                        senderName,
                        event.data,
                        event.stamp,
                        user.uid.equals(event.uid));
            } else if ("NOTICE".equals(event.type)
                    && remoteUid.equals(event.uid)
                    && event.data != null
                    && event.data.contains("PHRASE_GIVES_UP")) {
                markRemoteForfeit();
            } else if ("RESTART".equals(event.type)
                    && !user.uid.equals(event.uid)
                    && event.data != null
                    && !event.data.trim().isEmpty()) {
                listener.onRestartOffered(new BoardRef(event.data.trim(), board.server));
            } else if ("UNDOASK".equals(event.type) && !user.uid.equals(event.uid)) {
                listener.onUndoRequested(parseMoveIndex(event.data));
            } else if ("UNDODONE".equals(event.type)) {
                Game current = activeGame;
                if (current != null && current.getMoveList().size() > 0) {
                    Move undone = current.getMoveList().getMove();
                    if (undone != null && undone.getTeam() != team) {
                        sentMoveCount = Math.max(0, sentMoveCount - 1);
                    }
                }
                listener.onUndoCompleted(parseMoveIndex(event.data));
            }
        }

        lastEventId = response.latestEventId(lastEventId);
        for (Member player : response.players) {
            if (!remoteUid.equals(player.uid)) continue;
            if (player.timerLeft >= 0) setTime(player.timerLeft * 1000L);
            String status = player.status == null
                    ? ""
                    : player.status.toUpperCase(Locale.US);
            if ("QUIT".equals(status) || "LOST".equals(status)) markRemoteForfeit();
        }
    }

    private void markRemoteForfeit() {
        hasForfeited = true;
        pendingMoves.offer(END_MOVE);
    }

    /**
     * Explicit user exit. Game.stop() alone does not forfeit because it is also
     * used for rotation, fragment replacement, and rematch setup.
     */
    public void forfeit() {
        outOfGameExecutor.execute(() -> {
            try {
                client.command(board, user, lastEventId, "END", "type", "GIVEUP");
            } catch (Exception e) {
                listener.onNetworkError(safeMessage(e));
            }
        });
    }

    public void requestUndo(int moveIndex) {
        if (moveIndex < 0) return;
        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        parameters.put("type", "ASK");
        parameters.put("move_ind", Integer.toString(moveIndex));
        executeGameplayRequest(() -> process(
                client.handle(board, user, lastEventId, "UNDO", parameters),
                activeGame));
    }

    public void respondToUndo(boolean accept) {
        executeGameplayRequest(() -> process(
                client.command(
                        board,
                        user,
                        lastEventId,
                        "UNDO",
                        "type",
                        accept ? "ACCEPT" : "DENY"),
                activeGame));
    }

    public void sendChatMessage(String localId, String message) {
        String normalized = message == null ? "" : message.trim();
        if (normalized.isEmpty() || normalized.length() > GameChatStore.MAX_MESSAGE_LENGTH) {
            listener.onChatDelivery(localId, false);
            return;
        }
        executeGameplayRequest(() -> {
            try {
                process(
                        client.command(
                                board, user, lastEventId, "MSG", "message", normalized),
                        activeGame);
                listener.onChatDelivery(localId, true);
            } catch (Exception error) {
                listener.onChatDelivery(localId, false);
                throw error;
            }
        });
    }

    @Override
    public void newgameCalled() {
        outOfGameExecutor.execute(() -> {
            try {
                HandlerResponse response =
                        client.command(board, user, lastEventId, "RESTART");
                BoardRef replacement = restartBoard(response);
                if (replacement == null) {
                    listener.onNetworkError("The server did not create a rematch.");
                } else {
                    listener.onRestartCreated(replacement);
                }
            } catch (Exception e) {
                listener.onNetworkError(safeMessage(e));
            }
        });
    }

    private BoardRef restartBoard(HandlerResponse response) {
        lastEventId = response.latestEventId(lastEventId);
        for (Event event : response.events) {
            if ("RESTART".equals(event.type)
                    && event.data != null
                    && !event.data.trim().isEmpty()) {
                return new BoardRef(event.data.trim(), board.server);
            }
        }
        return null;
    }

    @Override
    public void lose(Game game) {
        // The game loop detects a local win before it enters the remote turn,
        // so submit the winning local move here.
        submitLatestLocalMove(game);
    }

    @Override
    public void quit() {
        if (!closed.compareAndSet(false, true)) return;
        ScheduledFuture<?> future = refreshFuture;
        if (future != null) future.cancel(true);
        pendingMoves.offer(END_MOVE);
        ioExecutor.shutdownNow();
    }

    @Override
    public void endMove() {
        pendingMoves.offer(END_MOVE);
    }

    @Override
    public boolean giveUp() {
        return hasForfeited;
    }

    @Override
    public void win() {}

    @Override
    public boolean supportsNewgame() {
        return true;
    }

    @Override
    public boolean supportsUndo(Game game) {
        activeGame = game;
        return !closed.get();
    }

    @Override
    public void undoCalled() {}

    @Override
    public boolean supportsSave() {
        return false;
    }

    @Override
    public Serializable getSaveState() {
        return null;
    }

    @Override
    public void setSaveState(Serializable state) {}

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setTime(long time) {
        this.timeLeft = time;
    }

    @Override
    public long getTime() {
        return timeLeft;
    }

    @Override
    public byte getTeam() {
        return (byte) team;
    }

    @Override
    public Player getType() {
        return Player.Net;
    }

    private void reportError(Exception error) {
        if (errorReported || closed.get()) return;
        errorReported = true;
        Log.w(TAG, "igGameCenter request failed; retrying after the normal poll interval");
        listener.onNetworkError(safeMessage(error));
    }

    private void reportRecovered() {
        errorReported = false;
    }

    private static String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? "Could not connect to the game server."
                : message;
    }

    private static int parseMoveIndex(String value) {
        try {
            return value == null ? -1 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Member member(HandlerResponse response, String uid) {
        for (Member player : response.players) {
            if (uid.equals(player.uid)) return player;
        }
        return null;
    }

    /**
     * MoveList.getPastMove() is broken in the bundled 2012 core. Reflection is
     * isolated here as a resume-game fallback; normal live games track their
     * opening coordinate as it is sent or received.
     */
    private static Point firstMove(MoveList moves) {
        try {
            Field field = MoveList.class.getDeclaredField("moveList");
            field.setAccessible(true);
            Object value = field.get(moves);
            if (!(value instanceof LinkedList) || ((LinkedList<?>) value).isEmpty()) return null;
            Object first = ((LinkedList<?>) value).getFirst();
            if (!(first instanceof Move)) return null;
            Move move = (Move) first;
            return new Point(move.getX(), move.getY());
        } catch (ReflectiveOperationException | SecurityException e) {
            Log.w(TAG, "Could not inspect legacy move list for swap detection");
            return null;
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    public interface Listener {
        void onNetworkError(String message);
        void onRestartCreated(BoardRef board);
        void onRestartOffered(BoardRef board);
        void onUndoRequested(int moveIndex);
        void onUndoCompleted(int moveIndex);
        void onChatMessage(
                long eventId,
                String sender,
                String message,
                long timestampSeconds,
                boolean ownMessage);
        void onChatDelivery(String localId, boolean delivered);
    }
}
