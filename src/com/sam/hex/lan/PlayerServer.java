//package com.sam.hex.lan;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//import android.content.DialogInterface;
//import android.content.SharedPreferences;
//import android.graphics.Point;
//import android.preference.PreferenceManager;
//
//import com.sam.hex.GameAction;
//import com.sam.hex.Global;
//import com.sam.hex.HexGame;
//import com.sam.hex.R;
//

/**
 * @author Will Harmon
 **/
//public class PlayerServer implements PlayerUnicastListener {
//	Thread thread;
//	boolean run = true;
//	ServerSocket serverSocket;
//	Socket clientSocket;
//	PrintWriter out;
//	BufferedReader in;
//	SharedPreferences prefs;
//	int team;
//	
//	public PlayerServer(int team) {
//		this.team = team;
//		prefs = PreferenceManager.getDefaultSharedPreferences(Global.board.getContext());
//		thread = new Thread(this, "LANscan"); //Create a new thread.
//		thread.start(); //Start the thread.
//	}
//	
//	public void run() {
//		try {
//			serverSocket = new ServerSocket(LANGlobal.PLAYERPORT);
//			do{
//				clientSocket = serverSocket.accept();
//			}
//			while(clientSocket==null || !clientSocket.getInetAddress().equals(LANGlobal.localPlayer.ip));
//			
//			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//			out = new PrintWriter(clientSocket.getOutputStream(), true);
//		} catch (IOException e) {
//			run = false;
//			e.printStackTrace();
//		}
//		
//	    while(run){
//	    	try{
//	    		String message = in.readLine();
//	    		if(message==null) continue;
//	            System.out.println(message);
//	            
//	            if(message.contains("Move: ")){
//	    			int x = Integer.decode(message.substring(6,message.indexOf(",")));
//					int y = Integer.decode(message.substring(message.indexOf(",")+1));
//					LANGlobal.hex = new Point(x,y);
//	    		}
//	    		else if(message.contains("I changed my color to ")){
//	    			//Full message looks like: I changed my color to _color_
//	    			LANGlobal.localPlayer.playerColor = Integer.decode(message.substring(22));
//	    			HexGame.setColors(prefs);
//	    			Global.board.postInvalidate();
//	    		}
//	    		else if(message.contains("I changed my name to ")){
//	    			//Full message looks like: I changed my name to _name_
//	    			LANGlobal.localPlayer.playerName = message.substring(21);
//	    			HexGame.setNames(prefs);
//	    			Global.board.postInvalidate();
//	    		}
//	    		else if(message.equals("I win!")){
//	    			stop();
//	    		}
//	    		else if(message.equals("Cheater.")){
//	    			
//	    		}
//	    		else if(message.equals("Want to play a new game?")){
//	    			new DialogBox(Global.board.getContext(), 
//	    					LANGlobal.localPlayer.playerName+" "+Global.board.getContext().getString(R.string.newLANGame), 
//	    					new DialogInterface.OnClickListener() {
//	    	    	    	    public void onClick(DialogInterface dialog, int which) {
//	    	    	    	        switch (which){
//	    	    	    	        case DialogInterface.BUTTON_POSITIVE:
//	    	    	    	            //Yes button clicked
//	    	    	    	        	HexGame.startNewGame=true;
//	    	    	    	        	Global.board.postInvalidate();
//	    	    	    	        	out.println("Sure, let's play again");
//	    	    	    	            break;
//	    	    	    	        case DialogInterface.BUTTON_NEGATIVE:
//	    	    	    	            //No button clicked
//	    	    	    	        	out.println("No, I don't want to play again");
//	    	    	    	            break;
//	    	    	    	        }
//	    	    	    	    }
//	    	    	    	}, 
//	    	    	    	Global.board.getContext().getString(R.string.yes), 
//	    	    	    	Global.board.getContext().getString(R.string.no));
//	    		}
//	    		else if(message.equals("Sure, let's play again")){
//	    			HexGame.startNewGame=true;
//	    			Global.board.postInvalidate();
//	    			new DialogBox(Global.board.getContext(), 
//	    					Global.board.getContext().getString(R.string.LANplayAgain), 
//	    					null, 
//	    					Global.board.getContext().getString(R.string.okay));
//	    	    }
//	    		else if(message.equals("No, I don't want to play again")){
//	    			new DialogBox(Global.board.getContext(), 
//	    					Global.board.getContext().getString(R.string.LANdontPlayAgain), 
//	    					null, 
//	    					Global.board.getContext().getString(R.string.okay));
//	    		}
//	    		else if(message.equals("Can I undo?")){
//	    			new DialogBox(Global.board.getContext(), 
//	    					LANGlobal.localPlayer.playerName+" "+Global.board.getContext().getString(R.string.LANUndo), 
//	    					new DialogInterface.OnClickListener() {
//	    	    	    	    public void onClick(DialogInterface dialog, int which) {
//	    	    	    	        switch (which){
//	    	    	    	        case DialogInterface.BUTTON_POSITIVE:
//	    	    	    	            //Yes button clicked
//	    	    	    	        	GameAction.undo();
//	    	    	    	        	Global.board.postInvalidate();
//	    	    	    	        	out.println("Sure, undo");
//	    	    	    	            break;
//	    	    	    	        case DialogInterface.BUTTON_NEGATIVE:
//	    	    	    	            //No button clicked
//	    	    	    	        	out.println("No, you cannot undo");
//	    	    	    	            break;
//	    	    	    	        }
//	    	    	    	    }
//	    	    	    	}, 
//	    					Global.board.getContext().getString(R.string.yes), 
//	    					Global.board.getContext().getString(R.string.no));
//	    		}
//	    		else if(message.equals("Sure, undo")){
//	    			GameAction.undo();
//	    			Global.board.postInvalidate();
//	    			new DialogBox(Global.board.getContext(), 
//	    					Global.board.getContext().getString(R.string.LANundoAccepted), 
//	    					null, 
//	    					Global.board.getContext().getString(R.string.okay));
//	    		}
//	    		else if(message.equals("No, you cannot undo")){
//	    			new DialogBox(Global.board.getContext(), 
//	    					Global.board.getContext().getString(R.string.LANundoDenied), 
//	    					null, 
//	    					Global.board.getContext().getString(R.string.okay));
//	    		}
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//	    }
//	    
//	    try {
//	    	if(out!=null) out.close();
//	    	if(in!=null) in.close();
//	    	if(clientSocket!=null) clientSocket.close();
//	    	if(serverSocket!=null) serverSocket.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	    
//	}
//	
//	public void quickMessage(final String message){
//		new Thread(new Runnable(){
//			@Override
//			public void run() {
//				if(out!=null) out.println(message);
//			}}, "message").start();
//	}
//	
//	public void stop() {
//		run = false;
//	}
//}