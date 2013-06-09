package com.sam.hex.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.sam.hex.MainActivity;
import com.sam.hex.R;
import com.sam.hex.view.SelectorLayout;

public class GameSelectionFragment extends SherlockFragment {
    SelectorLayout.Button mNetButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getSherlockActivity().getSupportActionBar().hide();
        View v = inflater.inflate(R.layout.game_selection, null);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        SelectorLayout selectorLayout = (SelectorLayout) v.findViewById(R.id.buttons);

        SelectorLayout.Button computerButton = selectorLayout.getButtons()[0];
        computerButton.setColor(0xffb7cf47);
        computerButton.setText(R.string.game_selection_button_computer);
        computerButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                prefs.edit().putString("player1Type", "0").apply();
                prefs.edit().putString("player2Type", "4").apply();

                getMainActivity().setGameFragment(new GameFragment());
                getMainActivity().swapFragmentWithoutBackStack(getMainActivity().getGameFragment());
            }
        });

        SelectorLayout.Button hotseatButton = selectorLayout.getButtons()[1];
        hotseatButton.setColor(0xff4ba5e2);
        hotseatButton.setText(R.string.game_selection_button_pass);
        hotseatButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                prefs.edit().putString("player1Type", "0").apply();
                prefs.edit().putString("player2Type", "0").apply();

                getMainActivity().setGameFragment(new GameFragment());
                getMainActivity().swapFragmentWithoutBackStack(getMainActivity().getGameFragment());
            }
        });

        mNetButton = selectorLayout.getButtons()[2];
        mNetButton.setColor(0xffcc5c57);
        mNetButton.setText(R.string.game_selection_button_net);
        mNetButton.setEnabled(getMainActivity().isSignedIn());
        mNetButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().setOnlineSelectionFragment(new OnlineSelectionFragment());
                getMainActivity().swapFragmentWithoutBackStack(getMainActivity().getOnlineSelectionFragment());
            }
        });

        return v;
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getSherlockActivity();
    }
}
