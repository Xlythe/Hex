package com.sam.hex.startup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.sam.hex.HexGame;
import com.sam.hex.Preferences;
import com.sam.hex.R;
import com.sam.hex.activity.HomeActivity;
import com.sam.hex.view.HexagonLayout;

/**
 * @author Will Harmon
 **/
public class StartUpActivity extends HomeActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getSupportActionBar().hide();

        TextView title = (TextView) findViewById(R.id.title);
        TextView timePlayed = (TextView) findViewById(R.id.timePlayed);
        TextView gamesPlayed = (TextView) findViewById(R.id.gamesPlayed);
        TextView gamesWon = (TextView) findViewById(R.id.gamesWon);
        HexagonLayout hexagonLayout = (HexagonLayout) findViewById(R.id.hexagonButtons);
        HexagonLayout.Button settingsButton = hexagonLayout.getButtons()[0];
        HexagonLayout.Button donateButton = hexagonLayout.getButtons()[1];
        HexagonLayout.Button rateButton = hexagonLayout.getButtons()[2];
        HexagonLayout.Button instructionsButton = hexagonLayout.getButtons()[3];
        HexagonLayout.Button achievementsButton = hexagonLayout.getButtons()[4];
        HexagonLayout.Button playButton = hexagonLayout.getButtons()[5];

        title.setText("Will's stats");
        timePlayed.setText("time played 00:00:00");
        gamesPlayed.setText("games played 15");
        gamesWon.setText("games won 10");

        settingsButton.setTitle("settings");
        settingsButton.setColor(0xcc5c57);
        settingsButton.setDrawable(getResources().getDrawable(R.drawable.icon));
        settingsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(getBaseContext(), Preferences.class));
            }
        });

        donateButton.setTitle("donate");
        donateButton.setColor(0x5f6ec2);
        donateButton.setDrawable(getResources().getDrawable(R.drawable.icon));
        donateButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.donate.hex")));
            }
        });

        rateButton.setTitle("rate");
        rateButton.setColor(0xcfca47);
        rateButton.setDrawable(getResources().getDrawable(R.drawable.icon));
        rateButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sam.hex")));
            }
        });

        instructionsButton.setTitle("how to play");
        instructionsButton.setColor(0xb7cf47);
        instructionsButton.setDrawable(getResources().getDrawable(R.drawable.icon));
        instructionsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(getBaseContext(), InstructionsActivity.class));
            }
        });

        achievementsButton.setTitle("achievements");
        achievementsButton.setColor(0xf48935);
        achievementsButton.setDrawable(getResources().getDrawable(R.drawable.icon));
        achievementsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {}
        });

        playButton.setTitle("play");
        playButton.setColor(0x4ba5e2);
        playButton.setDrawable(getResources().getDrawable(R.drawable.icon));
        playButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(getBaseContext(), HexGame.class));
            }
        });
    }
}
