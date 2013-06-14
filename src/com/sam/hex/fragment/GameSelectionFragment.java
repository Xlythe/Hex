package com.sam.hex.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hex.core.Player;
import com.sam.hex.MainActivity;
import com.sam.hex.R;
import com.sam.hex.view.SelectorLayout;

/**
 * @author Will Harmon
 **/
public class GameSelectionFragment extends Fragment {
    SelectorLayout.Button mNetButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.game_selection, null);

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

        mNetButton = selectorLayout.getButtons()[2];
        mNetButton.setColor(0xffcc5c57);
        mNetButton.setText(R.string.game_selection_button_net);
        mNetButton.setEnabled(getMainActivity().isSignedIn());
        mNetButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().setOnlineSelectionFragment(new OnlineSelectionFragment());
                getMainActivity().swapFragment(getMainActivity().getOnlineSelectionFragment());
            }
        });

        return v;
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
