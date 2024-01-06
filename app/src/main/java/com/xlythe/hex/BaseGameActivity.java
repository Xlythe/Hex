package com.xlythe.hex;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesCompat;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.TurnBasedMultiplayerClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static com.xlythe.hex.Settings.TAG;

public abstract class BaseGameActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SIGN_IN = 1001;

    private GoogleSignInClient mGoogleSignInClient;

    private GoogleSignInAccount mGoogleSignInAccount;

    private GamesClient mGamesClient;
    private TurnBasedMultiplayerClient mTurnBasedMultiplayerClient;
    private PlayersClient mPlayersClient;
    private AchievementsClient mAchievementsClient;
    private InvitationsClient mInvitationsClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestProfile()
                .build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleSignInClient.silentSignIn()
                .addOnSuccessListener(this::onSignInSucceeded)
                .addOnFailureListener(this::onSignInFailed);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(this::onSignInSucceeded)
                    .addOnFailureListener(this::onSignInFailed);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public GamesClient getGamesClient() {
        return mGamesClient;
    }

    public TurnBasedMultiplayerClient getTurnBasedMultiplayerClient() {
        return mTurnBasedMultiplayerClient;
    }

    public PlayersClient getPlayersClient() {
        return mPlayersClient;
    }

    public AchievementsClient getAchievementsClient() {
        return mAchievementsClient;
    }

    public InvitationsClient getInvitationsClient() {
        return mInvitationsClient;
    }

    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    public void signIn() {
        Log.v(TAG, "User initiated sign in");
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    public void signOut() {
        mGoogleSignInClient.signOut();
        onSignInFailed();
    }

    @Nullable
    public GoogleSignInAccount getGoogleSignInAccount() {
        return mGoogleSignInAccount;
    }

    public void onSignInSucceeded(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "User successfully signed in: " + googleSignInAccount.getDisplayName());
        mGoogleSignInAccount = googleSignInAccount;
        mGamesClient = Games.getGamesClient(this, googleSignInAccount);
        mTurnBasedMultiplayerClient = GamesCompat.getTurnBasedMultiplayerClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);
        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mInvitationsClient = GamesCompat.getInvitationsClient(this, googleSignInAccount);
    }

    public void onSignInFailed() {
        onSignInFailed(null);
    }

    public void onSignInFailed(@Nullable Throwable reason) {
        Log.e(TAG, "Failed to sign in", reason);
        mGoogleSignInAccount = null;
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
