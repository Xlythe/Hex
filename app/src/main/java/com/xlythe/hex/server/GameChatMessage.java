package com.xlythe.hex.server;

import androidx.annotation.NonNull;

/** Immutable chat row shared by the network layer and UI. */
public final class GameChatMessage {
    public enum Delivery {
        SENDING,
        SENT,
        FAILED
    }

    public final String id;
    public final String sender;
    public final String text;
    public final long timestampMillis;
    public final boolean ownMessage;
    public final Delivery delivery;

    public GameChatMessage(
            String id,
            String sender,
            String text,
            long timestampMillis,
            boolean ownMessage,
            Delivery delivery) {
        this.id = id;
        this.sender = sender;
        this.text = text;
        this.timestampMillis = timestampMillis;
        this.ownMessage = ownMessage;
        this.delivery = delivery;
    }

    GameChatMessage withDelivery(Delivery newDelivery) {
        return new GameChatMessage(
                id, sender, text, timestampMillis, ownMessage, newDelivery);
    }

    @NonNull
    @Override
    public String toString() {
        return sender + ": " + text;
    }
}
