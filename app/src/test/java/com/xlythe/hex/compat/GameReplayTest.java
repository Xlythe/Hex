package com.xlythe.hex.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hex.core.PlayerObject;
import com.hex.core.Timer;

import org.junit.Test;

public final class GameReplayTest {
    @Test
    public void persistedJsonRoundTripsRequiredGameState() {
        Game.GameOptions options = new Game.GameOptions();
        options.gridSize = 11;
        options.swap = true;
        options.timer = new Timer(0, 0, Timer.NO_TIMER);
        PlayerObject player1 = player(1, "Alice", 0xffff0000);
        PlayerObject player2 = player(2, "Bob", 0xff0000ff);
        Game original = new Game(options, player1, player2);

        String replay = original.toString();
        JsonObject json = new JsonParser().parse(replay).getAsJsonObject();
        assertTrue(json.has("gameOptions"));
        assertTrue(json.has("moveList"));
        assertTrue(json.has("player1"));
        assertTrue(json.has("player2"));
        assertEquals(1, json.get("formatVersion").getAsInt());

        Game restored = Game.load(replay);
        assertEquals(11, restored.getGridSize());
        assertTrue(restored.isFirstMoveSwapEnabled());
        assertEquals("Alice", restored.getPlayer1().getName());
        assertEquals("Bob", restored.getPlayer2().getName());

        json.remove("formatVersion");
        assertEquals("Alice", Game.load(json.toString()).getPlayer1().getName());

        json.addProperty("formatVersion", 2);
        try {
            Game.load(json.toString());
            fail("A future replay version must not be silently misread");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("version"));
        }
    }

    private PlayerObject player(int number, String name, int color) {
        PlayerObject player = new PlayerObject(number);
        player.setName(name);
        player.setColor(color);
        return player;
    }
}
