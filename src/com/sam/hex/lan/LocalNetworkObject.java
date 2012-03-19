package com.sam.hex.lan;

import java.net.InetAddress;

import com.sam.hex.Global;

public class LocalNetworkObject{
	public String playerName;
	public int playerColor=Global.player2DefaultColor;
	public int gridSize = 0;
	public InetAddress ip;
	public boolean firstMove = false;
	
	public LocalNetworkObject(String name, InetAddress ip) {
		this.playerName = name;
		this.ip = ip;
	}
	
	public String toString(){
		return playerName;
	}
	
	public boolean equals(Object e){
		if(e instanceof LocalNetworkObject){
			return this.playerName.equals(((LocalNetworkObject) e).playerName) && this.ip.equals(((LocalNetworkObject) e).ip);
		}
		else{
			return false;
		}
	}
	
	public String getIP(){
		return ip.getHostName();
	}
}