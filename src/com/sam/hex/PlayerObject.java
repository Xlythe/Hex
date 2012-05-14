package com.sam.hex;

import java.util.LinkedList;

import com.sam.hex.net.MoveListener;

import android.graphics.Point;

public class PlayerObject implements PlayingEntity {
	private String name;
	private int color;
	private long timeLeft;
	private int team;
	private LinkedList<Point> hex = new LinkedList<Point>();
	private GameObject game;
	
	public PlayerObject(int team, GameObject game) {
		this.team=team;
		this.game=game;
	}
	
	@Override
	public void getPlayerTurn() {
		while (true) {
			while (hex.size()==0) {
				try {
					Thread.sleep(80);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (hex.get(0).equals(new Point(-1,-1))){
				hex.remove(0);
				break;
			}
			if (GameAction.makeMove(this, (byte) team, hex.get(0), game)) {
				hex.remove(0);
				break;
			}
			hex.remove(0);
		}
	}
	
	@Override
	public void undoCalled(){
	}
	
	@Override
	public void newgameCalled() {
		endMove();
	}

	@Override
	public boolean supportsUndo() {
		return true;
	}

	@Override
	public boolean supportsNewgame() {
		return true;
	}

	@Override
	public void quit() {
		endMove();
	}

	@Override
	public void win() {
	}

	@Override
	public void lose() {
	}

	@Override
	public boolean supportsSave() {
		return false;
	}

	@Override
	public void endMove() {
		hex.add(new Point(-1,-1));
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public void setTime(long time) {
		this.timeLeft = time;
	}

	@Override
	public long getTime() {
		return timeLeft;
	}

	@Override
	public void setMove(Object o, Point hex) {
		if(o instanceof GameAction && game.currentPlayer==team){
			this.hex = new LinkedList<Point>();
			this.hex.add(hex);
		}
		else if(o instanceof MoveListener) this.hex.add(hex);
	}

	@Override
	public boolean giveUp() {
		return false;
	}
}