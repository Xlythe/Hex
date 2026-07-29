package com.xlythe.hex.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.games.AchievementsClient;
import com.xlythe.hex.compat.Game;
import com.xlythe.hex.MainActivity;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.xlythe.hex.Settings.TAG;

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
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to change screen on state because fragment is detached.");
            return;
        }

        activity.keepScreenOn(screenOn);
    }

    protected void applyOpenTransition(@AnimRes int enterAnim, @AnimRes int exitAnim) {
        getMainActivity().applyOpenTransition(enterAnim, exitAnim);
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

    protected AchievementsClient getAchievementsClient() {
        return getMainActivity().getAchievementsClient();
    }

    @Nullable
    protected String getPlayGamesPlayerName() {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            return null;
        }

        return activity.getPlayGamesPlayerName();
    }

    protected boolean isSignedIn() {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            return false;
        }

        return activity.isSignedIn();
    }

    protected void signIn() {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to sign in because current fragment is detached.");
            return;
        }

        activity.signIn();
    }

    protected void returnHome() {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to return home because current fragment is detached.");
            return;
        }

        activity.returnHome();
    }

    protected void startQuickGame() {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to start a quick game because current fragment is detached.");
            return;
        }

        activity.startQuickGame();
    }

    protected void inviteFriends() {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to invite friends because current fragment is detached.");
            return;
        }

        activity.inviteFriends();
    }

    protected void checkInvites() {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to check invites because current fragment is detached.");
            return;
        }

        activity.checkInvites();
    }

    protected void openAchievements() {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to open achievements because current fragment is detached.");
            return;
        }

        activity.openAchievements();
    }

    protected void switchToGame(Game game) {
        MainActivity activity = getMainActivity();
        if (activity == null || isDetached()) {
            Log.w(TAG, "Unable to switch to game because current fragment is detached.");
            return;
        }

        activity.switchToGame(game);
    }
}
