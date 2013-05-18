package com.sam.hex.playgames;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class LoginActivity extends BaseGameActivity {
    public static int REQUEST_ACHIEVEMENTS = 1001;
    public static int REQUEST_LEADERBOARD = 1002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_login);

        Button mSignInButton = (Button) findViewById(R.id.loginEnter);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the asynchronous sign in flow
                beginUserInitiatedSignIn();
            }
        });
    }

    @Override
    public void onSignInFailed() {}

    @Override
    public void onSignInSucceeded() {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()) {
        case R.id.menu_achievements:
            startActivityForResult(getGamesClient().getAchievementsIntent(), REQUEST_ACHIEVEMENTS);
            return true;
        case R.id.menu_leaderboard:
            startActivityForResult(getGamesClient().getLeaderboardIntent(getResources().getString(R.string.leaderboard_score)), REQUEST_LEADERBOARD);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
