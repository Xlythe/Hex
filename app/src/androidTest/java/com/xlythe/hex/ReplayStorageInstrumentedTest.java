package com.xlythe.hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hex.core.PlayerObject;
import com.hex.core.Timer;
import com.xlythe.hex.compat.Game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public final class ReplayStorageInstrumentedTest {
    private static final String REPLAY_NAME = "API 37 replay";

    private Context context;
    private File replay;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        replay = new File(FileUtil.historyDirectory(context), REPLAY_NAME + ".rhex");
        deleteTestArtifacts();
    }

    @After
    public void tearDown() {
        deleteTestArtifacts();
    }

    @Test
    public void replayRoundTripsInsideAppPrivateStorage() throws Exception {
        Game.GameOptions options = new Game.GameOptions();
        options.gridSize = 11;
        options.swap = true;
        options.timer = new Timer(0, 0, Timer.NO_TIMER);
        PlayerObject player1 = new PlayerObject(1);
        player1.setName("Alice");
        player1.setColor(0xffff0000);
        PlayerObject player2 = new PlayerObject(2);
        player2.setName("Bob");
        player2.setColor(0xff0000ff);
        Game original = new Game(options, player1, player2);

        FileUtil.autoSaveGame(context, REPLAY_NAME, original.toString());

        assertTrue(replay.isFile());
        assertTrue(replay.getCanonicalPath().startsWith(
                context.getFilesDir().getCanonicalPath() + File.separator));
        assertFalse(replay.getCanonicalPath().contains(
                File.separator + "Android" + File.separator + "data"
                        + File.separator));

        Game restored = Game.load(FileUtil.loadGameAsString(context, replay.getName()));
        assertEquals(11, restored.getGridSize());
        assertTrue(restored.isFirstMoveSwapEnabled());
        assertEquals("Alice", restored.getPlayer1().getName());
        assertEquals("Bob", restored.getPlayer2().getName());
    }

    private void deleteTestArtifacts() {
        File directory = FileUtil.historyDirectory(context);
        new File(directory, REPLAY_NAME + ".rhex.tmp").delete();
        new File(directory, REPLAY_NAME + ".rhex.bak").delete();
        replay.delete();
    }
}
