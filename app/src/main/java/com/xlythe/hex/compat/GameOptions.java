package com.xlythe.hex.compat;

import com.hex.core.Timer;

public class GameOptions extends com.hex.core.Game.GameOptions {
    private GameOptions() {}

    public static class Builder {
        private final GameOptions gameOptions = new GameOptions();

        public Builder() {
            setNoTimer();
        }

        public Builder setGridSize(int gridSize) {
            gameOptions.gridSize = gridSize;
            return this;
        }

        public Builder setSwapEnabled(boolean enabled) {
            gameOptions.swap = enabled;
            return this;
        }

        public Builder setTimer(Timer timer) {
            gameOptions.timer = timer;
            return this;
        }

        public Builder setTimerPerMove(long totalTimeMinutes, long additionalTimePerMoveSeconds) {
            return setTimer(new Timer(totalTimeMinutes, additionalTimePerMoveSeconds, Timer.PER_MOVE));
        }

        public Builder setTimerForEntireMatch(long totalTimeMinutes) {
            return setTimer(new Timer(totalTimeMinutes, 0, Timer.ENTIRE_MATCH));
        }

        public Builder setNoTimer() {
            return setTimer(new Timer(0, 0, Timer.NO_TIMER));
        }

        public GameOptions build() {
            return gameOptions;
        }
    }
}
