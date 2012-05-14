package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.os.Handler;

/**
 * @author Will Harmon
 **/
public class UnicastListener implements Runnable {
	Thread thread;
	boolean run = true;
	DatagramSocket socket;
	Handler handler;
	Runnable challenger;
	Runnable startGame;
	
	public UnicastListener(Handler handler, Runnable challenger, Runnable startGame) {
		try {
			socket = new DatagramSocket(LANGlobal.CHALLENGERPORT);
		} catch (Exception e) {
			run=false;
			e.printStackTrace();
		}
		this.handler = handler;
		this.challenger = challenger;
		this.startGame = startGame;
		thread = new Thread(this, "LANscan"); //Create a new thread.
		thread.start(); //Start the thread.
	}
	
	public void run() {
		//Listen for other players
		byte[] data = new byte[1024];
	    while(run && socket!=null){
	    	try {
	    		DatagramPacket packet = new DatagramPacket(data, data.length);
	    		socket.receive(packet);
	    		String message = new String(data, 0, packet.getLength());
	    		System.out.println(message);
	    		InetAddress address = packet.getAddress();        		
        		
	    		if(message.contains("challenges you. Grid size: ")){
    				LANGlobal.localPlayer.playerName = message.substring(0, message.lastIndexOf(" challenges you"));
    				LANGlobal.localPlayer.gridSize = Integer.decode(message.substring(message.lastIndexOf("Grid size: ")+11));
    				LANGlobal.localPlayer.ip = address;
    				handler.post(challenger);
	    		}
	    		else if(message.contains("Its on! My color is ") && LANGlobal.localPlayer.ip.equals(address)){
	    			//Full message looks like: Its on! My color is _playercolor_
	    			LANGlobal.localPlayer.playerColor = Integer.parseInt(message.substring(20));//Grab the color from the end of the message
	    			
	    			//Send our color over
	    			new LANMessage("My color is "+LANGlobal.playerColor, address, LANGlobal.CHALLENGERPORT);
	    			new LANMessage("My color is "+LANGlobal.playerColor, address, LANGlobal.CHALLENGERPORT);
	    			new LANMessage("My color is "+LANGlobal.playerColor, address, LANGlobal.CHALLENGERPORT);
	    			
	    			handler.post(startGame);
	    			stop();
	    		}
	    		else if(message.contains("My color is ") && LANGlobal.localPlayer.ip.equals(address)){
	    			//Full message looks like: My color is _playercolor_
	    			LANGlobal.localPlayer.playerColor = Integer.parseInt(message.substring(12));//Grab the color from the end of the message
	    			
	    			handler.post(startGame);
	    			stop();
	    		}
	    		else if(message.equals("What's your name?")){
	    			new LANMessage("My name is "+LANGlobal.playerName, address, LANGlobal.CHALLENGERPORT);
	    			new LANMessage("My name is "+LANGlobal.playerName, address, LANGlobal.CHALLENGERPORT);
	    			new LANMessage("My name is "+LANGlobal.playerName, address, LANGlobal.CHALLENGERPORT);
	    		}
	    		else if(message.contains("My name is ")){
	    			//Full message looks like: My name is _playername_
	    			LANGlobal.localPlayer.playerName = message.substring(11);//Grab the name from the end of the message
	    		}
			}
	    	catch (Exception e) {
				System.out.println(e);
			}
	    }
	    if(socket!=null && !socket.isClosed()){
	    	try{
	    		socket.close();
	    	}
	    	catch(Exception e){
	    		e.printStackTrace();
	    	}
	    }
	}
	
	public void stop() {
		run = false;
	}
}