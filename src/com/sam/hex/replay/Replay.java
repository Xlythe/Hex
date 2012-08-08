package com.sam.hex.replay;

import android.os.Handler;

import com.sam.hex.GameAction;
import com.sam.hex.GameObject;

/**
 * @author Will Harmon
 **/
public class Replay implements Runnable {
	private int time;
	private Handler handler;
	private GameObject game;
	private Runnable hideAnnouncementText;
	private Runnable showAnnouncementText;
	public Replay(int time, Handler handler, Runnable hideAnnouncementText, Runnable showAnnouncementText, GameObject game){
		this.time = time;
		this.handler = handler;
		this.hideAnnouncementText = hideAnnouncementText;
		this.showAnnouncementText = showAnnouncementText;
		this.game = game;
	}
	
	@Override
	public void run() {
		handler.post(hideAnnouncementText);
		game.views.board.replayRunning = true;
		game.moveList.replay(time, game);
		game.views.board.postInvalidate();
		game.views.board.replayRunning = false;
		if(game.gameOver){
			game.currentPlayer = game.currentPlayer%2+1;
			GameAction.getPlayer(game.currentPlayer, game).endMove();
		}
		handler.post(showAnnouncementText);
		game.start();
	}
}
