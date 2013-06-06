package com.sam.hex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.view.MenuItem;

public class GameSelectionActivity extends BaseGameActivity {
    Button mNetButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.game_selection);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Button AI = (Button) findViewById(R.id.button1);
        AI.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                prefs.edit().putString("player2Type", "4").apply();
                startActivity(new Intent(getBaseContext(), GameActivity.class));
                finish();
            }
        });

        Button hotseat = (Button) findViewById(R.id.button2);
        hotseat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                prefs.edit().putString("player2Type", "0").apply();
                startActivity(new Intent(getBaseContext(), GameActivity.class));
                finish();
            }
        });

        mNetButton = (Button) findViewById(R.id.button3);
        mNetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(getBaseContext(), OnlineSelectionActivity.class));
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSignInSucceeded() {
        mNetButton.setEnabled(true);
    }

    @Override
    public void onSignInFailed() {
        mNetButton.setEnabled(false);
    }
}
