package com.xlythe.hex;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static com.xlythe.hex.Settings.TAG;

public abstract class BaseGameActivity extends AppCompatActivity {

    private GamesSignInClient gamesSignInClient;
    private AchievementsClient mAchievementsClient;
    private String playGamesPlayerName;
    private boolean signedIn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayGamesSdk.initialize(getApplicationContext());
        gamesSignInClient = PlayGames.getGamesSignInClient(this);
        mAchievementsClient = PlayGames.getAchievementsClient(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        gamesSignInClient.isAuthenticated()
                .addOnSuccessListener(result -> {
                    if (result.isAuthenticated()) loadPlayGamesPlayer();
                    else onSignInFailed(null);
                })
                .addOnFailureListener(this::onSignInFailed);
    }

    public AchievementsClient getAchievementsClient() {
        return mAchievementsClient;
    }

    public boolean isSignedIn() {
        return signedIn;
    }

    public void signIn() {
        Log.v(TAG, "User initiated sign in");
        gamesSignInClient.signIn()
                .addOnSuccessListener(result -> {
                    if (result.isAuthenticated()) loadPlayGamesPlayer();
                    else onSignInFailed(null);
                })
                .addOnFailureListener(this::onSignInFailed);
    }

    @Nullable
    public String getPlayGamesPlayerName() {
        return playGamesPlayerName;
    }

    private void loadPlayGamesPlayer() {
        PlayGames.getPlayersClient(this).getCurrentPlayer()
                .addOnSuccessListener(player ->
                        onSignInSucceeded(player.getDisplayName()))
                .addOnFailureListener(this::onSignInFailed);
    }

    public void onSignInSucceeded(String playerName) {
        Log.d(TAG, "User successfully signed in to Play Games");
        signedIn = true;
        playGamesPlayerName = playerName;
    }

    public void onSignInFailed() {
        onSignInFailed(null);
    }

    public void onSignInFailed(@Nullable Throwable reason) {
        Log.e(TAG, "Failed to sign in", reason);
        signedIn = false;
        playGamesPlayerName = null;
    }

    public void keepScreenOn(boolean screenOn) {
        if (screenOn) {
            getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
        }
    }

    public abstract void startQuickGame();

    public abstract void inviteFriends();

    public abstract void checkInvites();

    public abstract void openAchievements();
}
