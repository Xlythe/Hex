package com.xlythe.hex.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hex.core.GameAction;
import com.hex.core.PlayerObject;
import com.hex.core.Point;
import com.xlythe.hex.compat.Game;
import com.xlythe.hex.compat.GameOptions;
import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServerNetworkPlayerTest {
    @Test
    public void serializesLocalMoveThenAppliesMockRemoteMove() throws Exception {
        QueueTransport transport = new QueueTransport();
        transport.responses.add(handler(
                "<event eid=\"2\" uid=\"9\" type=\"MOVE\" data=\"B2\"/>"));
        IgGameCenterClient client = new IgGameCenterClient(transport, "device");
        PlayerObject local = new PlayerObject(1);
        ServerNetworkPlayer remote = player(client, 2, Runnable::run);
        Game game = new Game(
                new GameOptions.Builder().setGridSize(11).setSwapEnabled(true).build(),
                local,
                remote);
        assertTrue(GameAction.makeMove(local, new Point(0, 0), game));

        remote.getPlayerTurn(game);
        remote.quit();

        assertEquals(2, game.getMoveList().size());
        assertEquals(1, game.getMoveList().getMove().getX());
        assertEquals(1, game.getMoveList().getMove().getY());
        assertEquals("MOVE", transport.requests.get(0).get("cmd"));
        assertEquals("A1", transport.requests.get(0).get("move"));
    }

    @Test
    public void sendsSwapWhenSecondMoveReusesOpeningCell() throws Exception {
        QueueTransport transport = new QueueTransport();
        transport.responses.add(handler(
                "<event eid=\"2\" uid=\"9\" type=\"MOVE\" data=\"C3\"/>"));
        IgGameCenterClient client = new IgGameCenterClient(transport, "device");
        PlayerObject first = new PlayerObject(1);
        PlayerObject localSecond = new PlayerObject(2);
        ServerNetworkPlayer remoteFirst = player(client, 1, Runnable::run);
        Game game = new Game(
                new GameOptions.Builder().setGridSize(11).setSwapEnabled(true).build(),
                remoteFirst,
                localSecond);
        assertTrue(GameAction.makeMove(first, new Point(3, 4), game));
        assertTrue(GameAction.makeMove(localSecond, new Point(3, 4), game));

        remoteFirst.getPlayerTurn(game);
        remoteFirst.quit();

        assertEquals("SWAP", transport.requests.get(0).get("move"));
    }

    @Test
    public void appliesRemoteSwapReceivedByBackgroundRefresh() throws Exception {
        QueueTransport transport = new QueueTransport();
        transport.responses.add(handler(""));
        IgGameCenterClient client = new IgGameCenterClient(transport, "device");
        PlayerObject local = new PlayerObject(1);
        ServerNetworkPlayer remote = player(client, 2, Runnable::run);
        Game game = new Game(
                new GameOptions.Builder().setGridSize(11).setSwapEnabled(true).build(),
                local,
                remote);
        assertTrue(GameAction.makeMove(local, new Point(3, 4), game));

        remote.supportsUndo(game); // The game is active while the poll runs.
        remote.process(IgGameCenterXml.parseHandler(handler(
                "<event eid=\"2\" uid=\"9\" type=\"MOVE\" data=\"SWAP\"/>")), null);
        remote.getPlayerTurn(game);
        remote.quit();

        assertEquals(2, game.getMoveList().size());
        assertEquals(4, game.getMoveList().getMove().getX());
        assertEquals(3, game.getMoveList().getMove().getY());
        assertEquals(0, game.gamePieces[3][4].getTeam());
        assertEquals(2, game.gamePieces[4][3].getTeam());
    }

    @Test
    public void notifiesOnceWhenOpponentMoveMakesItOurTurn() throws Exception {
        QueueTransport transport = new QueueTransport();
        RecordingListener listener = new RecordingListener();
        ServerNetworkPlayer remote = new ServerNetworkPlayer(
                2, new IgGameCenterClient(transport, "device"),
                new UserSession("7", "Alice", "token"), new BoardRef("42", "gc1"),
                "9", 11, 0, listener, Runnable::run);
        String response = handler("<event eid=\"2\" uid=\"9\" type=\"MOVE\" data=\"B2\"/>")
                .replace("active=\"0\"", "active=\"1\"");
        remote.process(IgGameCenterXml.parseHandler(response), null);
        remote.process(IgGameCenterXml.parseHandler(response), null);
        assertEquals(1, listener.localTurns);
        remote.quit();
    }

    @Test
    public void advertisesUndoWithoutSendingDuringCapabilityChecks() {
        QueueTransport transport = new QueueTransport();
        IgGameCenterClient client = new IgGameCenterClient(transport, "device");
        ServerNetworkPlayer remote = player(client, 2, Runnable::run);
        Game game = new Game(
                new GameOptions.Builder().setGridSize(11).build(),
                new PlayerObject(1),
                remote);

        assertTrue(remote.supportsUndo(game));
        assertTrue(transport.requests.isEmpty());
        remote.quit();
    }

    @Test
    public void sendsLegacyUndoIndexAndWaitsForServerCompletion() throws Exception {
        QueueTransport transport = new QueueTransport();
        transport.responses.add(handler(
                "<event eid=\"2\" uid=\"7\" type=\"UNDODONE\" data=\"4\"/>"));
        RecordingListener listener = new RecordingListener();
        IgGameCenterClient client = new IgGameCenterClient(transport, "device");
        ServerNetworkPlayer remote = new ServerNetworkPlayer(
                2,
                client,
                new UserSession("7", "Alice", "token"),
                new BoardRef("42", "gc1"),
                "9",
                11,
                1,
                listener,
                Runnable::run);

        remote.requestUndo(4);

        assertTrue(listener.undoCompleted.await(1, TimeUnit.SECONDS));
        assertEquals("UNDO", transport.requests.get(0).get("cmd"));
        assertEquals("ASK", transport.requests.get(0).get("type"));
        assertEquals("4", transport.requests.get(0).get("move_ind"));
        assertEquals(4, listener.completedMoveIndex);
        remote.quit();
    }

    @Test
    public void sendsChatThroughSerializedGameCommandAndReceivesEcho() throws Exception {
        QueueTransport transport = new QueueTransport();
        transport.responses.add(handler(
                "<member uid=\"7\" name=\"Alice\" place=\"1\"/>"
                        + "<event eid=\"2\" stamp=\"123\" uid=\"7\" type=\"MSG\""
                        + " data=\"good luck\"/>"));
        RecordingListener listener = new RecordingListener();
        IgGameCenterClient client = new IgGameCenterClient(transport, "device");
        ServerNetworkPlayer remote = new ServerNetworkPlayer(
                2,
                client,
                new UserSession("7", "Alice", "token"),
                new BoardRef("42", "gc1"),
                "9",
                11,
                1,
                listener,
                Runnable::run);

        remote.sendChatMessage("local:1", "good luck");

        assertTrue(listener.chatDelivered.await(1, TimeUnit.SECONDS));
        assertEquals("MSG", transport.requests.get(0).get("cmd"));
        assertEquals("good luck", transport.requests.get(0).get("message"));
        assertEquals("good luck", listener.chatMessage);
        assertTrue(listener.ownChatMessage);
        remote.quit();
    }

    @Test
    public void streamSignalRefreshesPlayerImmediately() throws Exception {
        QueueTransport transport = new QueueTransport();
        transport.responses.add(handler(
                "<member uid=\"9\" name=\"Bob\" place=\"2\"/>"
                        + "<event eid=\"2\" uid=\"9\" type=\"MSG\" data=\"now\"/>"));
        RecordingListener listener = new RecordingListener();
        ServerNetworkPlayer remote = new ServerNetworkPlayer(
                2, new IgGameCenterClient(transport, "device"),
                new UserSession("7", "Alice", "token"), new BoardRef("42", "gc1"),
                "9", 11, 1, listener, Runnable::run);

        remote.requestRefresh();

        assertTrue(listener.chatArrived.await(1, TimeUnit.SECONDS));
        assertEquals("REFRESH", transport.requests.get(0).get("cmd"));
        assertEquals("now", listener.chatMessage);
        remote.quit();
    }

    private static ServerNetworkPlayer player(
            IgGameCenterClient client, int team, Executor externalExecutor) {
        return new ServerNetworkPlayer(
                team,
                client,
                new UserSession("7", "Alice", "token"),
                new BoardRef("42", "gc1"),
                "9",
                11,
                1,
                new NoOpListener(),
                externalExecutor);
    }

    private static String handler(String events) {
        return "<handlerData><sessionInfo status=\"ACTIVE\" owner=\"7\"/>"
                + "<memberInfo active=\"0\" finished=\"0\" place=\"1\"/>"
                + "<eventList>" + events + "</eventList></handlerData>";
    }

    private static final class QueueTransport implements IgGameCenterTransport {
        final List<String> responses = new ArrayList<>();
        final List<Map<String, String>> requests = new ArrayList<>();

        @Override
        public synchronized String post(String path, Map<String, String> parameters) {
            requests.add(new LinkedHashMap<>(parameters));
            return responses.remove(0);
        }
    }

    private static class NoOpListener implements ServerNetworkPlayer.Listener {
        @Override public void onNetworkError(String message) {}
        @Override public void onRestartCreated(BoardRef board) {}
        @Override public void onRestartOffered(BoardRef board) {}
        @Override public void onUndoRequested(int moveIndex) {}
        @Override public void onUndoCompleted(int moveIndex) {}
        @Override public void onChatMessage(
                long eventId, String sender, String message, long timestampSeconds,
                boolean ownMessage) {}
        @Override public void onChatDelivery(String localId, boolean delivered) {}
    }

    private static final class RecordingListener extends NoOpListener {
        int localTurns;
        @Override public void onLocalTurn() { localTurns++; }
        final CountDownLatch undoCompleted = new CountDownLatch(1);
        int completedMoveIndex = -1;
        final CountDownLatch chatDelivered = new CountDownLatch(1);
        String chatMessage;
        final CountDownLatch chatArrived = new CountDownLatch(1);
        boolean ownChatMessage;

        @Override
        public void onUndoCompleted(int moveIndex) {
            completedMoveIndex = moveIndex;
            undoCompleted.countDown();
        }

        @Override
        public void onChatMessage(
                long eventId, String sender, String message, long timestampSeconds,
                boolean ownMessage) {
            chatMessage = message;
            ownChatMessage = ownMessage;
            chatArrived.countDown();
        }

        @Override
        public void onChatDelivery(String localId, boolean delivered) {
            if (delivered) chatDelivered.countDown();
        }
    }
}
