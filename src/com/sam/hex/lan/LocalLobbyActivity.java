package com.sam.hex.lan;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.sam.hex.Global;
import com.sam.hex.HexGame;
import com.sam.hex.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class LocalLobbyActivity extends Activity {
	WifiManager wm;
	MulticastLock mcLock;
	WifiBroadcastReceiver broadcastReceiver;
	IntentFilter intentFilter;
	MulticastListener listener;
	MulticastSender sender;
	MulticastSocket socket;
	public static LocalNetworkObject lno = new LocalNetworkObject("", null);
	private List<LocalNetworkObject> players = new ArrayList<LocalNetworkObject>();
    final Handler handler = new Handler();
    final Runnable updateResults = new Runnable() {
        public void run() {
        	if(players!=Global.localObjects){
        		players = Global.localObjects;
        		updateResultsInUi();
        	}
        }
    };
    final Runnable challenger = new Runnable() {
        public void run() {
        	challengeRecieved();
        }
    };
    final Runnable startGame = new Runnable() {
        public void run() {
        	Global.localPlayer = LocalLobbyActivity.lno;
        	HexGame.startNewGame = true;
        	startActivity(new Intent(getBaseContext(),HexGame.class));
        	finish();
        }
    };
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locallobby);
        
        wm = (WifiManager) getSystemService(WIFI_SERVICE);
        mcLock = wm.createMulticastLock("broadcastlock");
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        broadcastReceiver = new WifiBroadcastReceiver(handler, updateResults, challenger, startGame, listener, sender, wm);
        
        final Button ipButton = (Button) findViewById(R.id.customIP);
        ipButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	customIP();
            }
        });
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//Load preferences
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    	//Set player's name, color, grid size
    	Global.player1Name = prefs.getString("player1Name", "Player1");
    	Global.player1Color = prefs.getInt("player1Color", Global.player1DefaultColor);
    	Global.gridSize=Integer.decode(prefs.getString("gameSizePref", "7"));
    	if(Global.gridSize==0) Global.gridSize=Integer.decode(prefs.getString("customGameSizePref", "7"));
    	if(Global.gridSize<=0) Global.gridSize=1;
    	
        if (!wm.isWifiEnabled()) {
        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int which) {
        	        switch (which){
        	        case DialogInterface.BUTTON_POSITIVE:
        	            //Yes button clicked
        	        	wm.setWifiEnabled(true);
        	            break;
        	        case DialogInterface.BUTTON_NEGATIVE:
        	            //No button clicked
        	        	android.os.Process.killProcess(android.os.Process.myPid());
        	            break;
        	        }
        	    }
        	};

        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Wifi is off. Enable?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
        }
        
        //Allow for broadcasts
        mcLock.acquire();
        
        //Get our ip address
        WifiInfo wifiInfo = wm.getConnectionInfo();
        Global.LANipAddress = String.format("%d.%d.%d.%d",(wifiInfo.getIpAddress() & 0xff),(wifiInfo.getIpAddress() >> 8 & 0xff),(wifiInfo.getIpAddress() >> 16 & 0xff),(wifiInfo.getIpAddress() >> 24 & 0xff));
        
        try {
			//Create a socket
			InetAddress address = InetAddress.getByName("234.235.236.237");
			int port = 4080;
			socket = new MulticastSocket(port);
			socket.joinGroup(address);
			//(Disables hearing our own voice, off for testing purposes) TODO Turn back on
			socket.setLoopbackMode(true);
			
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
        
        //Listen for connections to a network (Or a disconnection)
        registerReceiver(broadcastReceiver, intentFilter);
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	
    	//Kill our threads
		try{
			sender.stop();
			listener.stop();
		}
		catch(Exception e){}
        mcLock.release();
        unregisterReceiver(broadcastReceiver);
        socket.close();
        
        //Clear our cached players from the network
        Global.localObjects = new ArrayList<LocalNetworkObject>();
    }
    
    private void challengeSent(){
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	new LANMessage(Global.player1Name+" challenges you. Grid size: "+Global.gridSize, lno.ip, 4080);
    	            break;
    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked
    	        	//Do nothing
    	            break;
    	        }
    	    }
    	};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Do you want to challenge "+LocalLobbyActivity.lno.toString()+"?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }
    
    private void challengeRecieved(){
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	new LANMessage("It's on! My color's "+Global.player1Color, lno.ip, 4080);
    	            break;
    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked
    	        	//Do nothing
    	            break;
    	        }
    	    }
    	};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(LocalLobbyActivity.lno.toString()+" challenges you. Accept?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }
    
    private void updateResultsInUi(){
    	final ListView lobby = (ListView) findViewById(R.id.players);
        ArrayAdapter<LocalNetworkObject> adapter = new ArrayAdapter<LocalNetworkObject>(this,android.R.layout.simple_list_item_1, Global.localObjects);
        lobby.setAdapter(adapter);
        
        lobby.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				challengeSent();
			}
        });
    }
    
    private void customIP(){
    	final EditText editText = new EditText(this);
    	editText.setInputType(InputType.TYPE_CLASS_PHONE);
    	final AlertDialog.Builder sent = new AlertDialog.Builder(this);
        sent.setPositiveButton("Okay", null);
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	if(editText.getText().toString().equals(Global.LANipAddress)){
    	    		sent.setMessage("That's your own ip").show();
    	    	}
    	    	else{
					try {
						InetAddress local = InetAddress.getByName(editText.getText().toString());
						LocalLobbyActivity.lno.ip = local;
						new LANMessage(Global.player1Name+" challenges you. Grid size: "+Global.gridSize, lno.ip, 4080);
						sent.setMessage("Challenge sent").show();
					}
					catch (UnknownHostException e) {
						e.printStackTrace();
					}
    	    	}
    	    }
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Your ip address is: "+Global.LANipAddress).setView(editText).setPositiveButton("Enter", dialogClickListener).show();
    }
}