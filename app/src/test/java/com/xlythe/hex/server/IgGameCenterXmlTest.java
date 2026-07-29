package com.xlythe.hex.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.xlythe.hex.server.IgGameCenterModels.ApiException;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.LobbyBoard;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import org.junit.Test;

import java.util.List;

public class IgGameCenterXmlTest {
    @Test
    public void parsesLoginAndApiErrors() throws Exception {
        UserSession login = IgGameCenterXml.parseLogin(
                "<loginResult><uid>123</uid><name>Alice</name>"
                        + "<session_id>secret</session_id></loginResult>");
        assertEquals("123", login.uid);
        assertEquals("Alice", login.name);
        assertEquals("secret", login.sessionId);

        try {
            IgGameCenterXml.parseLogin("<errorMessage>INVALID_PASSWORD</errorMessage>");
        } catch (ApiException e) {
            assertEquals("INVALID_PASSWORD", e.getMessage());
            return;
        }
        throw new AssertionError("Expected API error");
    }

    @Test
    public void parsesHandlerFixture() throws Exception {
        HandlerResponse response = IgGameCenterXml.parseHandler(
                "<handlerData>"
                        + "<sessionInfo cmd=\"REFRESH\" curtime=\"1\" status=\"ACTIVE\" owner=\"123\"/>"
                        + "<memberInfo active=\"0\" finished=\"0\" place=\"1\"/>"
                        + "<playerList>"
                        + "<player uid=\"123\" name=\"Alice\" place=\"1\" stat=\"PLAYING\""
                        + " lastRefresh=\"9\" active=\"0\" finished=\"0\"/>"
                        + "<player uid=\"456\" name=\"Bob\" place=\"2\" stat=\"PLAYING\""
                        + " lastRefresh=\"10\" timerLeft=\"294\" active=\"1\" finished=\"0\"/>"
                        + "</playerList>"
                        + "<eventList><event eid=\"17\" stamp=\"10\" uid=\"456\""
                        + " type=\"MOVE\" data=\"C7\"/></eventList>"
                        + "<gameData><board>1002</board></gameData>"
                        + "<gameOptions><hidden>0</hidden><scored>1</scored>"
                        + "<boardSize>11</boardSize><timerTotal>300</timerTotal>"
                        + "<timerInc>5</timerInc></gameOptions>"
                        + "</handlerData>");

        assertEquals("ACTIVE", response.status);
        assertEquals("1", response.localPlace);
        assertEquals(2, response.players.size());
        assertEquals("C7", response.events.get(0).data);
        assertEquals(17, response.latestEventId(0));
        assertEquals("1002", response.board);
        assertEquals(11, response.options.boardSize);
        assertEquals(300, response.options.timerTotalSeconds);
        assertFalse(response.options.hidden);
        assertTrue(response.options.scored);
    }

    @Test
    public void parsesLobbyAndAvailablePlaces() throws Exception {
        List<LobbyBoard> boards = IgGameCenterXml.parseLobby(
                "<sessionList><session sid=\"99\" serv=\"gc1\" stat=\"INIT\""
                        + " uid=\"123\" priv=\"0\"><member plc=\"1\" uid=\"123\""
                        + " nam=\"Alice\" stat=\"NONE\"/></session></sessionList>");
        assertEquals(1, boards.size());
        assertEquals("2", boards.get(0).firstAvailablePlace());
    }

    @Test(expected = ApiException.class)
    public void rejectsDocumentTypes() throws Exception {
        IgGameCenterXml.parse("<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<foo>&xxe;</foo>");
    }
}
