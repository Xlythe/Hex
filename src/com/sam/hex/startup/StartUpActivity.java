package com.sam.hex.startup;

import com.sam.hex.Global;
import com.sam.hex.HexGame;
import com.sam.hex.Preferences;
import com.sam.hex.R;
import com.sam.hex.net.NetGlobal;
import com.sam.hex.net.NetHexGame;
import com.sam.hex.net.NetLobbyActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

/**
 * @author Will Harmon
 **/
public class StartUpActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Second button
        final Button instructionsButton = (Button) findViewById(R.id.instructionsButton);
        instructionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startActivity(new Intent(getBaseContext(),InstructionsActivity.class));
            }
        });
        
        //Third button
        final Button optionsButton = (Button) findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startActivity(new Intent(getBaseContext(),Preferences.class));
            }
        });
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	//Refresh first button
        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startActivity(new Intent(getBaseContext(),HexGame.class));
            }
        });
        if(HexGame.startNewGame || HexGame.somethingChanged(prefs, Global.GAME_LOCATION, Global.game)){
        	HexGame.startNewGame = true;
        	startButton.setText(R.string.start);
    	}
        else{ 
        	startButton.setText(R.string.resume);
        }
        
        //Refresh fourth button
        final Button onlineButton = (Button) findViewById(R.id.onlineButton);
        if(HexGame.somethingChanged(prefs, NetGlobal.GAME_LOCATION, NetGlobal.game)){
        	onlineButton.setText(R.string.online);
        	onlineButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	startActivity(new Intent(getBaseContext(),NetLobbyActivity.class));
                }
            });
    	}
        else{ 
        	onlineButton.setText(R.string.resume);
        	onlineButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	startActivity(new Intent(getBaseContext(),NetHexGame.class));
                }
            });
        }
    }
}