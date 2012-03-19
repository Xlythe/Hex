package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class MulticastSender implements Runnable {
	Thread thread;
	MulticastSocket socket;
	DatagramPacket packet;
	boolean run = true;

	public MulticastSender(MulticastSocket socket, DatagramPacket packet) {
		this.socket = socket;
		this.packet = packet;
		thread = new Thread(this, "LANshout"); //Create a new thread.
		thread.start(); //Start the thread.
	}
	
	public void run() {
		while(run){
        	try {
        		socket.send(packet);
        		System.out.println("Sending...");
        		for(int i=0;i<10;i++){
        			Thread.sleep(500);
        			if(!run) break;
        		}
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
	}
	
	public void stop() {
		run = false;
	}
}