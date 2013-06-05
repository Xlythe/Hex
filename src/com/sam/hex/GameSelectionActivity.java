package com.sam.hex;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GameSelectionActivity extends BaseGameActivity {

	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.game_type);
	        getSupportActionBar().hide();
	        Button AI = (Button) findViewById(R.id.button1);
	        Button hotseat = (Button) findViewById(R.id.button2);
	        Button net = (Button) findViewById(R.id.button3);
	        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	        final GameSelectionActivity self = this;
	        AI.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					   startActivity(new Intent(getBaseContext(), GameActivity.class));
					   prefs.edit().putString("player2Type", "4").apply();
					   finish();
					
				}});
	        hotseat.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					   startActivity(new Intent(getBaseContext(), GameActivity.class));
					   prefs.edit().putString("player2Type", "0").apply();
					   finish();
				}});
	        net.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					   
					   
					   finish();
				}});
	        net.setEnabled(false);
	        
	 }

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}
}

