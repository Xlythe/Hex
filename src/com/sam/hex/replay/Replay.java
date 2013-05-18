package com.sam.hex.replay;

import android.os.Handler;

import com.sam.hex.Game;
import com.sam.hex.GameAction;

/**
 * @author Will Harmon
 **/
public class Replay implements Runnable {
    private final int time;
    private final Handler handler;
    private final Game game;
    private final Runnable hideAnnouncementText;
    private final Runnable showAnnouncementText;

    public Replay(int time, Handler handler, Runnable hideAnnouncementText, Runnable showAnnouncementText, Game game) {
        this.time = time;
        this.handler = handler;
        this.hideAnnouncementText = hideAnnouncementText;
        this.showAnnouncementText = showAnnouncementText;
        this.game = game;
    }

    @Override
    public void run() {
        handler.post(hideAnnouncementText);
        game.replayRunning = true;
        game.moveList.replay(time, game);
        game.gameListener.onReplay();
        game.replayRunning = false;
        if(game.gameOver) {
            game.currentPlayer = game.currentPlayer % 2 + 1;
            GameAction.getPlayer(game.currentPlayer, game).endMove();
        }
        handler.post(showAnnouncementText);
        game.start();
    }
}
