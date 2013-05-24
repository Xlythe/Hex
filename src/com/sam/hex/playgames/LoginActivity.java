package com.sam.hex.playgames;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.SignInButton;
import com.sam.hex.BaseGameActivity;
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

        SignInButton mSignInButton = (SignInButton) findViewById(R.id.loginEnter);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the asynchronous sign in flow
                beginUserInitiatedSignIn();
            }
        });
    }

    // private RoomConfig.Builder makeBasicRoomConfigBuilder() {
    // return RoomConfig.builder(this)
    // .setMessageReceivedListener(this)
    // .setRoomStatusUpdateListener(this)
    // }

    @Override
    public void onSignInSucceeded() {
        // ...

        if(getInvitationId() != null) {
            // RoomConfigBuilder roomConfigBuilder =
            // makeBasicRoomConfigBuilder();
            // roomConfigBuilder.setInvitationIdToAccept(getInvitationId());
            // getGamesClient().joinRoom(roomConfigBuilder.build());

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // go to game screen
        }
    }

    @Override
    public void onSignInFailed() {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu_play_login, menu);
        return true;
    }

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
