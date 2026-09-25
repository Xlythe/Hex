package com.xlythe.hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.xlythe.hex.server.IgGameCenterClient;
import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedHashMap;

/** Device-level POST/XML smoke test against the live mirror. */
@RunWith(AndroidJUnit4.class)
public final class LiveMirrorInstrumentedTest {
    @Test
    public void twoAndroidClientsCanPlayAndChat() throws Exception {
        String suffix = Long.toString(System.currentTimeMillis(), 36);
        IgGameCenterClient first = IgGameCenterClient.production("device-first-" + suffix);
        IgGameCenterClient second = IgGameCenterClient.production("device-second-" + suffix);
        String password = "test password 2026";
        String firstName = "AndroidA" + suffix;
        String secondName = "AndroidB" + suffix;
        first.register(firstName, password, null);
        second.register(secondName, password, null);
        UserSession alice = first.login(firstName, password);
        UserSession bob = second.login(secondName, password);

        BoardRef board = first.createBoard(alice, "1");
        assertFalse(board.sid.isEmpty());
        LinkedHashMap<String, String> setup = new LinkedHashMap<>();
        setup.put("boardSize", "11");
        first.handle(board, alice, 0, "SETUP", setup);
        assertTrue(second.fetchLobby(bob).stream().anyMatch(room -> board.sid.equals(room.sid)));
        second.handle(board, bob, 0, null, null);
        second.command(board, bob, 0, "PLACE", "place", "2");
        first.command(board, alice, 0, "START");
        HandlerResponse started = second.command(board, bob, 0, "START");
        assertEquals("ACTIVE", started.status);
        assertFalse(started.localActive);

        first.command(board, alice, 0, "MOVE", "move", "A1");
        HandlerResponse seen = second.refresh(board, bob, 0);
        assertEquals('1', seen.board.charAt(0));
        assertTrue(seen.localActive);
        second.command(board, bob, seen.latestEventId(0), "MSG", "message", "Android chat");
        assertTrue(first.refresh(board, alice, 0).events.stream()
                .anyMatch(event -> "MSG".equals(event.type) && "Android chat".equals(event.data)));
    }
}
