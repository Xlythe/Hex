package com.sam.hex.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.sam.hex.PreferencesActivity;
import com.sam.hex.R;
import com.sam.hex.Settings;
import com.sam.hex.Stats;
import com.sam.hex.view.DonateDialog;
import com.sam.hex.view.HexagonLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Will Harmon
 **/
public class MainFragment extends HexFragment {
    // Hexagon variables
    private HexagonLayout mHexagonLayout;
    private float mInitialSpin;
    private float mInitialRotation;

    // Stat variables
    private TextView mTitleTextView;
    private TextView mTimePlayedTextView;
    private TextView mGamesPlayedTextView;
    private TextView mGamesWonTextView;

    // Play variables
    private SignInButton mSignInButton;
    private Button mSignOutButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        keepScreenOn(false);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mHexagonLayout = view.findViewById(R.id.hexagonButtons);
        HexagonLayout.Button settingsButton = mHexagonLayout.getButtons()[0];
        HexagonLayout.Button donateButton = mHexagonLayout.getButtons()[1];
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

        mTitleTextView = view.findViewById(R.id.title);
        mTimePlayedTextView = view.findViewById(R.id.timePlayed);
        mGamesPlayedTextView = view.findViewById(R.id.gamesPlayed);
        mGamesWonTextView = view.findViewById(R.id.gamesWon);

        mSignInButton = view.findViewById(R.id.signInButton);
        mSignOutButton = view.findViewById(R.id.signOutButton);

        settingsButton.setText(R.string.main_button_settings);
        settingsButton.setColor(getResources().getColor(R.color.main_settings));
        settingsButton.setDrawableResource(R.drawable.settings);
        settingsButton.setOnClickListener(() -> {
            startActivity(new Intent(getMainActivity(), PreferencesActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        donateButton.setText(R.string.main_button_donate);
        donateButton.setColor(getResources().getColor(R.color.main_donate));
        donateButton.setDrawableResource(R.drawable.store);
        donateButton.setOnClickListener(() -> new DonateDialog.Builder(getMainActivity()).show());

        historyButton.setText(R.string.main_button_history);
        historyButton.setColor(getResources().getColor(R.color.main_history));
        historyButton.setDrawableResource(R.drawable.history);
        historyButton.setOnClickListener(() -> swapFragment(new HistoryFragment()));

        instructionsButton.setText(R.string.main_button_instructions);
        instructionsButton.setColor(getResources().getColor(R.color.main_instructions));
        instructionsButton.setDrawableResource(R.drawable.howtoplay);
        instructionsButton.setOnClickListener(() -> swapFragment(new InstructionsFragment()));

        achievementsButton.setText(R.string.main_button_achievements);
        achievementsButton.setColor(getResources().getColor(R.color.main_achievements));
        achievementsButton.setDrawableResource(R.drawable.achievements);
        achievementsButton.setOnClickListener(() -> {
            if (isSignedIn()) {
                openAchievements();
            } else {
                getMainActivity().setOpenAchievements(true);
                signIn();
            }
        });

        playButton.setText(R.string.main_button_play);
        playButton.setColor(getResources().getColor(R.color.main_play));
        playButton.setDrawableResource(R.drawable.play);
        playButton.setOnClickListener(() -> swapFragment(new GameSelectionFragment()));

        mSignInButton.setOnClickListener(v -> signIn());

        mSignOutButton.setOnClickListener(v -> {
            signOut();
            refreshPlayerInformation();
        });
        refreshPlayerInformation();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        showStats();
        showDonationStar();
    }

    private void showStats() {
        long timePlayedInMillis = Stats.getTimePlayed(getMainActivity());
        long timePlayedInHours = timePlayedInMillis / (1000 * 60 * 60);
        long timePlayedInMintues = (timePlayedInMillis - timePlayedInHours * (1000 * 60 * 60)) / (1000 * 60);
        long timePlayedInSeconds = (timePlayedInMillis - timePlayedInHours * (1000 * 60 * 60) - timePlayedInMintues * (1000 * 60)) / (1000);
        mTimePlayedTextView.setText(getString(R.string.main_stats_time_played, timePlayedInHours, timePlayedInMintues, timePlayedInSeconds));
        mGamesPlayedTextView.setText(getString(R.string.main_stats_games_played, Stats.getGamesPlayed(getMainActivity())));
        mGamesWonTextView.setText(getString(R.string.main_stats_games_won, Stats.getGamesWon(getMainActivity())));
    }

    private void refreshPlayerInformation() {
        try {
            // Network is async, no promise that we won't lose connectivity
            if (getMainActivity() == null) return;
            if (mSignOutButton != null)
                mSignOutButton.setVisibility(isSignedIn() ? View.VISIBLE : View.GONE);
            if (mSignInButton != null)
                mSignInButton.setVisibility(isSignedIn() ? View.GONE : View.VISIBLE);
            if (mTitleTextView != null)
                mTitleTextView.setText(getString(R.string.main_title,
                        Settings.getPlayer1Name(getMainActivity(), getGoogleSignInAccount())));
            if (mHexagonLayout != null) mHexagonLayout.invalidate();
            if (mTimePlayedTextView != null && mGamesPlayedTextView != null && mGamesWonTextView != null)
                showStats();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void setSignedIn(boolean isSignedIn) {
        refreshPlayerInformation();
    }

    public void setInitialSpin(float initialSpin) {
        mInitialSpin = initialSpin;
    }

    public void setInitialRotation(float initialRotation) {
        mInitialRotation = initialRotation;
    }

    private void showDonationStar() {
        int donationAmount = Stats.getDonationRank(getMainActivity());
        @DrawableRes int resource = R.drawable.donate_hollow;

        if (donationAmount >= 5) {
            resource = R.drawable.donate_gold;
        } else if (donationAmount >= 3) {
            resource = R.drawable.donate_silver;
        } else if (donationAmount >= 1) {
            resource = R.drawable.donate_bronze;
        }

        mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(resource, 0, 0, 0);
    }
}
