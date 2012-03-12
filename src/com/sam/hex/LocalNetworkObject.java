package com.sam.hex;

public class LocalNetworkObject{
	String playerName;
	String ip;
	
	public LocalNetworkObject(String name, String ip) {
		this.playerName = name;
		this.ip = ip;
	}
	
	public String toString(){
		return playerName;
	}
	
	public String getIP(){
		return ip;
	}
}