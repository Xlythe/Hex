package com.sam.hex;

/**
 * @author Will Harmon
 **/
public class Timer implements Runnable {
    public static final int NO_TIMER = 0;
    public static final int PER_MOVE = 1;
    public static final int ENTIRE_MATCH = 2;
    private boolean refresh = true;
    public long startTime;
    private long elapsedTime;
    public int type;
    public long totalTime;
    public long additionalTime;
    public Game game;
    private int currentPlayer;

    public Timer(Game game, long totalTime, long additionalTime, int type) {
        this.game = game;
        this.totalTime = totalTime * 60 * 1000;
        this.additionalTime = additionalTime * 1000;
        this.type = type;
        startTime = System.currentTimeMillis();
    }

    public void start() {
        refresh = true;
        if(type != 0) {
            game.gameListener.startTimer();
            new Thread(this).start();
        }
    }

    public void stop() {
        refresh = false;
    }

    @Override
    public void run() {
        while(refresh) {
            elapsedTime = System.currentTimeMillis() - startTime;
            currentPlayer = game.currentPlayer;

            if(!game.gameOver) {
                GameAction.getPlayer(currentPlayer, game).setTime(calculatePlayerTime(currentPlayer));
                if(GameAction.getPlayer(currentPlayer, game).getTime() > 0) {
                    displayTime();
                }
                else {
                    PlayingEntity player = GameAction.getPlayer(currentPlayer, game);
                    player.endMove();
                    game.gameListener.onTurn(player);
                }
            }

            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long calculatePlayerTime(int player) {
        return totalTime - elapsedTime + totalTime - GameAction.getPlayer(player % 2 + 1, game).getTime();
    }

    private void displayTime() {
        long millis = GameAction.getPlayer(game.currentPlayer, game).getTime();
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        game.gameListener.displayTime(minutes, seconds);
    }
}