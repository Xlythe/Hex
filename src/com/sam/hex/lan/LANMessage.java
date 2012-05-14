package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Will Harmon
 **/
public class LANMessage implements Runnable{
	String message;
	InetAddress ip;
	int port;
	public Thread thread;
	public LANMessage(String message, InetAddress ip, int port) {
		this.message = message;
		this.ip = ip;
		this.port = port;
		thread = new Thread(this, "messenger"); //Create a new thread.
		thread.start();
	}

	@Override
	public void run() {
		try{
        	DatagramSocket socket = new DatagramSocket();
        	DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), ip, port);
        	socket.send(packet);
        	socket.close();
    	}
    	catch(Exception e){
    		System.out.println(e);
    		e.getStackTrace();
    	}
	}
}