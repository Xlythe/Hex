package com.xlythe.hex.compat;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.hex.ai.AiTypes;
import com.hex.ai.GameAI;
import com.hex.core.AI;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The production Android bot roster. Tree is an experimental alternative to Bee.
 */
public final class AndroidBotFactory {
    public static final int EASY = 0;
    public static final int MEDIUM = 1;
    public static final int HARD = 2;
    public static final int TREE = 3;
    public static final int BOT_COUNT = 4;

    @IntDef({EASY, MEDIUM, HARD, TREE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Difficulty {}

    private AndroidBotFactory() {}

    @NonNull
    public static AI create(
            @Difficulty int difficulty,
            int team,
            int gridSize) {
        switch (difficulty) {
            case EASY:
                return new GameAI(team);
            case MEDIUM:
            case HARD:
                // The legacy factory maps level N to depth N and beam 7 - N.
                return AiTypes.newAI(AiTypes.BeeAI, team, gridSize, difficulty + 1);
            case TREE:
                return AiTypes.newAI(AiTypes.TreeAI, team, gridSize,
                        gridSize <= 11 ? 12000 : 4000);
            default:
                throw new IllegalArgumentException("Unknown bot difficulty: " + difficulty);
        }
    }
}
