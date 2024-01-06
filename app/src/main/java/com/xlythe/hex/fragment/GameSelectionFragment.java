package com.xlythe.hex.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hex.core.Player;
import com.xlythe.hex.R;
import com.xlythe.hex.view.SelectorLayout;

import androidx.annotation.NonNull;

/**
 * @author Will Harmon
 **/
public class GameSelectionFragment extends HexFragment {
    private SelectorLayout mSelectorLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_game_selection, container, false);

        mSelectorLayout = v.findViewById(R.id.buttons);

        SelectorLayout.Button computerButton = mSelectorLayout.getButtons()[0];
        computerButton.setColor(getResources().getColor(R.color.select_computer));
        computerButton.setText(R.string.game_selection_button_computer);
        computerButton.setOnClickListener(() -> {
            GameFragment gameFragment = new GameFragment();
            if (Math.random() > 0.5) {
                gameFragment.setPlayer1Type(Player.Human);
                gameFragment.setPlayer2Type(Player.AI);
            } else {
                gameFragment.setPlayer1Type(Player.AI);
                gameFragment.setPlayer2Type(Player.Human);
            }
            swapFragment(gameFragment);
        });

        SelectorLayout.Button hotseatButton = mSelectorLayout.getButtons()[1];
        hotseatButton.setColor(getResources().getColor(R.color.select_pass_to_play));
        hotseatButton.setText(R.string.game_selection_button_pass);
        hotseatButton.setOnClickListener(() -> {
            GameFragment gameFragment = new GameFragment();
            gameFragment.setPlayer1Type(Player.Human);
            gameFragment.setPlayer2Type(Player.Human);
            swapFragment(gameFragment);
        });

        SelectorLayout.Button netButton = mSelectorLayout.getButtons()[2];
        netButton.setColor(getResources().getColor(R.color.select_online));
        netButton.setText(R.string.game_selection_button_net);
        netButton.setOnClickListener(() -> {
            if (getMainActivity().isSignedIn()) {
                swapFragment(new OnlineSelectionFragment());
            } else {
                getMainActivity().setOpenOnlineSelectionFragment(true);
                signIn();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSelectorLayout.reset();
    }
}
