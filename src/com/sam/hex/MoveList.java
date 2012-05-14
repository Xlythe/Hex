package com.sam.hex;

import java.io.Serializable;

public class MoveList implements Serializable {
	private static final long serialVersionUID = 1L;
	public Move thisMove;
	public MoveList nextMove;
	public MoveList(){
		
	}
	
	public MoveList(int x, int y, byte teamNumber, long time, int moveNumber){
		thisMove= new Move(x,y,teamNumber, time, moveNumber);
	}
	
	public MoveList(MoveList oldMove, int x, int y, byte teamNumber, long time, int moveNumber){
		thisMove= new Move(x,y,teamNumber, time, moveNumber);
		nextMove=oldMove;
	}
	public MoveList(MoveList oldMove, Move thisMove){
		this.thisMove= thisMove;
		nextMove=oldMove;
	}
	public Move getmove(){
		return thisMove;
	}
	/* do not use makeMove might not work with
	 * base cases and is not tested*/
	public void makeMove(int x, int y, byte teamNumber, long time, int moveNumber){
		nextMove=new MoveList(nextMove, thisMove);
		thisMove= new Move(x, y, teamNumber, time, moveNumber);
	}
	//for replays
	public void replay(int time, GameObject game){
		if (thisMove==null) return;
		if (nextMove!=null) nextMove.replay(time, game);
		try {
			if(HexGame.replayRunning) Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		game.gamePiece[thisMove.getX()][thisMove.getY()].setTeam(thisMove.getTeam(),game);
		game.board.postInvalidate();
	}	
}