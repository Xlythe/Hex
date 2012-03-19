package com.sam.hex;

import com.sam.hex.lan.LocalLobbyActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

public class StartUpActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        

    	//Load preferences
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        //First button
        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(Integer.parseInt(prefs.getString("player2Type", "0")) == (byte)2 && !(Global.player2Type == (byte)2 && Global.gameRunning)){
            		startActivity(new Intent(getBaseContext(),LocalLobbyActivity.class));
            	}
            	else{
            		startActivity(new Intent(getBaseContext(),HexGame.class));
            	}
            }
        });
        
        //Second button
        final Button instructionsButton = (Button) findViewById(R.id.instructionsButton);
        instructionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent instructionsActivity = new Intent(getBaseContext(),InstructionsActivity.class);
            	startActivity(instructionsActivity);
            }
        });
        
        //Third button
        final Button optionsButton = (Button) findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent settingsActivity = new Intent(getBaseContext(),Preferences.class);
            	startActivity(settingsActivity);
            }
        });
    }
    
    @Override
    public void onResume(){
    	super.onResume();

    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	//Refresh first button
        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(Integer.parseInt(prefs.getString("player2Type", "0")) == (byte)2 && !(Global.player2Type == (byte)2 && Global.gameRunning)){
                	startActivity(new Intent(getBaseContext(),LocalLobbyActivity.class));
            	}
            	else{
            		startActivity(new Intent(getBaseContext(),HexGame.class));
            	}
            }
        });
        if(HexGame.somethingChanged(prefs) || HexGame.startNewGame){
        	startButton.setText("Start Game");
    	}
        else{
        	startButton.setText("Resume Game");
        }
        
        //Set player names
    	Global.player1Name = prefs.getString("player1Name", "Player1");
    	Global.player2Name = prefs.getString("player2Name", "Player2");
    }
}