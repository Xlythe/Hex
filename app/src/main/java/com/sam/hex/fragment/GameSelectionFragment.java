package com.sam.hex.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hex.core.Player;
import com.sam.hex.R;
import com.sam.hex.view.SelectorLayout;

/**
 * @author Will Harmon
 **/
public class GameSelectionFragment extends HexFragment {
    private SelectorLayout mSelectorLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_game_selection, null);

        mSelectorLayout = (SelectorLayout) v.findViewById(R.id.buttons);

        SelectorLayout.Button computerButton = mSelectorLayout.getButtons()[0];
        computerButton.setColor(getResources().getColor(R.color.select_computer));
        computerButton.setText(R.string.game_selection_button_computer);
        computerButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().setGameFragment(new GameFragment());
                if (Math.random() > 0.5) {
                    getMainActivity().getGameFragment().setPlayer1Type(Player.Human);
                    getMainActivity().getGameFragment().setPlayer2Type(Player.AI);
                } else {
                    getMainActivity().getGameFragment().setPlayer1Type(Player.AI);
                    getMainActivity().getGameFragment().setPlayer2Type(Player.Human);
                }
                getMainActivity().swapFragment(getMainActivity().getGameFragment());
            }
        });

        SelectorLayout.Button hotseatButton = mSelectorLayout.getButtons()[1];
        hotseatButton.setColor(getResources().getColor(R.color.select_pass_to_play));
        hotseatButton.setText(R.string.game_selection_button_pass);
        hotseatButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().setGameFragment(new GameFragment());
                getMainActivity().getGameFragment().setPlayer1Type(Player.Human);
                getMainActivity().getGameFragment().setPlayer2Type(Player.Human);
                getMainActivity().swapFragment(getMainActivity().getGameFragment());
            }
        });

        SelectorLayout.Button netButton = mSelectorLayout.getButtons()[2];
        netButton.setColor(getResources().getColor(R.color.select_online));
        netButton.setText(R.string.game_selection_button_net);
        netButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                if (getMainActivity().isSignedIn()) {
                    getMainActivity().setOnlineSelectionFragment(new OnlineSelectionFragment());
                    getMainActivity().swapFragment(getMainActivity().getOnlineSelectionFragment());
                } else {
                    getMainActivity().setOpenOnlineSelectionFragment(true);
                    getMainActivity().beginUserInitiatedSignIn();
                }
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
