package com.sam.hex;

import java.io.UnsupportedEncodingException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.OnStateLoadedListener;
import com.google.gson.Gson;
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
public class MainActivity extends NetActivity implements OnStateLoadedListener {
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
    private Fragment mActiveFragment;

    public MainActivity() {
        super(CLIENT_GAMES | CLIENT_APPSTATE);
    }

    @Override
    public void onStateConflict(int stateKey, String ver, byte[] localData, byte[] serverData) {
        byte[] resolvedData = serverData;
        try {
            if(stateKey == STAT_STATE) {
                Gson gson = new Gson();
                Stat localStat = gson.fromJson(new String(localData, "UTF-8"), Stat.class);
                Stat serverStat = gson.fromJson(new String(serverData, "UTF-8"), Stat.class);
                Stat resolvedStat = new Stat();

                resolvedStat.setTimePlayed(Math.max(localStat.getTimePlayed(), serverStat.getTimePlayed()));
                resolvedStat.setGamesWon(Math.max(localStat.getGamesWon(), serverStat.getGamesWon()));
                resolvedStat.setGamesPlayed(Math.max(localStat.getGamesPlayed(), serverStat.getGamesPlayed()));
                resolvedStat.setDonationRank(Math.max(localStat.getDonationRank(), serverStat.getDonationRank()));

                resolvedData = gson.toJson(resolvedStat).getBytes();
            }
        }
        catch(UnsupportedEncodingException e) {}

        getAppStateClient().resolveState(this, stateKey, ver, resolvedData);
    }

    @Override
    public void onStateLoaded(int statusCode, int stateKey, byte[] buffer) {
        if(statusCode == AppStateClient.STATUS_OK) {
            try {
                if(stateKey == STAT_STATE) {
                    Gson gson = new Gson();
                    Stat stat = gson.fromJson(new String(buffer, "UTF-8"), Stat.class);

                    Stats.setTimePlayed(this, stat.getTimePlayed());
                    Stats.setGamesPlayed(this, stat.getGamesPlayed());
                    Stats.setGamesWon(this, stat.getGamesWon());
                    Stats.setDonationRank(this, stat.getDonationRank());
                }
                mMainFragment.setSignedIn(mIsSignedIn);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // mHexRealTimeMessageReceivedListener = new
        // HexRealTimeMessageReceivedListener();
        // mHexRoomStatusUpdateListener = new HexRoomStatusUpdateListener();
        // mHexRoomUpdateListener = new HexRoomUpdateListener(this);

        mMainFragment = new MainFragment();
        mMainFragment.setInitialRotation(-120f);
        mMainFragment.setInitialSpin(50f);
        swapFragment(mMainFragment);

        popupRatingDialog();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mActiveFragment == mHistoryFragment) {
                if(mHistoryFragment.goUp()) return true;
            }

            if(mActiveFragment != mMainFragment) {
                returnHome();
            }
            else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void returnHome() {
        if(mMainFragment == null) mMainFragment = new MainFragment();
        swapFragment(mMainFragment);
    }

    @Override
    public void onSignInSucceeded() {
        super.onSignInSucceeded();
        mIsSignedIn = true;

        // TODO Remove this after testers have wiped their bad stats
        getAppStateClient().updateState(0, null);
        getAppStateClient().updateState(1, null);
        getAppStateClient().updateState(2, null);

        getAppStateClient().loadState(this, STAT_STATE);
        mMainFragment.setSignedIn(mIsSignedIn);

        if(mOpenAchievements) {
            mOpenAchievements = false;
            startActivityForResult(getGamesClient().getAchievementsIntent(), RC_ACHIEVEMENTS);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        if(mOpenOnlineSelectionFragment) {
            mOpenOnlineSelectionFragment = false;
            setOnlineSelectionFragment(new OnlineSelectionFragment());
            swapFragment(this.getOnlineSelectionFragment());
        }
    }

    @Override
    public void onSignInFailed() {
        super.onSignInFailed();
        mIsSignedIn = false;
        mMainFragment.setSignedIn(mIsSignedIn);
    }

    public void swapFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.content, newFragment)
                .commit();
        mActiveFragment = newFragment;
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
        if(numTimesAppOpened != -1) {
            numTimesAppOpened++;
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("num_times_app_opened_review", numTimesAppOpened).commit();
            if(numTimesAppOpened > 5) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
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
                builder.setTitle(R.string.review_popup_title).setMessage(R.string.review_popup_message)
                        .setPositiveButton(R.string.review_popup_ok, dialogClickListener).setNegativeButton(R.string.review_popup_never, dialogClickListener);

                // Wrap in try/catch because this can sometimes leak window
                try {
                    builder.show();
                }
                catch(Exception e) {
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
    public void switchToGame(Game game, boolean leaveRoom) {
        if(mGameFragment != null) {
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // No call for super(). Bug on API Level > 11.
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
