package com.sam.hex;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.games.Games;
import com.hex.core.Game;
import com.sam.hex.fragment.GameFragment;
import com.sam.hex.fragment.GameSelectionFragment;
import com.sam.hex.fragment.HistoryFragment;
import com.sam.hex.fragment.InstructionsFragment;
import com.sam.hex.fragment.MainFragment;
import com.sam.hex.fragment.OnlineSelectionFragment;

/**
 * @author Will Harmon
 **/
public class MainActivity extends NetActivity {
    public static int STAT_STATE = 3;

    // Play variables
    private boolean mIsSignedIn = false;
    private boolean mOpenAchievements = false;
    private boolean mOpenOnlineSelectionFragment = false;

    // Fragments
    private MainFragment mMainFragment;
    private GameFragment mGameFragment;
    private GameSelectionFragment mGameSelectionFragment;
    private HistoryFragment mHistoryFragment;
    private InstructionsFragment mInstructionsFragment;
    private OnlineSelectionFragment mOnlineSelectionFragment;
    
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mMainFragment = new MainFragment();
            mMainFragment.setInitialRotation(-120f);
            mMainFragment.setInitialSpin(50f);
            getSupportFragmentManager().beginTransaction().add(R.id.content, mMainFragment).commit();
        }

        popupRatingDialog();
    }

    public void returnHome() {
        if (mMainFragment == null) mMainFragment = new MainFragment();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onSignInSucceeded(@Nullable Bundle bundle) {
        super.onSignInSucceeded(bundle);
        mIsSignedIn = true;

        //getAppStateClient().loadState(this, STAT_STATE);
        if (mMainFragment != null) mMainFragment.setSignedIn(mIsSignedIn);

        if (mOpenAchievements) {
            mOpenAchievements = false;
            startActivityForResult(Games.Achievements.getAchievementsIntent(getClient()), RC_ACHIEVEMENTS);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        if (mOpenOnlineSelectionFragment) {
            mOpenOnlineSelectionFragment = false;
            setOnlineSelectionFragment(new OnlineSelectionFragment());
            swapFragment(this.getOnlineSelectionFragment());
        }
    }

    @Override
    public void onSignInFailed() {
        super.onSignInFailed();
        mIsSignedIn = false;
        if (mMainFragment != null) mMainFragment.setSignedIn(mIsSignedIn);
    }

    public void swapFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.content, newFragment).addToBackStack(null).commitAllowingStateLoss();
    }

    public MainFragment getMainFragment() {
        return mMainFragment;
    }

    public void setMainFragment(MainFragment mainFragment) {
        this.mMainFragment = mainFragment;
    }

    public GameFragment getGameFragment() {
        return mGameFragment;
    }

    public void setGameFragment(GameFragment gameFragment) {
        this.mGameFragment = gameFragment;
    }

    public GameSelectionFragment getGameSelectionFragment() {
        return mGameSelectionFragment;
    }

    public void setGameSelectionFragment(GameSelectionFragment gameSelectionFragment) {
        this.mGameSelectionFragment = gameSelectionFragment;
    }

    public HistoryFragment getHistoryFragment() {
        return mHistoryFragment;
    }

    public void setHistoryFragment(HistoryFragment historyFragment) {
        this.mHistoryFragment = historyFragment;
    }

    public InstructionsFragment getInstructionsFragment() {
        return mInstructionsFragment;
    }

    public void setInstructionsFragment(InstructionsFragment instructionsFragment) {
        this.mInstructionsFragment = instructionsFragment;
    }

    public OnlineSelectionFragment getOnlineSelectionFragment() {
        return mOnlineSelectionFragment;
    }

    public void setOnlineSelectionFragment(OnlineSelectionFragment onlineSelectionFragment) {
        this.mOnlineSelectionFragment = onlineSelectionFragment;
    }

    private void popupRatingDialog() {
        // Popup asking to rate app after countdown
        int numTimesAppOpened = PreferenceManager.getDefaultSharedPreferences(this).getInt("num_times_app_opened_review", 0);
        if (numTimesAppOpened != -1) {
            numTimesAppOpened++;
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("num_times_app_opened_review", numTimesAppOpened).commit();
            if (numTimesAppOpened > 5) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("num_times_app_opened_review", -1).commit();
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sam.hex")));
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("num_times_app_opened_review", -1).commit();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.review_popup_title).setMessage(R.string.review_popup_message).setPositiveButton(R.string.review_popup_ok, dialogClickListener).setNegativeButton(R.string.review_popup_never, dialogClickListener);

                // Wrap in try/catch because this can sometimes leak window
                try {
                    builder.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setOpenAchievements(boolean open) {
        this.mOpenAchievements = open;
    }

    public void setOpenOnlineSelectionFragment(boolean open) {
        this.mOpenOnlineSelectionFragment = open;
    }

    @Override
    public void switchToGame(@NonNull Game game, boolean leaveRoom) {
        if (mGameFragment != null) {
            mGameFragment.setLeaveRoom(leaveRoom);
        }

        Bundle b = new Bundle();
        b.putBoolean(GameFragment.NET, true);

        mGameFragment = new GameFragment();
        mGameFragment.setGame(game);
        mGameFragment.setPlayer1Type(game.getPlayer1().getType());
        mGameFragment.setPlayer2Type(game.getPlayer2().getType());
        mGameFragment.setArguments(b);

        swapFragment(mGameFragment);
    }

    public static class Stat {
        private long timePlayed;
        private long gamesWon;
        private long gamesPlayed;
        private int donationRank;

        public long getTimePlayed() {
            return timePlayed;
        }

        public void setTimePlayed(long timePlayed) {
            this.timePlayed = timePlayed;
        }

        public long getGamesWon() {
            return gamesWon;
        }

        public void setGamesWon(long gamesWon) {
            this.gamesWon = gamesWon;
        }

        public long getGamesPlayed() {
            return gamesPlayed;
        }

        public void setGamesPlayed(long gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
        }

        public int getDonationRank() {
            return donationRank;
        }

        public void setDonationRank(int donationRank) {
            this.donationRank = donationRank;
        }
    }
}
