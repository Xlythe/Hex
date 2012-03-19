package com.sam.hex;

import android.graphics.Point;

public class PlayerObject implements PlayingEntity {
	
	byte[][] gameBoard; 
	byte team;
	
	public PlayerObject(byte i) {
	this.team=i;//Set the player's team
	}

	
	public void getPlayerTurn(byte[][] gameBoard) {
		 this.gameBoard=gameBoard;
		 makeMove();
	}
	
	public Point getPlayerTurn(Point hex){
		if (validMove(hex)) {
			Global.gamePiece[hex.x][hex.y].setTeam(team);
			Global.moveList.add(hex);
			return new Point(-1,-1);
		}
		return new Point(-1,-1);
	}
	
	public boolean validMove(Point hex){
		return hex!=null && Global.gamePiece[hex.x][hex.y].getTeam() == 0;
	}
	
	public void getPlayerTurn() {
		this.gameBoard=BoardTools.teamGrid();
		makeMove();
	}
	
	public void makeMove(){
		GameAction.getPlayerTurn(team);//Have the player make a move
	}
	
	public void undo(Point hex){
		Global.gamePiece[hex.x][hex.y].setTeam((byte)0);
	}
}