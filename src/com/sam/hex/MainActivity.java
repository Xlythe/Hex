package com.sam.hex;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.sam.hex.view.HexagonLayout;

/**
 * @author Will Harmon
 **/
public class MainActivity extends BaseGameActivity {
    public static final int REQUEST_ACHIEVEMENTS = 1001;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getSupportActionBar().hide();

        HexagonLayout hexagonLayout = (HexagonLayout) findViewById(R.id.hexagonButtons);
        HexagonLayout.Button settingsButton = hexagonLayout.getButtons()[0];
        HexagonLayout.Button donateButton = hexagonLayout.getButtons()[1];
        HexagonLayout.Button historyButton = hexagonLayout.getButtons()[2];
        HexagonLayout.Button instructionsButton = hexagonLayout.getButtons()[3];
        HexagonLayout.Button achievementsButton = hexagonLayout.getButtons()[4];
        HexagonLayout.Button playButton = hexagonLayout.getButtons()[5];

        hexagonLayout.setText(R.string.app_name);

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
            public void onClick() {
                startActivityForResult(getGamesClient().getAchievementsIntent(), REQUEST_ACHIEVEMENTS);
            }
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

    @Override
    public void onResume() {
        super.onResume();

        TextView title = (TextView) findViewById(R.id.title);
        TextView timePlayed = (TextView) findViewById(R.id.timePlayed);
        TextView gamesPlayed = (TextView) findViewById(R.id.gamesPlayed);
        TextView gamesWon = (TextView) findViewById(R.id.gamesWon);

        title.setText(String.format(getString(R.string.main_title), Stats.getPlayer1Name(this)));
        long timePlayedInMillis = Stats.getTimePlayed(this);
        long timePlayedInHours = timePlayedInMillis / (1000 * 60 * 60);
        long timePlayedInMintues = (timePlayedInMillis - timePlayedInHours * (1000 * 60 * 60)) / (1000 * 60);
        long timePlayedInSeconds = (timePlayedInMillis - timePlayedInHours * (1000 * 60 * 60) - timePlayedInMintues * (1000 * 60)) / (1000);
        timePlayed.setText(String.format(getString(R.string.main_stats_time_played), timePlayedInHours, timePlayedInMintues, timePlayedInSeconds));
        gamesPlayed.setText(String.format(getString(R.string.main_stats_games_played), Stats.getGamesPlayed(this)));
        gamesWon.setText(String.format(getString(R.string.main_stats_games_won), Stats.getGamesWon(this)));
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
