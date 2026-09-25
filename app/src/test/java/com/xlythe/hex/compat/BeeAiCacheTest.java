package com.xlythe.hex.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.hex.ai.BeeGameAI;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class BeeAiCacheTest {
    @Test
    public void distinctBoardsDoNotShareAnOverflowedCacheKey() throws Exception {
        BeeGameAI bee = new BeeGameAI(1, 11, 2, 2);
        Field boardField = BeeGameAI.class.getDeclaredField("pieces");
        boardField.setAccessible(true);
        int[][] board = (int[][]) boardField.get(bee);
        Method keyMethod = BeeGameAI.class.getDeclaredMethod("piecesString");
        keyMethod.setAccessible(true);
        String emptyKey = (String) keyMethod.invoke(bee);
        int legacyEmptyKey = legacyKey(board);

        // The old base-3 key was stored in a 32-bit Integer. Adding exactly
        // 2^32 to its board digits produced a different board with the same key.
        long remaining = 1L << 32;
        for (int i = 11; i >= 1 && remaining != 0; i--) {
            for (int j = 11; j >= 1 && remaining != 0; j--) {
                board[i][j] = (int) (remaining % 3);
                remaining /= 3;
            }
        }
        assertEquals(0, remaining);
        assertEquals(legacyEmptyKey, legacyKey(board));
        assertNotEquals(emptyKey, keyMethod.invoke(bee));
    }

    private static int legacyKey(int[][] board) {
        int key = board.length - 2;
        for (int i = 1; i < board.length - 1; i++) {
            for (int j = 1; j < board.length - 1; j++) {
                key = key * 3 + board[i][j];
            }
        }
        return key;
    }
}
