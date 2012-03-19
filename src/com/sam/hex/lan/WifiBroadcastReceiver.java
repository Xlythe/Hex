package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import com.sam.hex.Global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class WifiBroadcastReceiver extends BroadcastReceiver{
	Handler handler;
	Runnable updateResults;
	Runnable challenger;
	Runnable startGame;
	MulticastListener listener;
	MulticastSender sender;
	WifiManager wm;
	
	public WifiBroadcastReceiver(Handler handler, Runnable updateResults, Runnable challenger, Runnable startGame, MulticastListener listener, MulticastSender sender, WifiManager wm) {
		this.handler = handler;
		this.updateResults = updateResults;
		this.challenger = challenger;
		this.startGame = startGame;
		this.listener = listener;
		this.sender = sender;
		this.wm = wm;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
	    if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
	        if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
	        	//Clear our cached players from the network
	            Global.localObjects = new ArrayList<LocalNetworkObject>();
	        	handler.post(updateResults);
	        	
	        	//Get our new ip address
	            WifiInfo wifiInfo = wm.getConnectionInfo();
	            Global.LANipAddress = String.format("%d.%d.%d.%d",(wifiInfo.getIpAddress() & 0xff),(wifiInfo.getIpAddress() >> 8 & 0xff),(wifiInfo.getIpAddress() >> 16 & 0xff),(wifiInfo.getIpAddress() >> 24 & 0xff));
	            
	        	try {
	        		//Kill previous threads
	        		if(sender!=null){
	        			sender.stop();
	        			listener.stop();
	        			Thread.sleep(600);
	        		}
	        		
					//Create a socket
					InetAddress address = InetAddress.getByName("234.235.236.237");
					int port = 4080;
					MulticastSocket socket = new MulticastSocket(port);
					socket.joinGroup(address);
					
					//Create a packet
					String message = ("Let's play Hex. I'm "+Global.player1Name);
					DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, port);
					
					//Start sending
					sender=new MulticastSender(socket,packet);
					//Start listening
			        listener=new MulticastListener(socket, handler, updateResults, challenger, startGame);
				}
				catch (Exception e) {
					System.out.println(e);
				}
	        }
	        else {
	            //Wifi connection was lost
	        	//Clear our cached players from the network
	            Global.localObjects = new ArrayList<LocalNetworkObject>();
	        	handler.post(updateResults);
	        }
	    }
	}
}