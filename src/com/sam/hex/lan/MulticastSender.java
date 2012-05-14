package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * @author Will Harmon
 **/
public class MulticastSender implements Runnable {
	Thread thread;
	MulticastSocket socket;
	DatagramPacket packet;
	boolean run = true;

	public MulticastSender(MulticastSocket socket) {
		this.socket = socket;
		

		//Create a packet
		String message = ("Let's play Hex. I'm "+LANGlobal.playerName);
		try {
			packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(LANGlobal.MULTICASTADDRESS), LANGlobal.MULTICASTPORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		thread = new Thread(this, "LANshout"); //Create a new thread.
		thread.start(); //Start the thread.
	}
	
	public void run() {
		while(run){
        	try {
        		socket.send(packet);
        		System.out.println("Sending...");
        		Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
	}
	
	public void stop() {
		run = false;
	}
}