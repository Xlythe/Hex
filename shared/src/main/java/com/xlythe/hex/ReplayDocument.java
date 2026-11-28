package com.xlythe.hex;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xlythe.hex.compat.Game;
import com.hex.core.GameAction;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;


/** Validates an external .rhex before allocating a board or placing it in game history. */
public final class ReplayDocument {
    public static final int MAX_BOARD_SIZE = 30;

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
        String text = bytes.toString("UTF-8");
        try {
            JsonObject root = new JsonParser().parse(text).getAsJsonObject();
            JsonObject options = root.getAsJsonObject("gameOptions");
            int size = options.get("gridSize").getAsBigDecimal().intValueExact();
            if (size < 3 || size > MAX_BOARD_SIZE) throw new IOException("Unsupported board size");
            JsonArray moves = root.getAsJsonObject("moveList").getAsJsonArray("moveList");
            if (moves.size() > size * size + 1) throw new IOException("Too many moves");
            Game game = Game.load(text);
            if (game.gameOptions.timer == null) throw new IOException("Missing timer");
            int winner = 0;
            int firstX = -1, firstY = -1;
            for (int i = 0; i < moves.size(); i++) {
                if (winner != 0) throw new IOException("Move after a winning connection");
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
                    game.gamePieces[firstX][firstY].setTeam((byte) 0, game);
                }
                if (game.gamePieces[x][y].getTeam() != 0) throw new IOException("Cell already occupied");
                game.gamePieces[x][y].setTeam((byte) team, game);
                if (GameAction.checkWinPlayer(team, game)) winner = team;
                if (i == 0) { firstX = x; firstY = y; }
            }
            if (root.has("result")) {
                JsonObject result = root.getAsJsonObject("result");
                if (winner == 0 && result.has("reason") && !result.get("reason").isJsonNull()
                        && "connection".equals(result.get("reason").getAsString())) {
                    throw new IOException("Missing winning connection");
                }
                if (result.has("winner") && !result.get("winner").isJsonNull()) {
                    int recorded = result.get("winner").getAsBigDecimal().intValueExact();
                    if ((recorded != 1 && recorded != 2) || (winner != 0 && recorded != winner)) throw new IOException("Invalid replay winner");
                }
            }
        } catch (RuntimeException invalid) {
            throw (IOException) new IOException("Invalid Hex replay").initCause(invalid);
        }
        return text;
    }
}
