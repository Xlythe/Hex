package com.sam.hex;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;

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
    private static final int DONT_ASK_AGAIN = -1;
    private static final int TIMES_OPEN_UNTIL_REVIEW_REQUEST = 15;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 3;

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

            popupRatingDialog();
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
            if (fragment != null) {
                if (fragment instanceof MainFragment) {
                    mMainFragment = (MainFragment) fragment;
                } else if (fragment instanceof GameFragment) {
                    mGameFragment = (GameFragment) fragment;
                } else if (fragment instanceof GameSelectionFragment) {
                    mGameSelectionFragment = (GameSelectionFragment) fragment;
                } else if (fragment instanceof HistoryFragment) {
                    mHistoryFragment = (HistoryFragment) fragment;
                } else if (fragment instanceof InstructionsFragment) {
                    mInstructionsFragment = (InstructionsFragment) fragment;
                } else if (fragment instanceof OnlineSelectionFragment) {
                    mOnlineSelectionFragment = (OnlineSelectionFragment) fragment;
                }
            }
        }

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
                finish();
            }
        }
    }

    public void returnHome() {
        if (mMainFragment == null) mMainFragment = new MainFragment();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onSignInSucceeded(@Nullable Bundle bundle) {
        super.onSignInSucceeded(bundle);
        mIsSignedIn = true;

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
        int numTimesAppOpened = Settings.getNumTimesOpened(this);
        if (numTimesAppOpened != DONT_ASK_AGAIN) {
            Settings.incrementNumTimesOpened(this);
            if (numTimesAppOpened > TIMES_OPEN_UNTIL_REVIEW_REQUEST) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Settings.setTimesOpened(getApplicationContext(), DONT_ASK_AGAIN);
                                startActivity(new Intent(Intent.ACTION_VIEW, getMarketUri()));
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Settings.setTimesOpened(getApplicationContext(), DONT_ASK_AGAIN);
                                break;
                        }
                    }

                    private Uri getMarketUri() {
                        return Uri.parse("market://details?id=" + getPackageName());
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

    /**
     * Returns true if all given permissions are available
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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
