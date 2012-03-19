package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class LANMessage{
	
	public LANMessage(String message, InetAddress ip, int port) {
		try{
        	DatagramSocket socket = new DatagramSocket();
        	DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), LocalLobbyActivity.lno.ip, port);
        	socket.send(packet);
        	socket.close();
    	}
    	catch(Exception e){
    		e.getStackTrace();
    	}
	}
}