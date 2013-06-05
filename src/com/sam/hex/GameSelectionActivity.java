package com.sam.hex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class GameSelectionActivity extends SherlockFragmentActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_type);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Button AI = (Button) findViewById(R.id.button1);
        AI.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(getBaseContext(), GameActivity.class));
                prefs.edit().putString("player2Type", "4").apply();
                finish();
            }
        });

        Button hotseat = (Button) findViewById(R.id.button2);
        hotseat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(getBaseContext(), GameActivity.class));
                prefs.edit().putString("player2Type", "0").apply();
                finish();
            }
        });

        Button net = (Button) findViewById(R.id.button3);
        net.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        net.setEnabled(false);

    }
}
