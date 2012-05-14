package com.sam.hex.lan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

/**
 * @author Will Harmon
 **/
public class WifiBroadcastReceiver extends BroadcastReceiver{
	Handler handler;
	Runnable updateResults;
	Runnable challenger;
	Runnable startGame;
	MulticastListener multicastListener;
	UnicastListener unicastListener;
	MulticastSender sender;
	WifiManager wm;
	
	public WifiBroadcastReceiver(Handler handler, Runnable updateResults, Runnable challenger, Runnable startGame, MulticastListener multicastListener, UnicastListener unicastListener, MulticastSender sender, WifiManager wm) {
		this.handler = handler;
		this.updateResults = updateResults;
		this.challenger = challenger;
		this.startGame = startGame;
		this.multicastListener = multicastListener;
		this.unicastListener = unicastListener;
		this.sender = sender;
		this.wm = wm;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
	    if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
	        if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
	        	//Clear our cached players from the network
	            LANGlobal.localObjects.clear();
	        	handler.post(updateResults);
	        	
	        	//Get our new ip address
	            WifiInfo wifiInfo = wm.getConnectionInfo();
	            LANGlobal.LANipAddress = String.format("%d.%d.%d.%d",(wifiInfo.getIpAddress() & 0xff),(wifiInfo.getIpAddress() >> 8 & 0xff),(wifiInfo.getIpAddress() >> 16 & 0xff),(wifiInfo.getIpAddress() >> 24 & 0xff));
	        }
	        else {
	            //Wifi connection was lost
	        	//Clear our cached players from the network
	            LANGlobal.localObjects.clear();
	        	handler.post(updateResults);
	        }
	    }
	}
}