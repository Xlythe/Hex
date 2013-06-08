package com.sam.hex.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.sam.hex.MainActivity;
import com.sam.hex.R;

public class GameSelectionFragment extends SherlockFragment {
    Button mNetButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getSherlockActivity().getSupportActionBar().show();
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View v = inflater.inflate(R.layout.game_selection, null);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Button AI = (Button) v.findViewById(R.id.button1);
        AI.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                prefs.edit().putString("player1Type", "0").apply();
                prefs.edit().putString("player2Type", "4").apply();

                getMainActivity().setGameFragment(new GameFragment());
                getMainActivity().swapFragmentWithoutBackStack(getMainActivity().getGameFragment());
            }
        });

        Button hotseat = (Button) v.findViewById(R.id.button2);
        hotseat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                prefs.edit().putString("player1Type", "0").apply();
                prefs.edit().putString("player2Type", "0").apply();

                getMainActivity().setGameFragment(new GameFragment());
                getMainActivity().swapFragmentWithoutBackStack(getMainActivity().getGameFragment());
            }
        });

        mNetButton = (Button) v.findViewById(R.id.button3);
        mNetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
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
