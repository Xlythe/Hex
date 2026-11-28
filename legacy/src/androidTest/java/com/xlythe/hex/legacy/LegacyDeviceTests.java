package com.xlythe.hex.legacy;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewGroup;

import com.hex.core.GameAction;
import com.hex.core.PlayerObject;
import com.hex.core.Point;
import com.xlythe.hex.FileUtil;
import com.xlythe.hex.ReplayDocument;
import com.xlythe.hex.compat.AndroidBotFactory;
import com.xlythe.hex.compat.Game;
import com.xlythe.hex.server.IgGameCenterClient;
import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.Event;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;
import java.util.UUID;
import com.xlythe.hex.server.IgGameCenterXml;
import com.xlythe.hex.view.BoardView;
import com.xlythe.hex.view.HexagonLayout;

import java.io.InputStream;

/** Uses platform instrumentation because AndroidX Test requires a newer OS than Eclair. */
public final class LegacyDeviceTests extends Instrumentation {
    private boolean online;
    @Override public void onCreate(Bundle args) {
        super.onCreate(args);
        online = args != null && "true".equals(args.getString("online"));
        start();
    }

    private void playOnline() throws Exception {
        String run = Long.toString(System.currentTimeMillis(), 36);
        String password = UUID.randomUUID().toString();
        IgGameCenterClient client = LegacyServer.connect("legacy-" + run);
        client.register("LegacyA" + run, password, null);
        client.register("LegacyB" + run, password, null);
        UserSession first = client.login("LegacyA" + run, password);
        UserSession second = client.login("LegacyB" + run, password);
        BoardRef room = client.createBoard(first, "1");
        try {
            client.handle(room, second, 0, null, null);
            client.command(room, second, 0, "PLACE", "place", "2");
            client.command(room, first, 0, "START");
            client.command(room, second, 0, "START");
            client.command(room, first, 0, "MOVE", "move", "A2");
            HandlerResponse afterSwap = client.command(room, second, 0, "MOVE", "move", "SWAP");
            if (afterSwap.board.charAt(1) != '2') throw new AssertionError("Remote swap missing");
            client.command(room, first, 0, "MSG", "message", "Hello from legacy Hex");
            for (Event event : client.refresh(room, second, 0).events) {
                if ("MSG".equals(event.type) && "Hello from legacy Hex".equals(event.data)) return;
            }
            throw new AssertionError("Remote chat missing");
        } finally {
            client.command(room, first, 0, "LEAVE");
        }
    }

    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            Activity activity = startActivitySync(new Intent(getTargetContext(), LegacyActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            runOnMainSync(() -> {
                ViewGroup content = activity.findViewById(android.R.id.content);
                HexagonLayout wheel = (HexagonLayout) ((ViewGroup) content.getChildAt(0)).getChildAt(0);
                if (wheel.getButtons().length != 6) throw new AssertionError("Missing menu wedge");
                wheel.getButtons()[5].performClick();
                if (!(((ViewGroup) content.getChildAt(0)).getChildAt(0) instanceof BoardView))
                    throw new AssertionError("Play did not open the board");
            });
            String text;
            try (InputStream input = getContext().getAssets().open("browser-game.rhex")) {
                text = ReplayDocument.read(input);
            }
            Game imported = Game.load(text);
            if (imported.replayWinner() != 1 || imported.getMoveList().size() != 7)
                throw new AssertionError("Browser replay lost its winner or swap");
            FileUtil.autoSaveGame(activity, "device-test", imported.save());
            try (InputStream shared = activity.getContentResolver().openInputStream(ReplayProvider.uri("device-test.rhex"))) {
                if (Game.load(ReplayDocument.read(shared)).replayWinner() != 1)
                    throw new AssertionError("Shared replay changed the winner");
            }
            for (int d = 0; d < 3; d++) {
                Game game = LegacyActivity.newGame(7, new PlayerObject(1), AndroidBotFactory.create(d, 2, 7));
                try {
                    game.start();
                    long deadline = SystemClock.elapsedRealtime() + 30000;
                    while (game.getMoveNumber() < 3 && SystemClock.elapsedRealtime() < deadline) {
                        if (game.getMoveNumber() == 1) GameAction.setPiece(new Point(0, 1), game);
                        SystemClock.sleep(50);
                    }
                    if (game.getMoveNumber() != 3) throw new AssertionError("Bot did not reply at difficulty " + d);
                } finally { game.stop(); }
            }
            if (!"test".equals(IgGameCenterXml.parseLogin("<response><name>test</name><uid>1</uid><session_id>s</session_id></response>").name))
                throw new AssertionError("Login XML");
            runOnMainSync(activity::finish);
            if (online) {
                playOnline();

            }
            result.putString("stream", "PASS: wheel, local board, three bots, browser replay, file sharing and XML");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable error) {
            result.putString("stream", Log.getStackTraceString(error));
            finish(Activity.RESULT_CANCELED, result);
        }
    }
}
