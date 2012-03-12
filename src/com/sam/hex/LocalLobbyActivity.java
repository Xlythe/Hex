package com.sam.hex;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class LocalLobbyActivity extends Activity {
	private List<LocalNetworkObject> values = new ArrayList<LocalNetworkObject>();
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locallobby);
        
        newPlayerFound("Sean", "192.168.1.1");
        newPlayerFound("Will", "192.168.1.2");
        newPlayerFound("Sam", "192.168.1.3");
        
        //TODO Create a thread that loops, looking for players to add
        
        //TODO Create a listener that runs challengeRecieved() if someone calls us
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	final WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);

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
        
        MulticastLock mcLock = wm.createMulticastLock("broadcastlock");
        mcLock.acquire();
    }
    
    private void newPlayerFound(String playerName, String ip){
    	values.add(new LocalNetworkObject(playerName, ip));
    	
    	final ListView playerList = (ListView) findViewById(R.id.players);
        ArrayAdapter<LocalNetworkObject> adapter = new ArrayAdapter<LocalNetworkObject>(this,android.R.layout.simple_list_item_1, values);
        playerList.setAdapter(adapter);
        
        playerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(getApplicationContext(), "IP Address: " + ((LocalNetworkObject) playerList.getItemAtPosition(position)).getIP(), Toast.LENGTH_LONG).show();
				challengeRecieved(playerList.getItemAtPosition(position).toString(),((LocalNetworkObject) playerList.getItemAtPosition(position)).getIP());
			}
        });
    }
    
    private void challengeRecieved(String name, String ipAddress){
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	startActivity(new Intent(getBaseContext(),HexGame.class));
    	            break;
    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked
    	        	//Do nothing
    	            break;
    	        }
    	    }
    	};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(name+" challenges you. Accept?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }
}