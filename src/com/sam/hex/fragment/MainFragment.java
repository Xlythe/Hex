package com.sam.hex.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.sam.hex.view.HexDialog;
import com.sam.hex.view.HexagonLayout;

/**
 * @author Will Harmon
 **/
public class MainFragment extends Fragment {
    // Hexagon variables
    HexagonLayout.Button mAchievementsButton;
    HexagonLayout.Button mDonateButton;

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

        HexagonLayout hexagonLayout = (HexagonLayout) v.findViewById(R.id.hexagonButtons);
        HexagonLayout.Button settingsButton = hexagonLayout.getButtons()[0];
        mDonateButton = hexagonLayout.getButtons()[1];
        HexagonLayout.Button historyButton = hexagonLayout.getButtons()[2];
        HexagonLayout.Button instructionsButton = hexagonLayout.getButtons()[3];
        mAchievementsButton = hexagonLayout.getButtons()[4];
        HexagonLayout.Button playButton = hexagonLayout.getButtons()[5];

        hexagonLayout.setTopMargin(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics()));

        mTitleTextView = (TextView) v.findViewById(R.id.title);
        mTimePlayedTextView = (TextView) v.findViewById(R.id.timePlayed);
        mGamesPlayedTextView = (TextView) v.findViewById(R.id.gamesPlayed);
        mGamesWonTextView = (TextView) v.findViewById(R.id.gamesWon);

        mSignInButton = (SignInButton) v.findViewById(R.id.signInButton);
        mSignOutButton = (Button) v.findViewById(R.id.signOutButton);

        hexagonLayout.setText(R.string.app_name);

        settingsButton.setText(R.string.main_button_settings);
        settingsButton.setColor(0xffcc5c57);
        settingsButton.setDrawableResource(R.drawable.settings);
        settingsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivity(new Intent(getMainActivity(), PreferencesActivity.class));
            }
        });

        mDonateButton.setText(R.string.main_button_donate);
        mDonateButton.setColor(0xff5f6ec2);
        mDonateButton.setDrawableResource(R.drawable.store);
        mDonateButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                HexDialog hd = new HexDialog(getMainActivity());
                hd.setPositiveButton("yay", getResources().getDrawable(R.drawable.play_again), null);
                hd.setNegativeButton(null, getResources().getDrawable(R.drawable.home), null);
                hd.setNeutralButton("lol", null, null);
                hd.show();
                // getMainActivity().purchaseItem(MainActivity.ITEM_SKU_INTERMEDIATE);
            }
        });

        historyButton.setText(R.string.main_button_history);
        historyButton.setColor(0xfff9db00);
        historyButton.setDrawableResource(R.drawable.history);
        historyButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().swapFragment(new HistoryFragment());
            }
        });

        instructionsButton.setText(R.string.main_button_instructions);
        instructionsButton.setColor(0xffb7cf47);
        instructionsButton.setDrawableResource(R.drawable.howtoplay);
        instructionsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().swapFragment(new InstructionsFragment());
            }
        });

        mAchievementsButton.setText(R.string.main_button_achievements);
        mAchievementsButton.setColor(0xfff48935);
        mAchievementsButton.setDrawableResource(R.drawable.achievements);
        mAchievementsButton.setOnClickListener(new HexagonLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                startActivityForResult(getMainActivity().getGamesClient().getAchievementsIntent(), MainActivity.REQUEST_ACHIEVEMENTS);
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
        if(mSignOutButton != null) mSignOutButton.setVisibility(getMainActivity().isSignedIn() ? View.VISIBLE : View.GONE);
        if(mSignInButton != null) mSignInButton.setVisibility(getMainActivity().isSignedIn() ? View.GONE : View.VISIBLE);
        if(mTitleTextView != null) mTitleTextView.setText(String.format(getString(R.string.main_title),
                Settings.getPlayer1Name(getMainActivity(), getMainActivity().getGamesClient())));
        if(mAchievementsButton != null) mAchievementsButton.setEnabled(getMainActivity().isSignedIn());
        if(mDonateButton != null) mDonateButton.setEnabled(getMainActivity().isIabSetup());
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    public void setSignedIn(boolean isSignedIn) {
        refreshPlayerInformation();
    }

    public void setIabSetup(boolean isIabSetup) {
        refreshPlayerInformation();
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
