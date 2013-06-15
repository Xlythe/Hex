package com.sam.hex.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.sam.hex.MainActivity;
import com.sam.hex.PreferencesActivity;
import com.sam.hex.R;
import com.sam.hex.Settings;
import com.sam.hex.Stats;
import com.sam.hex.view.DonateDialog;
import com.sam.hex.view.HexDialog;
import com.sam.hex.view.HexagonLayout;

/**
 * @author Will Harmon
 **/
public class MainFragment extends HexFragment {
    // Hexagon variables
    HexagonLayout mHexagonLayout;
    HexagonLayout.Button mDonateButton;
    private float mInitialSpin;
    private float mInitialRotation;

    // Stat variables
    TextView mTitleTextView;
    TextView mTimePlayedTextView;
    TextView mGamesPlayedTextView;
    TextView mGamesWonTextView;

    // Play variables
    SignInButton mSignInButton;
    Button mSignOutButton;

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getMainActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View v = inflater.inflate(R.layout.home, null);

        mHexagonLayout = (HexagonLayout) v.findViewById(R.id.hexagonButtons);
        HexagonLayout.Button settingsButton = mHexagonLayout.getButtons()[0];
        mDonateButton = mHexagonLayout.getButtons()[1];
        HexagonLayout.Button historyButton = mHexagonLayout.getButtons()[2];
        HexagonLayout.Button instructionsButton = mHexagonLayout.getButtons()[3];
        HexagonLayout.Button achievementsButton = mHexagonLayout.getButtons()[4];
        HexagonLayout.Button playButton = mHexagonLayout.getButtons()[5];

        mHexagonLayout.setTopMargin(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics()));
        mHexagonLayout.setText(R.string.app_name);
        mHexagonLayout.setInitialRotation(mInitialRotation);
        mInitialRotation = 0f;
        mHexagonLayout.setInitialSpin(mInitialSpin);
        mInitialSpin = 0f;

        mTitleTextView = (TextView) v.findViewById(R.id.title);
        mTimePlayedTextView = (TextView) v.findViewById(R.id.timePlayed);
        mGamesPlayedTextView = (TextView) v.findViewById(R.id.gamesPlayed);
        mGamesWonTextView = (TextView) v.findViewById(R.id.gamesWon);

        mSignInButton = (SignInButton) v.findViewById(R.id.signInButton);
        mSignOutButton = (Button) v.findViewById(R.id.signOutButton);

        settingsButton.setText(R.string.main_button_settings);
        settingsButton.setColor(0xffcc5c57);
        settingsButton.setDrawableResource(R.drawable.settings);
        settingsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(getMainActivity(), PreferencesActivity.class));
                getMainActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        mDonateButton.setText(R.string.main_button_donate);
        mDonateButton.setColor(0xff5f6ec2);
        mDonateButton.setDrawableResource(R.drawable.store);
        mDonateButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                HexDialog hd = new DonateDialog(getMainActivity());
                hd.show();
            }
        });

        historyButton.setText(R.string.main_button_history);
        historyButton.setColor(0xfff9db00);
        historyButton.setDrawableResource(R.drawable.history);
        historyButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().setHistoryFragment(new HistoryFragment());
                getMainActivity().swapFragment(getMainActivity().getHistoryFragment());
            }
        });

        instructionsButton.setText(R.string.main_button_instructions);
        instructionsButton.setColor(0xffb7cf47);
        instructionsButton.setDrawableResource(R.drawable.howtoplay);
        instructionsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().setInstructionsFragment(new InstructionsFragment());
                getMainActivity().swapFragment(getMainActivity().getInstructionsFragment());
            }
        });

        achievementsButton.setText(R.string.main_button_achievements);
        achievementsButton.setColor(0xfff48935);
        achievementsButton.setDrawableResource(R.drawable.achievements);
        achievementsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                if(getMainActivity().isSignedIn()) {
                    startActivityForResult(getMainActivity().getGamesClient().getAchievementsIntent(), MainActivity.REQUEST_ACHIEVEMENTS);
                    getMainActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                else {
                    getMainActivity().setOpenAchievements(true);
                    getMainActivity().beginUserInitiatedSignIn();
                }
            }
        });

        playButton.setText(R.string.main_button_play);
        playButton.setColor(0xff4ba5e2);
        playButton.setDrawableResource(R.drawable.play);
        playButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().setGameSelectionFragment(new GameSelectionFragment());
                getMainActivity().swapFragment(getMainActivity().getGameSelectionFragment());
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().beginUserInitiatedSignIn();
            }
        });

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().signOut();
                refreshPlayerInformation();
            }
        });
        refreshPlayerInformation();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        long timePlayedInMillis = Stats.getTimePlayed(getMainActivity());
        long timePlayedInHours = timePlayedInMillis / (1000 * 60 * 60);
        long timePlayedInMintues = (timePlayedInMillis - timePlayedInHours * (1000 * 60 * 60)) / (1000 * 60);
        long timePlayedInSeconds = (timePlayedInMillis - timePlayedInHours * (1000 * 60 * 60) - timePlayedInMintues * (1000 * 60)) / (1000);
        mTimePlayedTextView.setText(String.format(getString(R.string.main_stats_time_played), timePlayedInHours, timePlayedInMintues, timePlayedInSeconds));
        mGamesPlayedTextView.setText(String.format(getString(R.string.main_stats_games_played), Stats.getGamesPlayed(getMainActivity())));
        mGamesWonTextView.setText(String.format(getString(R.string.main_stats_games_won), Stats.getGamesWon(getMainActivity())));
        showDonationStar();
    }

    private void refreshPlayerInformation() {
        if(getMainActivity() == null) return;
        if(mSignOutButton != null) mSignOutButton.setVisibility(getMainActivity().isSignedIn() ? View.VISIBLE : View.GONE);
        if(mSignInButton != null) mSignInButton.setVisibility(getMainActivity().isSignedIn() ? View.GONE : View.VISIBLE);
        if(mTitleTextView != null) mTitleTextView.setText(String.format(getString(R.string.main_title),
                Settings.getPlayer1Name(getMainActivity(), getMainActivity().getGamesClient())));
        if(mDonateButton != null) mDonateButton.setEnabled(getMainActivity().isIabSetup());
        if(mHexagonLayout != null) mHexagonLayout.invalidate();
    }

    public void setSignedIn(boolean isSignedIn) {
        refreshPlayerInformation();
    }

    public void setIabSetup(boolean isIabSetup) {
        refreshPlayerInformation();
    }

    public void setInitialSpin(float initialSpin) {
        mInitialSpin = initialSpin;
    }

    public void setInitialRotation(float initialRotation) {
        mInitialRotation = initialRotation;
    }

    private void showDonationStar() {
        int donationAmount = Stats.getDonationAmount(getMainActivity());
        int resource = R.drawable.donate_hollow;

        if(donationAmount >= 5) {
            resource = R.drawable.donate_gold;
        }
        else if(donationAmount >= 3) {
            resource = R.drawable.donate_silver;
        }
        else if(donationAmount >= 1) {
            resource = R.drawable.donate_bronze;
        }

        mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(resource, 0, 0, 0);
    }
}
