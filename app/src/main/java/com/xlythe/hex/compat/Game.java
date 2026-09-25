package com.xlythe.hex.compat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hex.core.MoveList;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Timer;

import java.lang.reflect.Field;

public class Game extends com.hex.core.Game {
    private static final int REPLAY_FORMAT_VERSION = 1;
    private boolean hasStarted = false;

    public Game(GameOptions gameOptions, PlayingEntity player1, PlayingEntity player2) {
        super(gameOptions, player1, player2);
    }

    public int getGridSize() {
        return gameOptions.gridSize;
    }

    public boolean isFirstMoveSwapEnabled() {
        return gameOptions.swap;
    }

    public boolean hasTimer() {
        return gameOptions.timer.type != Timer.NO_TIMER;
    }

    public void startTimer() {
        gameOptions.timer.start(this);
    }

    public synchronized boolean hasStarted() {
        return hasStarted;
    }

    @Override
    public synchronized void start() {
        if (hasStarted) {
            return;
        }

        hasStarted = true;
        super.start();
    }

    @Override
    public synchronized void stop() {
        if (!hasStarted) {
            return;
        }

        super.stop();
        hasStarted = false;
    }

    /**
     * Produces the replay format consumed by {@link #load(String)}.
     *
     * <p>The current core library exposes the historical JSON representation
     * through {@link #save()} rather than {@code toString()}.</p>
     */
    @Override
    public String toString() {
        JsonObject replay = new JsonParser().parse(save()).getAsJsonObject();
        replay.addProperty("formatVersion", REPLAY_FORMAT_VERSION);
        return replay.toString();
    }

    public static Game load(String state) {
        return load(state, new PlayerObject(1), new PlayerObject(2));
    }

    public static Game load(String state, PlayingEntity player1, PlayingEntity player2) {
        JsonObject object = new JsonParser().parse(state).getAsJsonObject();
        if (object.has("formatVersion")) {
            int version = object.get("formatVersion").getAsInt();
            if (version < 1 || version > REPLAY_FORMAT_VERSION) {
                throw new IllegalArgumentException("Unsupported replay format version: " + version);
            }
        }

        Gson gson = new Gson();
        Game.GameOptions options = gson.fromJson(object.get("gameOptions"), Game.GameOptions.class);
        MoveList moves = gson.fromJson(object.get("moveList"), MoveList.class);

        player1.setColor(object.get("player1").getAsJsonObject().get("color").getAsInt());
        player1.setName(object.get("player1").getAsJsonObject().get("name").getAsString());
        player2.setColor(object.get("player2").getAsJsonObject().get("color").getAsInt());
        player2.setName(object.get("player2").getAsJsonObject().get("name").getAsString());

        Game game = new Game(options, player1, player2);
        game.setCurrentPlayer(object.get("currentPlayer").getAsInt());
        game.setStartTime(object.get("gameStart").getAsLong());
        game.setEndTime(object.get("gameEnd").getAsLong());
        game.setMoveList(moves);
        return game;
    }

    private Class getSuperClass() {
        return com.hex.core.Game.class;
    }

    private void setCurrentPlayer(int player) {
        try {
            Field field = getSuperClass().getDeclaredField("currentPlayer");
            field.setAccessible(true);
            field.setInt(this, player);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setStartTime(long startTime) {
        try {
            Field field = getSuperClass().getDeclaredField("gameStart");
            field.setAccessible(true);
            field.setLong(this, startTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setEndTime(long endTime) {
        try {
            Field field = getSuperClass().getDeclaredField("gameEnd");
            field.setAccessible(true);
            field.setLong(this, endTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setMoveList(MoveList moveList) {
        try {
            Field field = getSuperClass().getDeclaredField("moveList");
            field.setAccessible(true);
            field.set(this, moveList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
