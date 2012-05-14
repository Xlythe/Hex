package com.sam.hex.lan;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author Will Harmon
 **/
public class LANReciever implements Runnable{
	String message;
	InetAddress ip;
	int port;
	MessageRunnable run;
	public Thread thread;
	public LANReciever(String message, InetAddress ip, int port, MessageRunnable run) {
		this.message = message;
		this.ip = ip;
		this.port = port;
		this.run = run;
		
		thread = new Thread(this, "reciever"); //Create a new thread.
		thread.start();
	}

	@Override
	public void run() {
		byte[] data = new byte[1024];
		try {
			DatagramSocket socket = new DatagramSocket(port);
			DatagramPacket packet = new DatagramPacket(data, data.length);
			socket.receive(packet);
			String packetMessage = new String(data, 0, packet.getLength());
			System.out.println(packetMessage);
			while(!packetMessage.contains(message) || !packet.getAddress().equals(ip)){
				socket.receive(packet);
			}
			run.message = packetMessage;
			run.run();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}