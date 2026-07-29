package com.xlythe.hex.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class GameChatStoreTest {
    @Test
    public void reconcilesOwnServerEchoWithoutDuplicateBubble() {
        GameChatStore store = new GameChatStore();
        store.addOutgoing("local:1", "Alice", " hello ", 10);

        boolean added = store.receive("event:4", "Alice", "hello", 11, true);

        assertFalse(added);
        assertEquals(1, store.snapshot().size());
        assertEquals(GameChatMessage.Delivery.SENT, store.snapshot().get(0).delivery);
        assertEquals(0, store.unreadCount());
    }

    @Test
    public void countsOnlyRemoteMessagesAsUnread() {
        GameChatStore store = new GameChatStore();

        store.receive("event:1", "Bob", "hi", 10, false);
        store.receive("event:2", "Alice", "hello", 11, true);

        assertEquals(2, store.snapshot().size());
        assertEquals(1, store.unreadCount());
        store.markRead();
        assertEquals(0, store.unreadCount());
    }

    @Test
    public void recordsFailedOptimisticDelivery() {
        GameChatStore store = new GameChatStore();
        store.addOutgoing("local:1", "Alice", "hello", 10);

        store.markFailed("local:1");

        assertEquals(GameChatMessage.Delivery.FAILED, store.snapshot().get(0).delivery);
    }
}
