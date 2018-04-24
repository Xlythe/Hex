package com.sam.hex.compat;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hex.core.MoveList;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Timer;

import java.lang.reflect.Field;

public class Game extends com.hex.core.Game {
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

    public static Game load(String state) {
        return load(state, new PlayerObject(1), new PlayerObject(2));
    }

    public static Game load(String state, PlayingEntity player1, PlayingEntity player2) {
        Log.d("TEST", "Game state: " + state);
        JsonObject object = new JsonParser().parse(state).getAsJsonObject();

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
