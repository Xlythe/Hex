package com.sam.hex;

import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameObject implements Runnable {
	private boolean game=true;
	public final RegularPolygonGameObject[][] gamePiece;
	public final int gridSize;
	public final boolean swap;
	public int moveNumber;
	public MoveList moveList;
	public int currentPlayer;
	public boolean gameOver=false;
	public PlayingEntity player1;
	public PlayingEntity player2;
	public int player1Type;
	public int player2Type;
	public long moveStart;
	public Thread gameThread;
	public Timer timer;
	public BoardView board;
	public TextView timerText;
	public TextView winnerText;
	public String winnerMsg = "";
	public ImageButton replayForward;
	public ImageButton replayPlayPause;
	public ImageButton replayBack;
	public RelativeLayout replayButtons;
	public ImageButton player1Icon;
	public ImageButton player2Icon;
	public Handler handler;

	public GameObject(int gridSize, boolean swap) {
		gameThread = new Thread(this, "runningGame"); //Create a new thread.
		
		this.gridSize = gridSize;
		gamePiece=new RegularPolygonGameObject[gridSize][gridSize];
		for(int i=0; i<gridSize;i++){
			for(int j=0;j<gridSize;j++){
				gamePiece[i][j] = new RegularPolygonGameObject();
			}
    	}
		this.swap = swap;
		
		moveNumber=1;
		moveList= new MoveList();
		currentPlayer = 1;
		game=true;
		gameOver=false;
	}
	
	public void start(){
		if(gameOver) handler.post(new Runnable(){
			public void run(){
				winnerText.setVisibility(View.GONE);
			}
		});
		gameOver=false;
		game=true;
		timer.start();
		gameThread = new Thread(this, "runningGame");
		gameThread.start();
	}
	
	public void stop(){
		timer.stop();
		player1.quit();
		player2.quit();
		game=false;
		gameOver=true;
		gameThread.setPriority(Thread.MIN_PRIORITY);
	}
	
	public void run() {
		while(game){//Loop the game
			if(!checkForWinner()){
				moveStart = System.currentTimeMillis();
				if(timer.type==1){
					timer.startTime = System.currentTimeMillis();
					GameAction.getPlayer((currentPlayer%2)+1, this).setTime(timer.totalTime);
				}
				GameAction.getPlayer((currentPlayer%2)+1, this).setTime(GameAction.getPlayer((currentPlayer%2)+1, this).getTime() + timer.additionalTime);
				board.postInvalidate();
				GameAction.getPlayer(currentPlayer, this).getPlayerTurn();
			}
			
			currentPlayer=(currentPlayer%2)+1;
		}
		System.out.println("Thread died");
	}
	
	private void announceWinner(int team){
		board.postInvalidate();
		new GameAction.AnnounceWinner(team, this);
	}
	
	private boolean checkForWinner(){
		GameAction.checkedFlagReset(this);
		if(GameAction.checkWinPlayer(1,this)){
			game=false;
			gameOver = true;
			player1.win();
			player2.lose();
			announceWinner(1);
		}
		else if(GameAction.checkWinPlayer(2,this)){
			game=false;
			gameOver = true;
			player1.lose();
			player2.win();
			announceWinner(2);
		}
		
		return gameOver;
	}
	
	public void clearBoard(){
		for(int i=0; i<gridSize;i++){
			for(int j=0;j<gridSize;j++){
				gamePiece[i][j] = new RegularPolygonGameObject();
			}
    	}
		board.postInvalidate();
	}
}