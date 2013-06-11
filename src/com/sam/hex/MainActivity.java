package com.sam.hex;

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
import com.sam.hex.fragment.GameFragment;
import com.sam.hex.fragment.GameSelectionFragment;
import com.sam.hex.fragment.HistoryFragment;
import com.sam.hex.fragment.InstructionsFragment;
import com.sam.hex.fragment.MainFragment;
import com.sam.hex.fragment.OnlineSelectionFragment;

/**
 * @author Will Harmon
 **/
public class MainActivity extends BaseGameActivity {
    public static final int REQUEST_ACHIEVEMENTS = 1001;
    public final static int RC_SELECT_PLAYERS = 1002;
    public final static int RC_WAITING_ROOM = 1003;

    // Play variables
    private boolean mIsSignedIn = false;
    private HexRealTimeMessageReceivedListener mHexRealTimeMessageReceivedListener;
    private HexRoomStatusUpdateListener mHexRoomStatusUpdateListener;
    private HexRoomUpdateListener mHexRoomUpdateListener;

    // Fragments
    private MainFragment mMainFragment;
    private GameFragment mGameFragment;
    private GameSelectionFragment mGameSelectionFragment;
    private HistoryFragment mHistoryFragment;
    private InstructionsFragment mInstructionsFragment;
    private OnlineSelectionFragment mOnlineSelectionFragment;
    private Fragment mActiveFragment;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mHexRealTimeMessageReceivedListener = new HexRealTimeMessageReceivedListener();
        mHexRoomStatusUpdateListener = new HexRoomStatusUpdateListener();
        mHexRoomUpdateListener = new HexRoomUpdateListener(this);

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
}
