package com.sam.hex;

import com.sam.hex.lan.LocalPlayerObject;

import android.graphics.Point;
import android.os.Looper;
import android.widget.Toast;

public class GameObject implements Runnable {
	Thread theGameRunner;
	private Point hex;
	boolean go=true;

	public GameObject() {
		theGameRunner = new Thread(this, "runningGame"); //Create a new thread.
		System.out.println(theGameRunner.getName());
		
//		//Decide who goes first
//		Global.currentPlayer = (byte) (Math.random()*2+1);
//		if(Global.currentPlayer==(byte)1){
//			Toast.makeText(Global.board.getContext(), Global.player1Name+" starts.", Toast.LENGTH_SHORT).show();
//		}
//		else if(Global.currentPlayer==(byte)2){
//			Toast.makeText(Global.board.getContext(), Global.player2Name+" starts.", Toast.LENGTH_SHORT).show();
//		}
		
		theGameRunner.start(); //Start the thread.
	}
	
	public void stop(){
		//theGameRunner.stop();
		go=false;
	}
	
	public void setPiece(Point h){
		hex=h;
	}
	
	public void run() {
		//Loop the game
		while(go){
			GameAction.checkedFlagReset();
			if(go) doStuff();
			Global.board.postInvalidate();
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void doStuff(){
		if (Global.currentPlayer == 1) {
			boolean success=true;
			if(Global.player1 instanceof PlayerObject){
				success=((PlayerObject)Global.player1).validMove(hex);
				if(success) Global.player1.getPlayerTurn(hex);
			}
			else if(Global.player1 instanceof LocalPlayerObject){
				//TODO Send this move to the other phone
				success=Global.player1.getPlayerTurn(Global.moveList.get(Global.moveList.size()-1))!=new Point(-1,-1);
			}
			else
				Global.player1.getPlayerTurn();
			hex=null;
			if (success && GameAction.checkWinPlayer1()){
				announceWinner(Global.currentPlayer);
				go=false;
			}
			
			if(success)
				Global.currentPlayer = 2;
		}
		else {
			boolean success=true;
			if(Global.player2 instanceof PlayerObject){
				success=((PlayerObject)Global.player2).validMove(hex);
				if(success) Global.player2.getPlayerTurn(hex);
			}
			else if(Global.player2 instanceof LocalPlayerObject){
				//TODO Send this move to the other phone
				success=Global.player2.getPlayerTurn(Global.moveList.get(Global.moveList.size()-1))!=new Point(-1,-1);
			}
			else
				Global.player2.getPlayerTurn();
			hex=null;
			if (success && GameAction.checkWinPlayer2()){
				announceWinner(Global.currentPlayer);
				go=false;
			}
			if(success)
				Global.currentPlayer = 1;
		}
	}
	
	private void announceWinner(byte team){
		Global.board.postInvalidate();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(team==(byte)1){
			Looper.prepare();
			Toast.makeText(Global.board.getContext(), Global.player1Name+" wins!", Toast.LENGTH_SHORT).show();
			Looper.loop();
		}
		else{
			Looper.prepare();
			Toast.makeText(Global.board.getContext(), Global.player2Name+" wins!", Toast.LENGTH_SHORT).show();
			Looper.loop();
		}
	}
}
