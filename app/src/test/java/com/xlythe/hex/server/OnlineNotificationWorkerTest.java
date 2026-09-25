package com.xlythe.hex.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.xlythe.hex.server.IgGameCenterModels.Event;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.Member;

import org.junit.Test;

import java.util.Arrays;

public final class OnlineNotificationWorkerTest {
    @Test
    public void alertsOnlyForUnseenOpponentEventsAndLocalTurn() {
        HandlerResponse active = response(true);
        OnlineNotificationWorker.Alerts alerts =
                OnlineNotificationWorker.alerts(active, "alice", 3);
        assertTrue(alerts.turn);
        assertEquals("Bob", alerts.sender);
        assertEquals("new message", alerts.message);

        OnlineNotificationWorker.Alerts alreadySeen =
                OnlineNotificationWorker.alerts(active, "alice", 6);
        assertFalse(alreadySeen.turn);
        assertNull(alreadySeen.message);

        OnlineNotificationWorker.Alerts otherTurn =
                OnlineNotificationWorker.alerts(response(false), "alice", 3);
        assertFalse(otherTurn.turn);
    }

    private static HandlerResponse response(boolean localActive) {
        return new HandlerResponse("", "ACTIVE", "alice", "1", localActive,
                Arrays.asList(
                        new Member("alice", "Alice", "1", "READY", false, false, 0, 0),
                        new Member("bob", "Bob", "2", "READY", false, false, 0, 0)),
                Arrays.asList(
                        new Event(2, 0, "bob", "MSG", "old message"),
                        new Event(4, 0, "alice", "MOVE", "A1"),
                        new Event(5, 0, "bob", "MOVE", "B1"),
                        new Event(6, 0, "bob", "MSG", "new message")),
                null, "");
    }
}
