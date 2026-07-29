package com.xlythe.hex.server;

import com.hex.core.Point;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/** Wire-level constants and codecs from the last production pre-Google-Play client. */
public final class IgGameCenterProtocol {
    public static final String APP_ID = "17";
    public static final String APP_CODE = "wihamo8984";
    public static final String HEX_GAME_ID = "12";
    public static final long POLL_INTERVAL_MS = 15_000L;
    public static final String DEFAULT_API_BASE_URL =
            "https://ig-game-center-proxy-12702774477.us-west1.run.app";

    private static final String COLUMNS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private IgGameCenterProtocol() {}

    public static String encodeMove(Point point, int boardSize) {
        validateBoardSize(boardSize);
        if (point.x < 0 || point.x >= boardSize
                || point.y < 0 || point.y >= boardSize) {
            throw new IllegalArgumentException("Point is outside the Hex board");
        }
        return COLUMNS.charAt(point.y) + Integer.toString(point.x + 1);
    }

    public static Point decodeMove(String value, int boardSize) {
        validateBoardSize(boardSize);
        if (value == null) return null;
        String normalized = value.trim().toUpperCase(Locale.US);
        if ("SWAP".equals(normalized)) return new Point(-1, -1);
        if (!normalized.matches("^[A-Z][1-9][0-9]*$")) return null;

        int column = COLUMNS.indexOf(normalized.charAt(0));
        int row;
        try {
            row = Integer.parseInt(normalized.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return null;
        }
        if (column < 0 || column >= boardSize || row < 0 || row >= boardSize) return null;
        return new Point(row, column);
    }

    public static boolean isSwap(Point point) {
        return point != null && point.x == -1 && point.y == -1;
    }

    public static boolean isValidServerName(String serverName) {
        return serverName != null && serverName.matches("(?i)^[a-z0-9][a-z0-9-]{0,62}$");
    }

    public static String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(32);
            for (byte current : bytes) {
                result.append(String.format(Locale.US, "%02x", current & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Android must provide MD5 for the legacy login API", e);
        }
    }

    private static void validateBoardSize(int boardSize) {
        if (boardSize < 1 || boardSize > COLUMNS.length()) {
            throw new IllegalArgumentException("Unsupported Hex board size: " + boardSize);
        }
    }
}
