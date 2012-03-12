package com.sam.hex;

import android.graphics.Point;
import android.os.Looper;
import android.widget.Toast;

public class GameObject implements Runnable {
	Thread theGameRunner;
	private Point hex;
	PlayingEntity player1;
	PlayingEntity player2;
	byte player = 1;
	boolean go=true;

	public GameObject() {
		theGameRunner = new Thread(this, "runningGame"); //Create a new thread.
		System.out.println(theGameRunner.getName());
		
		if(Global.gameType==(byte) 0){
			player1=new PlayerObject((byte)1);
			player2=new PlayerObject((byte)2);
		}
		else if(Global.gameType==(byte) 1){
			player1=new PlayerObject((byte)1);
			player2=new GameAI((byte)2,(byte)1);
		}
		else if(Global.gameType==(byte) 2){
			player1=new GameAI((byte)1,(byte)1);
			player2=new PlayerObject((byte)2);
		}
		else if(Global.gameType==(byte) 3){
			player1=new GameAI((byte)1,(byte)1);
			player2=new GameAI((byte)2,(byte)1);
		}
		else if(Global.gameType==(byte) 4){
			player1=new PlayerObject((byte)1);
			player2=new LocalPlayerObject((byte)2);
		}
		replay();
		
		theGameRunner.start(); //Start the thread.
		
	}
	public void replay(){
		Global.slowAI=false;
		for(int i=0; i<Global.moveList.size(); i++){
			if(Global.moveList.get(i)==null){
				if(player==1) player1.getPlayerTurn();
				else player2.getPlayerTurn();
			}
			else{	
				Global.gamePiece[Global.moveList.get(i).x][Global.moveList.get(i).y].setTeam(player);
			}
			
			if(player==1)
				player=2;
			else
				player=1;
		}
		if(go && (GameAction.checkWinPlayer1() || GameAction.checkWinPlayer2())){
			go=false;
		}
			GameAction.checkedFlagReset();
		hex=null;
		Global.slowAI=true;
		Global.board.postInvalidate();
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
		if (player == 1) {
			boolean success=true;
			if(player1 instanceof PlayerObject){
				success=((PlayerObject)player1).validMove(hex);
				if(success) player1.getPlayerTurn(hex);
			}
			else if(player1 instanceof LocalPlayerObject){
				//TODO Send this move to the other phone
				success=player1.getPlayerTurn(Global.moveList.get(Global.moveList.size()-1))!=new Point(-1,-1);
			}
			else
				player1.getPlayerTurn();
			hex=null;
			if (success && GameAction.checkWinPlayer1()){
				announceWinner(player);
				go=false;
			}
			
			if(success)
				player = 2;
		}
		else {
			boolean success=true;
			if(player2 instanceof PlayerObject){
				success=((PlayerObject)player2).validMove(hex);
				if(success) player2.getPlayerTurn(hex);
			}
			else if(player2 instanceof LocalPlayerObject){
				//TODO Send this move to the other phone
				success=player2.getPlayerTurn(Global.moveList.get(Global.moveList.size()-1))!=new Point(-1,-1);
			}
			else
				player2.getPlayerTurn();
			hex=null;
			if (success && GameAction.checkWinPlayer2()){
				announceWinner(player);
				go=false;
			}
			if(success)
				player = 1;
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
			Toast.makeText(Global.board.getContext(), Global.playerOneName+" wins!", Toast.LENGTH_SHORT).show();
			Looper.loop();
		}
		else{
			Global.board.postInvalidate();
			Looper.prepare();
			Toast.makeText(Global.board.getContext(), Global.playerTwoName+" wins!", Toast.LENGTH_SHORT).show();
			Looper.loop();
		}
	}
}
