package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.sam.hex.Global;

import android.graphics.Color;
import android.os.Handler;

public class MulticastListener implements Runnable {
	Thread thread;
	boolean run = true;
	MulticastSocket socket;
	Handler handler;
	Runnable updateUI;
	Runnable challenger;
	Runnable startGame;
	
	public MulticastListener(MulticastSocket socket, Handler handler, Runnable updateUI, Runnable challenger, Runnable startGame) {
		this.socket = socket;
		this.handler = handler;
		this.updateUI = updateUI;
		this.challenger = challenger;
		this.startGame = startGame;
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
	    		System.out.println(message);
	    		InetAddress address = packet.getAddress();        		
        		
	    		if(message.contains("Let's play Hex.")){
	    			//Full message looks like: Let's play Hex. I'm _playername_
	    			String name = message.substring(20);//Grab the name from the end of the message
	        		LocalNetworkObject lno = new LocalNetworkObject(name,address);
	        		if(!Global.localObjects.contains(lno)){
	        			Global.localObjects.add(lno);
	        		}
	        		handler.post(updateUI);
	    		}
	    		else if(message.contains("challenges you. Grid size: ")){
	    			boolean flag = true;
	    			for(int i=0;i<Global.localObjects.size();i++){
	        			if(Global.localObjects.get(i).ip.equals(address)){
	        				flag = false;
	        				LocalLobbyActivity.lno = Global.localObjects.get(i);
	        				LocalLobbyActivity.lno.firstMove = true;
	        				//Full message looks like: _playername_ challenges you. Grid size: _gridsize_
	        				LocalLobbyActivity.lno.gridSize = Integer.decode(message.substring(message.lastIndexOf("Grid size: ")+11));//Grab the grid size from the end of the message
	        				handler.post(challenger);
	        				break;
	        			}
	        		}
	    			if(flag){
	    				LocalLobbyActivity.lno.playerName = message.substring(0, message.lastIndexOf(" challenges you"));
	    				LocalLobbyActivity.lno.firstMove = true;
	    				LocalLobbyActivity.lno.gridSize = Integer.decode(message.substring(message.lastIndexOf("Grid size: ")+11));
	    				handler.post(challenger);
	    			}
	    		}
	    		else if(message.contains("It's on! My color's ") && LocalLobbyActivity.lno.ip.equals(address)){
	    			//Full message looks like: It's on! My color's _playercolor_
	    			LocalLobbyActivity.lno.playerColor = Integer.decode(message.substring(20));//Grab the color from the end of the message
	    			
	    			//Send our color over
	    			new LANMessage("My color is "+Global.player1Color, LocalLobbyActivity.lno.ip, 4080);
	    			
	    			handler.post(startGame);
    				break;
	    		}
	    		else if(message.contains("My color is ") && LocalLobbyActivity.lno.ip.equals(address)){
	    			//Full message looks like: My color is _playercolor_
	    			Global.localPlayer.playerColor = Color.parseColor(message.substring(12));//Grab the color from the end of the message
	    			
	    			handler.post(startGame);
	    			break;
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
}