package com.sam.hex;

import android.graphics.Point;

public class LocalPlayerObject implements PlayingEntity {
	byte[][] gameBoard; 
	byte team;
	
	public LocalPlayerObject(byte i) {
		this.team=i;//Set the player's team
	}
	
	//Do not use
	public void getPlayerTurn(byte[][] gameBoard) {
		this.gameBoard=gameBoard;
		makeMove();
	}

	public Point getPlayerTurn(Point hex){
		if (hex!=null && Global.gamePiece[hex.x][hex.y].getTeam() == 0) {
			Global.gamePiece[hex.x][hex.y].setTeam((byte) ((team+1)%2));
			Global.moveList.add(hex);
			makeMove();
			return Global.moveList.get(Global.moveList.size()-1);
		}
		else{
			return new Point(-1,-1);
		}
	}
	
	//Do not use
	public void getPlayerTurn() {
		this.gameBoard=BoardTools.teamGrid();
		makeMove();
	}
	
	public void makeMove(){
		GameAction.getPlayerTurn(team);//Have the player make a move
	}
}