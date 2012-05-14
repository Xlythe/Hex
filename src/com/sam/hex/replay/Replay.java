package com.sam.hex.replay;

import android.os.Handler;

import com.sam.hex.GameObject;
import com.sam.hex.Global;
import com.sam.hex.HexGame;
import com.sam.hex.lan.LANGlobal;
import com.sam.hex.net.NetGlobal;
import com.sam.hex.net.NetHexGame;

/**
 * @author Will Harmon
 **/
public class Replay implements Runnable {
	private int time;
	private Handler handler;
	private GameObject game;
	private Runnable hideAnnouncementText;
	private Runnable showAnnouncementText;
	private int gameLocation;
	public Replay(int time, Handler handler, Runnable hideAnnouncementText, Runnable showAnnouncementText, GameObject game, int gameLocation){
		this.time = time;
		this.handler = handler;
		this.hideAnnouncementText = hideAnnouncementText;
		this.showAnnouncementText = showAnnouncementText;
		this.game = game;
		this.gameLocation = gameLocation;
	}
	
	@Override
	public void run() {
		HexGame.stopGame(game);
		handler.post(hideAnnouncementText);
		game.moveList.replay(time, game);
		game.board.postInvalidate();
		if(gameLocation==Global.GAME_LOCATION) HexGame.replayRunning = false;
		else if(gameLocation==LANGlobal.GAME_LOCATION){}
		else if(gameLocation==NetGlobal.GAME_LOCATION) NetHexGame.replayRunning = false;
		game.start();
		handler.post(showAnnouncementText);
	}
}
