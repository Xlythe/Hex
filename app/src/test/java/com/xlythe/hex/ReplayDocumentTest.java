package com.xlythe.hex;

import com.hex.core.GameAction;
import com.xlythe.hex.compat.Game;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class ReplayDocumentTest {
    private String browserReplay() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/browser-game.rhex")) {
            return ReplayDocument.read(input);
        }
    }

    @Test public void browserSavePlaysToSameWinner() throws Exception {
        Game game = Game.load(browserReplay());
        assertEquals(7, game.getMoveList().size());
        assertEquals(1, game.replayWinner());
        game.getMoveList().replay(0, game);
        assertEquals(2, game.gamePieces[0][1].getTeam());
        assertTrue(GameAction.checkWinPlayer(1, game));
        assertFalse(GameAction.checkWinPlayer(2, game));
        assertEquals("Alice", game.getPlayer1().getName());
    }

    @Test public void rejectsInvalidDocumentsBeforeImport() throws Exception {
        String json = browserReplay();
        for (String invalid : new String[]{json.replace("\"gridSize\":3", "\"gridSize\":30000"),
                json.replace("\"formatVersion\":1", "\"formatVersion\":2"),
                json.replace("\"team\":2", "\"team\":1"), "{", "null"}) {
            try {
                ReplayDocument.read(new ByteArrayInputStream(invalid.getBytes(StandardCharsets.UTF_8)));
                fail("Accepted invalid replay");
            } catch (IOException expected) { }
        }
        try {
            ReplayDocument.read(new ByteArrayInputStream(new byte[1024 * 1024 + 1]));
            fail("Accepted oversized replay");
        } catch (IOException expected) { }
    }
}
