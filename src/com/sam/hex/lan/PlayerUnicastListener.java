package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.preference.PreferenceManager;

import com.sam.hex.DialogBox;
import com.sam.hex.GameAction;
import com.sam.hex.GameObject;
import com.sam.hex.Global;
import com.sam.hex.HexGame;
import com.sam.hex.MoveList;
import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class PlayerUnicastListener implements Runnable {
	private Thread thread;
	private boolean run = true;
	private DatagramSocket socket;
	SharedPreferences prefs;
	int team;
	private GameObject game;
	
	public PlayerUnicastListener(int team, GameObject game) {
		this.team = team;
		this.game = game;
		try {
			this.socket = new DatagramSocket(LANGlobal.PLAYERPORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		prefs = PreferenceManager.getDefaultSharedPreferences(game.board.getContext());
		thread = new Thread(this, "LANscan"); //Create a new thread.
		thread.start(); //Start the thread.
	}
	
	public void run() {
		//Listen for other players
		byte[] data = new byte[1024];
	    while(run)
	    {
	    	try {
	    		DatagramPacket packet = new DatagramPacket(data, data.length);
	    		socket.receive(packet);
	    		String message = new String(data, 0, packet.getLength());
	    		InetAddress address = packet.getAddress();
        		
	    		if(!LANGlobal.localPlayer.ip.equals(address)) continue;
	    		
	    		System.out.println(message);
	    		if(message.contains("Move: ")){
	    			int x = Integer.decode(message.substring(6,message.indexOf(",")));
					int y = Integer.decode(message.substring(message.indexOf(",")+1));
					GameAction.getPlayer(game.currentPlayer,LANGlobal.game).setMove(this, new Point(x,y));
	    		}
	    		else if(message.contains("I changed my color to ")){
	    			//Full message looks like: I changed my color to _color_
	    			LANGlobal.localPlayer.playerColor = Integer.decode(message.substring(22));
//	    			HexGame.setColors(LANGlobal.gameLocation, LANGlobal.game);
	    			game.moveList.replay(0,game);
	    			game.board.postInvalidate();
	    		}
	    		else if(message.contains("I changed my name to ")){
	    			//Full message looks like: I changed my name to _name_
	    			LANGlobal.localPlayer.playerName = message.substring(21);
//	    			HexGame.setNames(LANGlobal.gameLocation, LANGlobal.game);
	    			game.board.postInvalidate();
	    		}
	    		else if(message.equals("Quitting")){
	    			HexGame.stopGame(LANGlobal.game);
	    			if(!game.gameOver)
		    			new DialogBox(game.board.getContext(), 
		    					game.board.getContext().getString(R.string.playerQuit), 
		    					null, 
		    	    	    	game.board.getContext().getString(R.string.okay));
	    		}
	    		else if(message.equals("Want to play a new game?")){
	    			new DialogBox(game.board.getContext(), 
	    					GameAction.insert(game.board.getContext().getString(R.string.newLANGame), LANGlobal.localPlayer.playerName), 
	    					new DialogInterface.OnClickListener() {
	    	    	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	    	        switch (which){
	    	    	    	        case DialogInterface.BUTTON_POSITIVE:
	    	    	    	            //Yes button clicked
	    	    	    	        	new LANMessage("Sure, let's play again", LANGlobal.localPlayer.ip, LANGlobal.PLAYERPORT);
	    	    	    	        	initalizeNewGame();
	    	    	    	            break;
	    	    	    	        case DialogInterface.BUTTON_NEGATIVE:
	    	    	    	            //No button clicked
	    	    	    	        	new LANMessage("No, I don't want to play again", LANGlobal.localPlayer.ip, LANGlobal.PLAYERPORT);
	    	    	    	            break;
	    	    	    	        }
	    	    	    	    }
	    	    	    	}, 
	    	    	    	game.board.getContext().getString(R.string.yes), 
	    	    	    	game.board.getContext().getString(R.string.no));
	    		}
	    		else if(message.equals("Sure, let's play again")){
	    			initalizeNewGame();
	    			new DialogBox(game.board.getContext(), 
	    					game.board.getContext().getString(R.string.LANplayAgain), 
	    					null, 
	    					game.board.getContext().getString(R.string.okay));
	    	    }
	    		else if(message.equals("No, I don't want to play again")){
	    			new DialogBox(game.board.getContext(), 
	    					game.board.getContext().getString(R.string.LANdontPlayAgain), 
	    					null, 
	    					game.board.getContext().getString(R.string.okay));
	    		}
	    		else if(message.equals("Can I undo?")){
	    			new DialogBox(game.board.getContext(), 
	    					GameAction.insert(game.board.getContext().getString(R.string.LANUndo), LANGlobal.localPlayer.playerName), 
	    					new DialogInterface.OnClickListener() {
	    	    	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	    	        switch (which){
	    	    	    	        case DialogInterface.BUTTON_POSITIVE:
	    	    	    	            //Yes button clicked
	    	    		    			LANGlobal.undoRequested = true;
	    	    	    	        	GameAction.undo(LANGlobal.GAME_LOCATION,LANGlobal.game);
	    	    	    	        	new LANMessage("Sure, undo"+LANGlobal.undoNumber, LANGlobal.localPlayer.ip, LANGlobal.PLAYERPORT);
	    	    	    	        	new LANMessage("Sure, undo"+LANGlobal.undoNumber, LANGlobal.localPlayer.ip, LANGlobal.PLAYERPORT);
	    	    	    	        	new LANMessage("Sure, undo"+LANGlobal.undoNumber, LANGlobal.localPlayer.ip, LANGlobal.PLAYERPORT);
	    	    	    	        	LANGlobal.undoNumber++;
	    	    	    	            break;
	    	    	    	        case DialogInterface.BUTTON_NEGATIVE:
	    	    	    	            //No button clicked
	    	    	    	        	new LANMessage("No, you cannot undo", LANGlobal.localPlayer.ip, LANGlobal.PLAYERPORT);
	    	    	    	            break;
	    	    	    	        }
	    	    	    	    }
	    	    	    	}, 
	    					game.board.getContext().getString(R.string.yes), 
	    					game.board.getContext().getString(R.string.no));
	    		}
	    		else if(message.contains("Sure, undo")){
	    			int num = Integer.parseInt(message.substring(10));
	    			if(num==LANGlobal.undoNumber){
	    				LANGlobal.undoNumber++;
	    				GameAction.undo(LANGlobal.GAME_LOCATION,LANGlobal.game);
	    				new DialogBox(game.board.getContext(), 
		    					game.board.getContext().getString(R.string.LANundoAccepted), 
		    					null, 
		    					game.board.getContext().getString(R.string.okay));
	    			}
	    		}
	    		else if(message.equals("No, you cannot undo")){
	    			new DialogBox(game.board.getContext(), 
	    					game.board.getContext().getString(R.string.LANundoDenied), 
	    					null, 
	    					game.board.getContext().getString(R.string.okay));
	    		}
			}
	    	catch (Exception e) {
				e.printStackTrace();
			}
	    }

	}
	
	public void stop() {
		run = false;
	}
	
	private void initalizeNewGame(){
		if(game.gameOver){
			game.start();
			
			//Make sure defaults are set
	    	game.moveList=new MoveList();
	    	game.currentPlayer = 1;
	    	game.moveNumber = 1;
	    	
	    	//Clear the board
	    	for(int x=0;x<game.gridSize;x++){
				for(int y=0;y<game.gridSize;y++){
					game.gamePiece[x][y].setColor(Color.WHITE);
					game.gamePiece[x][y].setTeam((byte)0,game);
				}
			}
    	}
		else{
			int turn = game.currentPlayer;
			if(turn==2){
	    		GameAction.getPlayer(game.currentPlayer,LANGlobal.game).endMove();
	    		while(turn == game.currentPlayer){
					try {
						Thread.sleep(80);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			//Make sure defaults are set
	    	game.moveList=new MoveList();
	    	game.currentPlayer = 1;
	    	game.moveNumber = 1;
	    	
	    	//Clear the board
	    	for(int x=0;x<game.gridSize;x++){
				for(int y=0;y<game.gridSize;y++){
					game.gamePiece[x][y].setColor(Color.WHITE);
					game.gamePiece[x][y].setTeam((byte)0,game);
				}
			}
		}
		
    	game.board.postInvalidate();
	}
}