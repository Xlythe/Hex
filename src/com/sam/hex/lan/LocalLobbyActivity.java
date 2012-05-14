package com.sam.hex.lan;

import java.net.InetAddress;
import java.net.MulticastSocket;

import com.sam.hex.GameAction;
import com.sam.hex.Global;
import com.sam.hex.HexGame;
import com.sam.hex.Preferences;
import com.sam.hex.R;
import com.sam.hex.net.NetLobbyActivity;

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
import android.provider.Settings;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * @author Will Harmon
 **/
public class LocalLobbyActivity extends Activity {
	WifiManager wm;
	MulticastLock mcLock;
	WifiBroadcastReceiver broadcastReceiver;
	IntentFilter intentFilter;
	MulticastListener multicastListener;
	UnicastListener unicastListener;
	MulticastSender sender;
	MulticastSocket socket;
    final Handler handler = new Handler();
    final Runnable updateResults = new Runnable() {
        public void run() {
        	updateResultsInUi();
        }
    };
    final Runnable challenger = new Runnable() {
        public void run() {
        	challengeRecieved();
        }
    };
    final Runnable startGame = new Runnable() {
        public void run() {
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
        
        LANGlobal.localPlayer = new LocalNetworkObject();
        
        wm = (WifiManager) getSystemService(WIFI_SERVICE);
        mcLock = wm.createMulticastLock("broadcastlock");
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        broadcastReceiver = new WifiBroadcastReceiver(handler, updateResults, challenger, startGame, multicastListener, unicastListener, sender, wm);
        
        final Button ipButton = (Button) findViewById(R.id.customIP);
        ipButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	customIP();
            }
        });
        
        Button home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//Load preferences
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	if(Integer.decode(prefs.getString("gameLocation", "0")) != LANGlobal.GAME_LOCATION && Integer.decode(prefs.getString("gameLocation", "0")) == 0){
    		//Go into a normal game
        	startActivity(new Intent(getBaseContext(),HexGame.class));
        	finish();
    	}
        else if(Integer.decode(prefs.getString("gameLocation", "0")) != LANGlobal.GAME_LOCATION && Integer.decode(prefs.getString("gameLocation", "0")) == 2){
    		//Go to the local lobby
        	startActivity(new Intent(getBaseContext(),NetLobbyActivity.class));
        	finish();
    	}
        else{

	    	//Set player's name, color, grid size
	    	LANGlobal.playerName = prefs.getString("lanPlayerName", "Player");
	    	LANGlobal.playerColor = prefs.getInt("lanPlayerColor", Global.player1DefaultColor);
	    	LANGlobal.gridSize=Integer.decode(prefs.getString("gameSizePref", "7"));
	    	if(LANGlobal.gridSize==0) LANGlobal.gridSize=Integer.decode(prefs.getString("customGameSizePref", "7"));
	    	if(LANGlobal.gridSize<=0) LANGlobal.gridSize=1;
	    	
	        if (!wm.isWifiEnabled()) {
	        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	        	    public void onClick(DialogInterface dialog, int which) {
	        	        switch (which){
	        	        case DialogInterface.BUTTON_POSITIVE:
	        	            //Yes button clicked
	        	        	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
	        	            break;
	        	        case DialogInterface.BUTTON_NEGATIVE:
	        	            //No button clicked
	        	        	android.os.Process.killProcess(android.os.Process.myPid());
	        	            break;
	        	        }
	        	    }
	        	};
	
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setMessage(getApplicationContext().getString(R.string.wifiOff)).setPositiveButton(getApplicationContext().getString(R.string.yes), dialogClickListener).setNegativeButton(getApplicationContext().getString(R.string.no), dialogClickListener).show();
	        }
	        
	        //Allow for broadcasts
	        mcLock.acquire();
	        
	        //Get our ip address
	        WifiInfo wifiInfo = wm.getConnectionInfo();
	        LANGlobal.LANipAddress = String.format("%d.%d.%d.%d",(wifiInfo.getIpAddress() & 0xff),(wifiInfo.getIpAddress() >> 8 & 0xff),(wifiInfo.getIpAddress() >> 16 & 0xff),(wifiInfo.getIpAddress() >> 24 & 0xff));
	        
	        try {
				//Create a socket
				InetAddress address = InetAddress.getByName(LANGlobal.MULTICASTADDRESS);
				socket = new MulticastSocket(LANGlobal.MULTICASTPORT);
				socket.joinGroup(address);
				//(Disables hearing our own voice, off for testing purposes) TODO Turn back on
				socket.setLoopbackMode(true);
			}
	        catch (Exception e) {
				e.printStackTrace();
			}
	        
	        //Start sending
			sender=new MulticastSender(socket);
			//Start listening
	        multicastListener=new MulticastListener(socket, handler, updateResults);
	        unicastListener=new UnicastListener(handler, challenger, startGame);
	        
	        //Listen for connections to a network (Or a disconnection)
	        registerReceiver(broadcastReceiver, intentFilter);
        }
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	
    	//Kill our threads
		try{
			unregisterReceiver(broadcastReceiver);
			sender.stop();
			multicastListener.stop();
			unicastListener.stop();
	        socket.close();
	        mcLock.release();
		}
		catch(Exception e){
			System.out.println(e);
		}
        
        //Clear our cached players from the network
        LANGlobal.localObjects.clear();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.locallobby_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.settings:
        	startActivity(new Intent(getBaseContext(),Preferences.class));
            return true;
        case R.id.quit:
        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int which) {
        	        switch (which){
        	        case DialogInterface.BUTTON_POSITIVE:
        	            //Yes button clicked
        	        	android.os.Process.killProcess(android.os.Process.myPid());
        	            break;
        	        case DialogInterface.BUTTON_NEGATIVE:
        	            //No button clicked
        	        	//Do nothing
        	            break;
        	        }
        	    }
        	};

        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage(this.getString(R.string.confirmExit)).setPositiveButton(this.getString(R.string.yes), dialogClickListener).setNegativeButton(this.getString(R.string.no), dialogClickListener).show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void challengeSent(final LocalNetworkObject player){
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	LANGlobal.localPlayer.ip = player.ip;
					LANGlobal.localPlayer.firstMove = false;
					new LANMessage(LANGlobal.playerName+" challenges you. Grid size: "+LANGlobal.gridSize, LANGlobal.localPlayer.ip, LANGlobal.CHALLENGERPORT);
					new LANMessage("What's your name?", LANGlobal.localPlayer.ip, LANGlobal.CHALLENGERPORT);
					new LANMessage("What's your name?", LANGlobal.localPlayer.ip, LANGlobal.CHALLENGERPORT);
					new LANMessage("What's your name?", LANGlobal.localPlayer.ip, LANGlobal.CHALLENGERPORT);
    	            break;
    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked
    	        	//Do nothing
    	            break;
    	        }
    	    }
    	};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(GameAction.insert(getApplicationContext().getString(R.string.sendChallenge), player.playerName)).setPositiveButton(getApplicationContext().getString(R.string.yes), dialogClickListener).setNegativeButton(getApplicationContext().getString(R.string.no), dialogClickListener).show();
    }
    
    private void challengeRecieved(){
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	LANGlobal.localPlayer.firstMove = true;
    	        	new LANMessage("Its on! My color is "+LANGlobal.playerColor, LANGlobal.localPlayer.ip, LANGlobal.CHALLENGERPORT);
    	            break;
    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked
    	        	LANGlobal.localPlayer = new LocalNetworkObject();
    	            break;
    	        }
    	    }
    	};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(GameAction.insert(getApplicationContext().getString(R.string.challenger), LANGlobal.localPlayer.playerName)).setPositiveButton(getApplicationContext().getString(R.string.yes), dialogClickListener).setNegativeButton(getApplicationContext().getString(R.string.no), dialogClickListener).show();
    }
    
    private void updateResultsInUi(){
    	final ListView lobby = (ListView) findViewById(R.id.players);
        ArrayAdapter<LocalNetworkObject> adapter = new ArrayAdapter<LocalNetworkObject>(this,R.layout.simple_list_item_1, LANGlobal.localObjects);
        lobby.setAdapter(adapter);
        
        lobby.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				challengeSent(LANGlobal.localObjects.get(position));
			}
        });
    }
    
    private void customIP(){
    	final EditText editText = new EditText(this);
    	editText.setInputType(InputType.TYPE_CLASS_PHONE);
    	final AlertDialog.Builder sent = new AlertDialog.Builder(this);
        sent.setPositiveButton(getApplicationContext().getString(R.string.okay), null);
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	if(editText.getText().toString().equals(LANGlobal.LANipAddress)){
    	    		sent.setMessage(getApplicationContext().getString(R.string.yourIPWarning)).show();
    	    	}
    	    	else if(!editText.getText().toString().equals("")){
					try {
						LANGlobal.localPlayer.ip = InetAddress.getByName(editText.getText().toString());
						LANGlobal.localPlayer.firstMove = false;
						new LANMessage(LANGlobal.playerName+" challenges you. Grid size: "+LANGlobal.gridSize, LANGlobal.localPlayer.ip, LANGlobal.CHALLENGERPORT);
						new LANMessage("What's your name?", LANGlobal.localPlayer.ip, LANGlobal.CHALLENGERPORT);
						new LANMessage("What's your name?", LANGlobal.localPlayer.ip, LANGlobal.CHALLENGERPORT);
						new LANMessage("What's your name?", LANGlobal.localPlayer.ip, LANGlobal.CHALLENGERPORT);
						sent.setMessage(getApplicationContext().getString(R.string.challengeSent)).show();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
    	    	}
    	    }
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(GameAction.insert(getApplicationContext().getString(R.string.yourIP), LANGlobal.LANipAddress)).setView(editText).setPositiveButton(getApplicationContext().getString(R.string.enter), dialogClickListener).show();
    }
}