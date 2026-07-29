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
    public void doesNotAdvertiseCoreUndoThatWouldDesynchronizeBoards() {
        QueueTransport transport = new QueueTransport();
        IgGameCenterClient client = new IgGameCenterClient(transport, "device");
        ServerNetworkPlayer remote = player(client, 2, Runnable::run);
        Game game = new Game(
                new GameOptions.Builder().setGridSize(11).build(),
                new PlayerObject(1),
                remote);

        assertFalse(remote.supportsUndo(game));
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

    private static final class NoOpListener implements ServerNetworkPlayer.Listener {
        @Override public void onNetworkError(String message) {}
        @Override public void onRestartCreated(BoardRef board) {}
        @Override public void onRestartOffered(BoardRef board) {}
        @Override public void onUndoUnavailable() {}
    }
}
