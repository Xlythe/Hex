package com.sam.hex;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.sam.hex.view.HexagonLayout;

/**
 * @author Will Harmon
 **/
public class MainActivity extends BaseGameActivity {
    public static final int REQUEST_ACHIEVEMENTS = 1001;

    // Stat variables
    TextView mTitleTextView;
    TextView mTimePlayedTextView;
    TextView mGamesPlayedTextView;
    TextView mGamesWonTextView;

    // Play variables
    SignInButton mSignInButton;
    Button mSignOutButton;
    boolean mIsSignedIn = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getSupportActionBar().hide();
        enableDebugLog(true, "HEX_DEBUG");

        HexagonLayout hexagonLayout = (HexagonLayout) findViewById(R.id.hexagonButtons);
        HexagonLayout.Button settingsButton = hexagonLayout.getButtons()[0];
        HexagonLayout.Button donateButton = hexagonLayout.getButtons()[1];
        HexagonLayout.Button historyButton = hexagonLayout.getButtons()[2];
        HexagonLayout.Button instructionsButton = hexagonLayout.getButtons()[3];
        HexagonLayout.Button achievementsButton = hexagonLayout.getButtons()[4];
        HexagonLayout.Button playButton = hexagonLayout.getButtons()[5];

        mTitleTextView = (TextView) findViewById(R.id.title);
        mTimePlayedTextView = (TextView) findViewById(R.id.timePlayed);
        mGamesPlayedTextView = (TextView) findViewById(R.id.gamesPlayed);
        mGamesWonTextView = (TextView) findViewById(R.id.gamesWon);

        mSignInButton = (SignInButton) findViewById(R.id.signInButton);
        mSignOutButton = (Button) findViewById(R.id.signOutButton);

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
                if(mIsSignedIn) {
                    startActivityForResult(getGamesClient().getAchievementsIntent(), REQUEST_ACHIEVEMENTS);
                }
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

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginUserInitiatedSignIn();
            }
        });

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                mSignOutButton.setVisibility(View.GONE);
                mSignInButton.setVisibility(View.VISIBLE);
                mTitleTextView.setText(String.format(getString(R.string.main_title),
                        mIsSignedIn ? getGamesClient().getCurrentPlayer().getDisplayName() : Stats.getPlayer1Name(getApplicationContext())));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        mTitleTextView.setText(String.format(getString(R.string.main_title),
                mIsSignedIn ? getGamesClient().getCurrentPlayer().getDisplayName() : Stats.getPlayer1Name(this)));
        long timePlayedInMillis = Stats.getTimePlayed(this);
        long timePlayedInHours = timePlayedInMillis / (1000 * 60 * 60);
        long timePlayedInMintues = (timePlayedInMillis - timePlayedInHours * (1000 * 60 * 60)) / (1000 * 60);
        long timePlayedInSeconds = (timePlayedInMillis - timePlayedInHours * (1000 * 60 * 60) - timePlayedInMintues * (1000 * 60)) / (1000);
        mTimePlayedTextView.setText(String.format(getString(R.string.main_stats_time_played), timePlayedInHours, timePlayedInMintues, timePlayedInSeconds));
        mGamesPlayedTextView.setText(String.format(getString(R.string.main_stats_games_played), Stats.getGamesPlayed(this)));
        mGamesWonTextView.setText(String.format(getString(R.string.main_stats_games_won), Stats.getGamesWon(this)));
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsSignedIn = false;
    }

    @Override
    public void onSignInSucceeded() {
        System.out.println("Signed in");
        mIsSignedIn = true;
        mSignOutButton.setVisibility(View.VISIBLE);
        mSignInButton.setVisibility(View.GONE);
        mTitleTextView.setText(String.format(getString(R.string.main_title),
                mIsSignedIn ? getGamesClient().getCurrentPlayer().getDisplayName() : Stats.getPlayer1Name(this)));
    }

    @Override
    public void onSignInFailed() {
        System.out.println("Sign in failed");
    }
}
