package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.os.Handler;

/**
 * @author Will Harmon
 **/
public class MulticastListener implements Runnable {
	Thread thread;
	boolean run = true;
	MulticastSocket socket;
	Handler handler;
	Runnable updateUI;
	
	public MulticastListener(MulticastSocket socket, Handler handler, Runnable updateUI) {
		this.socket = socket;
		this.handler = handler;
		this.updateUI = updateUI;
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
	        		if(!LANGlobal.localObjects.contains(lno)){
	        			LANGlobal.localObjects.add(lno);
	        			handler.post(updateUI);
	        		}
	    		}
			}
	    	catch (Exception e) {
				System.out.println(e);
			}
	    }
	}
	
	public void stop() {
		run = false;
	}
}