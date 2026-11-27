package com.xlythe.hex;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xlythe.hex.compat.Game;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Validates an external .rhex before allocating a board or placing it in game history. */
public final class ReplayDocument {
    private ReplayDocument() {}

    public static String read(InputStream input) throws IOException {
        if (input == null) throw new IOException("No replay stream");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (count + bytes.size() > 1024 * 1024) throw new IOException("Replay is too large");
            bytes.write(buffer, 0, count);
        }
        String text = bytes.toString(StandardCharsets.UTF_8.name());
        try {
            JsonObject root = new JsonParser().parse(text).getAsJsonObject();
            JsonObject options = root.getAsJsonObject("gameOptions");
            int size = options.get("gridSize").getAsBigDecimal().intValueExact();
            if (size < 3 || size > Settings.MAX_BOARD_SIZE) throw new IOException("Unsupported board size");
            boolean[][] occupied = new boolean[size][size];
            JsonArray moves = root.getAsJsonObject("moveList").getAsJsonArray("moveList");
            if (moves.size() > size * size + 1) throw new IOException("Too many moves");
            int firstX = -1, firstY = -1;
            for (int i = 0; i < moves.size(); i++) {
                JsonObject move = moves.get(i).getAsJsonObject();
                int x = move.get("x").getAsBigDecimal().intValueExact();
                int y = move.get("y").getAsBigDecimal().intValueExact();
                int team = move.get("team").getAsBigDecimal().intValueExact();
                if (x < 0 || x >= size || y < 0 || y >= size || team != i % 2 + 1) {
                    throw new IOException("Invalid move");
                }
                if (move.has("swap") && move.get("swap").getAsBoolean()) {
                    if (i != 1 || !options.get("swap").getAsBoolean() || x != firstY || y != firstX) {
                        throw new IOException("Invalid swap");
                    }
                    occupied[firstX][firstY] = false;
                }
                if (occupied[x][y]) throw new IOException("Cell already occupied");
                occupied[x][y] = true;
                if (i == 0) { firstX = x; firstY = y; }
            }
            if (root.has("result")) {
                JsonObject result = root.getAsJsonObject("result");
                if (result.has("winner") && !result.get("winner").isJsonNull()) {
                    int winner = result.get("winner").getAsBigDecimal().intValueExact();
                    if (winner != 1 && winner != 2) throw new IOException("Invalid replay winner");
                }
            }
            if (Game.load(text).gameOptions.timer == null) throw new IOException("Missing timer");
        } catch (RuntimeException invalid) {
            throw new IOException("Invalid Hex replay", invalid);
        }
        return text;
    }
}
