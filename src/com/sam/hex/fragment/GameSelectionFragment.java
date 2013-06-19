package com.sam.hex.fragment;

import android.os.Bundle;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_game_selection, null);

        SelectorLayout selectorLayout = (SelectorLayout) v.findViewById(R.id.buttons);

        SelectorLayout.Button computerButton = selectorLayout.getButtons()[0];
        computerButton.setColor(0xffb7cf47);
        computerButton.setText(R.string.game_selection_button_computer);
        computerButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().setGameFragment(new GameFragment());
                getMainActivity().getGameFragment().setPlayer1Type(Player.Human);
                getMainActivity().getGameFragment().setPlayer2Type(Player.AI);
                getMainActivity().swapFragment(getMainActivity().getGameFragment());
            }
        });

        SelectorLayout.Button hotseatButton = selectorLayout.getButtons()[1];
        hotseatButton.setColor(0xff4ba5e2);
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

        SelectorLayout.Button netButton = selectorLayout.getButtons()[2];
        netButton.setColor(0xffcc5c57);
        netButton.setText(R.string.game_selection_button_net);
        netButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                if(getMainActivity().isSignedIn()) {
                    getMainActivity().setOnlineSelectionFragment(new OnlineSelectionFragment());
                    getMainActivity().swapFragment(getMainActivity().getOnlineSelectionFragment());
                }
                else {
                    getMainActivity().setOpenOnlineSelectionFragment(true);
                    getMainActivity().beginUserInitiatedSignIn();
                }
            }
        });

        return v;
    }
}
