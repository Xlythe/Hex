package com.sam.hex;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.sam.hex.view.HexagonLayout;

/**
 * @author Will Harmon
 **/
public class MainActivity extends SherlockFragmentActivity {
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
        HexagonLayout.Button historyButton = hexagonLayout.getButtons()[2];
        HexagonLayout.Button instructionsButton = hexagonLayout.getButtons()[3];
        HexagonLayout.Button achievementsButton = hexagonLayout.getButtons()[4];
        HexagonLayout.Button playButton = hexagonLayout.getButtons()[5];

        title.setText("Will's stats");
        timePlayed.setText("time played 00:00:00");
        gamesPlayed.setText("games played 15");
        gamesWon.setText("games won 10");

        settingsButton.setText(R.string.main_button_settings);
        settingsButton.setColor(0xcc5c57);
        settingsButton.setDrawableResource(R.drawable.icon);
        settingsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(getBaseContext(), PreferencesActivity.class));
            }
        });

        donateButton.setText(R.string.main_button_donate);
        donateButton.setColor(0x5f6ec2);
        donateButton.setDrawableResource(R.drawable.icon);
        donateButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.donate.hex")));
            }
        });

        historyButton.setText(R.string.main_button_history);
        historyButton.setColor(0xcfca47);
        historyButton.setDrawableResource(R.drawable.icon);
        historyButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(getBaseContext(), HistoryActivity.class));
            }
        });

        instructionsButton.setText(R.string.main_button_instructions);
        instructionsButton.setColor(0xb7cf47);
        instructionsButton.setDrawableResource(R.drawable.icon);
        instructionsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(getBaseContext(), InstructionsActivity.class));
            }
        });

        achievementsButton.setText(R.string.main_button_achievements);
        achievementsButton.setColor(0xf48935);
        achievementsButton.setDrawableResource(R.drawable.icon);
        achievementsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {}
        });

        playButton.setText(R.string.main_button_play);
        playButton.setColor(0x4ba5e2);
        playButton.setDrawableResource(R.drawable.icon);
        playButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(getBaseContext(), GameActivity.class));
            }
        });
    }
}
