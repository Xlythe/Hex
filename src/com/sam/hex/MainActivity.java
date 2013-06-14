package com.sam.hex;

import java.io.UnsupportedEncodingException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.OnStateLoadedListener;
import com.hex.android.net.GameManager;
import com.hex.android.net.HexRoomUpdateListener;
import com.sam.hex.fragment.GameFragment;
import com.sam.hex.fragment.GameSelectionFragment;
import com.sam.hex.fragment.HistoryFragment;
import com.sam.hex.fragment.InstructionsFragment;
import com.sam.hex.fragment.MainFragment;
import com.sam.hex.fragment.OnlineSelectionFragment;

/**
 * @author Will Harmon
 **/
public class MainActivity extends BaseGameActivity implements OnStateLoadedListener {
    public static int PLAY_TIME_STATE = 0;
    public static int GAMES_PLAYED_STATE = 1;
    public static int GAMES_WON_STATE = 2;
    public static final int REQUEST_ACHIEVEMENTS = 1001;
    public final static int RC_SELECT_PLAYERS = 1002;
    public final static int RC_WAITING_ROOM = 1003;

    // Play variables
    private boolean mIsSignedIn = false;
    private GameManager gameManager = null;
    

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
            if(stateKey == PLAY_TIME_STATE) {
                resolvedData = String.valueOf(Math.max(Long.parseLong(new String(localData, "UTF-8")), Long.parseLong(new String(serverData, "UTF-8"))))
                        .getBytes();
            }
            else if(stateKey == GAMES_PLAYED_STATE) {
                resolvedData = String.valueOf(Math.max(Long.parseLong(new String(localData, "UTF-8")), Long.parseLong(new String(serverData, "UTF-8"))))
                        .getBytes();
            }
            else if(stateKey == GAMES_WON_STATE) {
                resolvedData = String.valueOf(Math.max(Long.parseLong(new String(localData, "UTF-8")), Long.parseLong(new String(serverData, "UTF-8"))))
                        .getBytes();
            }
        }
        catch(UnsupportedEncodingException e) {}

        getAppStateClient().resolveState(this, stateKey, ver, resolvedData);
    }

    @Override
    public void onStateLoaded(int statusCode, int stateKey, byte[] buffer) {
        if(statusCode == AppStateClient.STATUS_OK) {
            try {
                if(stateKey == PLAY_TIME_STATE) {
                    String s = new String(buffer, "UTF-8");
                    Stats.setTimePlayed(this, Long.parseLong(s));
                }
                else if(stateKey == GAMES_PLAYED_STATE) {
                    String s = new String(buffer, "UTF-8");
                    Stats.setGamesPlayed(this, Long.parseLong(s));
                }
                else if(stateKey == GAMES_WON_STATE) {
                    String s = new String(buffer, "UTF-8");
                    Stats.setGamesWon(this, Long.parseLong(s));
                }
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
        setContentView(R.layout.main);

        //mHexRealTimeMessageReceivedListener = new HexRealTimeMessageReceivedListener();
        //mHexRoomStatusUpdateListener = new HexRoomStatusUpdateListener();
        //mHexRoomUpdateListener = new HexRoomUpdateListener(this);

        mMainFragment = new MainFragment();
        swapFragment(mMainFragment);

        popupRatingDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()) {
        case android.R.id.home:
            getSupportFragmentManager().popBackStack(mMainFragment.toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            swapFragment(mMainFragment);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mActiveFragment != mMainFragment) {
                getSupportFragmentManager().popBackStack(mMainFragment.toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                swapFragment(mMainFragment);
            }
            else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSignInSucceeded() {
        System.out.println("Signed in");
        mIsSignedIn = true;
        getAppStateClient().loadState(this, PLAY_TIME_STATE);
        getAppStateClient().loadState(this, GAMES_PLAYED_STATE);
        getAppStateClient().loadState(this, GAMES_WON_STATE);
        mMainFragment.setSignedIn(mIsSignedIn);
    }

    @Override
    public void onSignInFailed() {
        System.out.println("Sign in failed");
        mIsSignedIn = false;
        mMainFragment.setSignedIn(mIsSignedIn);
    }

    public void swapFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, newFragment).addToBackStack(newFragment.toString()).commit();
        mActiveFragment = newFragment;
    }

    public void swapFragmentWithoutBackStack(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, newFragment).commit();
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
/*
    public HexRealTimeMessageReceivedListener getHexRealTimeMessageReceivedListener() {
        return mHexRealTimeMessageReceivedListener;
    }

    public void setHexRealTimeMessageReceivedListener(HexRealTimeMessageReceivedListener mHexRealTimeMessageReceivedListener) {
        this.mHexRealTimeMessageReceivedListener = mHexRealTimeMessageReceivedListener;
    }

    public HexRoomStatusUpdateListener getHexRoomStatusUpdateListener() {
        return mHexRoomStatusUpdateListener;
    }

    public void setHexRoomStatusUpdateListener(HexRoomStatusUpdateListener mHexRoomStatusUpdateListener) {
        this.mHexRoomStatusUpdateListener = mHexRoomStatusUpdateListener;
    }

    public HexRoomUpdateListener getHexRoomUpdateListener() {
        return mHexRoomUpdateListener;
    }

    public void setHexRoomUpdateListener(HexRoomUpdateListener mHexRoomUpdateListener) {
        this.mHexRoomUpdateListener = mHexRoomUpdateListener;
    }
 */
    private void popupRatingDialog() {
        // Popup asking to rate app after countdown
        int numTimesAppOpened = PreferenceManager.getDefaultSharedPreferences(this).getInt("num_times_app_opened_review", 0);
        if(numTimesAppOpened != -1) {
            numTimesAppOpened++;
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("num_times_app_opened_review", numTimesAppOpened).commit();
            if(numTimesAppOpened > 2) {
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
                        .setPositiveButton(R.string.review_popup_ok, dialogClickListener).setNegativeButton(R.string.review_popup_never, dialogClickListener)
                        .show();
            }
        }
    }

	/**
	 * @return the gameManager
	 */
	public GameManager getGameManager() {
		return gameManager= new GameManager(this.mHelper,this);
	}

	/**
	 * @param gameManager the gameManager to set
	 */
	
}
