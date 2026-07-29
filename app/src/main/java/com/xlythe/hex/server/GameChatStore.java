package com.xlythe.hex.server;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main-thread chat state with deterministic reconciliation of optimistic sends.
 *
 * <p>The server may echo the sender's MSG event before or after the command
 * response. Matching the oldest pending message prevents duplicate bubbles.</p>
 */
public final class GameChatStore {
    public static final int MAX_MESSAGE_LENGTH = 200;
    private static final int MAX_MESSAGES = 100;

    private final List<GameChatMessage> messages = new ArrayList<>();
    private int unreadCount;

    @NonNull
    public synchronized List<GameChatMessage> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public synchronized int unreadCount() {
        return unreadCount;
    }

    public synchronized void clear() {
        messages.clear();
        unreadCount = 0;
    }

    public synchronized void markRead() {
        unreadCount = 0;
    }

    public synchronized void addOutgoing(
            String localId, String sender, String text, long timestampMillis) {
        messages.add(new GameChatMessage(
                localId,
                sender,
                normalize(text),
                timestampMillis,
                true,
                GameChatMessage.Delivery.SENDING));
        trimHistory();
    }

    /**
     * Adds an event from the server, or acknowledges a matching optimistic row.
     *
     * @return true when a new visible row was added.
     */
    public synchronized boolean receive(
            String eventId,
            String sender,
            String text,
            long timestampMillis,
            boolean ownMessage) {
        String normalized = normalize(text);
        for (GameChatMessage message : messages) {
            if (eventId.equals(message.id)) return false;
        }
        if (ownMessage) {
            for (int i = 0; i < messages.size(); i++) {
                GameChatMessage message = messages.get(i);
                if (message.ownMessage
                        && message.delivery == GameChatMessage.Delivery.SENDING
                        && message.text.equals(normalized)) {
                    messages.set(i, message.withDelivery(GameChatMessage.Delivery.SENT));
                    return false;
                }
            }
        }
        messages.add(new GameChatMessage(
                eventId,
                sender,
                normalized,
                timestampMillis,
                ownMessage,
                GameChatMessage.Delivery.SENT));
        if (!ownMessage) unreadCount++;
        trimHistory();
        return true;
    }

    public synchronized void markDelivered(String localId) {
        updateDelivery(localId, GameChatMessage.Delivery.SENT);
    }

    public synchronized void markFailed(String localId) {
        updateDelivery(localId, GameChatMessage.Delivery.FAILED);
    }

    private void updateDelivery(String localId, GameChatMessage.Delivery delivery) {
        for (int i = 0; i < messages.size(); i++) {
            GameChatMessage message = messages.get(i);
            if (localId.equals(message.id)) {
                messages.set(i, message.withDelivery(delivery));
                return;
            }
        }
    }

    private void trimHistory() {
        while (messages.size() > MAX_MESSAGES) messages.remove(0);
        unreadCount = Math.min(unreadCount, messages.size());
    }

    private static String normalize(String text) {
        if (text == null) return "";
        String normalized = text.trim();
        return normalized.length() <= MAX_MESSAGE_LENGTH
                ? normalized
                : normalized.substring(0, MAX_MESSAGE_LENGTH);
    }
}
