package com.sam.hex.fragment;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.TurnBasedMultiplayerClient;
import com.sam.hex.compat.Game;
import com.sam.hex.MainActivity;

import static com.sam.hex.Settings.TAG;

/**
 * @author Will Harmon
 **/
public class HexFragment extends Fragment {

    private ViewGroup mContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mContainer = container;
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mContainer.requestFocus();
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    protected void keepScreenOn(boolean screenOn) {
        getMainActivity().keepScreenOn(screenOn);
    }

    protected void overridePendingTransition(@AnimRes int enterAnim, @AnimRes int exitAnim) {
        getMainActivity().overridePendingTransition(enterAnim, exitAnim);
    }

    protected void runOnUiThread(Runnable r) {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to run runnable on ui thread because fragment is detached.");
            return;
        }
        activity.runOnUiThread(r);
    }

    protected void swapFragment(Fragment fragment) {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to swap fragment because current fragment is detached.");
            return;
        }
        activity.swapFragment(fragment);
    }

    protected GamesClient getGamesClient() {
        return getMainActivity().getGamesClient();
    }

    protected TurnBasedMultiplayerClient getTurnBasedMultiplayerClient() {
        return getMainActivity().getTurnBasedMultiplayerClient();
    }

    protected PlayersClient getPlayersClient() {
        return getMainActivity().getPlayersClient();
    }

    protected AchievementsClient getAchievementsClient() {
        return getMainActivity().getAchievementsClient();
    }

    @Nullable
    protected GoogleSignInAccount getGoogleSignInAccount() {
        return getMainActivity().getGoogleSignInAccount();
    }

    protected boolean isSignedIn() {
        return getMainActivity().isSignedIn();
    }

    protected void signIn() {
        getMainActivity().signIn();
    }

    protected void signOut() {
        getMainActivity().signOut();
    }

    protected void returnHome() {
        getMainActivity().returnHome();
    }

    protected void startQuickGame() {
        getMainActivity().startQuickGame();
    }

    protected void inviteFriends() {
        getMainActivity().inviteFriends();
    }

    protected void checkInvites() {
        getMainActivity().checkInvites();
    }

    protected void openAchievements() {
        getMainActivity().openAchievements();
    }

    protected void switchToGame(Game game) {
        getMainActivity().switchToGame(game);
    }
}
