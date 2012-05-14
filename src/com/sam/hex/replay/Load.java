package com.sam.hex.replay;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import com.sam.hex.GameObject;
import com.sam.hex.Global;
import com.sam.hex.HexGame;
import com.sam.hex.MoveList;
import com.sam.hex.PlayerObject;
import com.sam.hex.Timer;

/**
 * @author Will Harmon
 **/
public class Load implements Runnable{
	private File file;
	public Load(File file){
		this.file = file;
	}
	@Override
	public void run() {
		try {
			HexGame.stopGame(Global.game);
    		if(file!=null){
	            //Construct the ObjectInputStream object
	        	ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));

				int gridSize = (Integer) inputStream.readObject();
				boolean swap = (Boolean) inputStream.readObject();

				Global.game = new GameObject(gridSize, swap);
				
				Global.game.player1=new PlayerObject((byte)1,Global.game);
				Global.game.player2=new PlayerObject((byte)2,Global.game);
	        	
				Global.game.player1Type = (Integer) inputStream.readObject();
	        	Global.game.player2Type = (Integer) inputStream.readObject();
	        	Global.game.player1.setColor((Integer) inputStream.readObject());
	            Global.game.player2.setColor((Integer) inputStream.readObject());
				Global.game.player1.setName((String) inputStream.readObject());
				Global.game.player2.setName((String) inputStream.readObject());
				Global.game.moveList = (MoveList) inputStream.readObject();
				Global.game.moveNumber = (Integer) inputStream.readObject();
				int timertype = (Integer) inputStream.readObject();
				long timerlength = (Long) inputStream.readObject();
				Global.game.timer = new Timer(Global.game, timerlength, 0, timertype);
				
				inputStream.close();
				
				Global.game.currentPlayer=(Global.game.moveNumber%2)+1;
				HexGame.replay = true;
				HexGame.replayRunning = false;
				HexGame.startNewGame = false;
				
				//Does not support saving PlayingEntities yet
				Global.game.player1Type = 0;
				Global.game.player2Type = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}