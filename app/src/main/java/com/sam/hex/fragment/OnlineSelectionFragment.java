package com.sam.hex.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.games.Games;
import com.sam.hex.MainActivity;
import com.sam.hex.R;
import com.sam.hex.view.SelectorLayout;

/**
 * @author Will Harmon
 **/
public class OnlineSelectionFragment extends HexFragment {
    private SelectorLayout mSelectorLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_online_selection, null);

        mSelectorLayout = (SelectorLayout) v.findViewById(R.id.buttons);

        SelectorLayout.Button quickGameButton = mSelectorLayout.getButtons()[0];
        quickGameButton.setColor(getResources().getColor(R.color.select_quick_game));
        quickGameButton.setText(R.string.online_selection_button_quick);
        quickGameButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                try {
                    // Network is async, no promise that we won't lose
                    // connectivity
                    getMainActivity().startQuickGame();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });

        SelectorLayout.Button inviteButton = mSelectorLayout.getButtons()[1];
        inviteButton.setColor(getResources().getColor(R.color.select_friends));
        inviteButton.setText(R.string.online_selection_button_invite);
        inviteButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                try {
                    // Network is async, no promise that we won't lose
                    // connectivity
                    getMainActivity().startActivityForResult(Games.TurnBasedMultiplayer.getSelectOpponentsIntent(getMainActivity().getClient(), 1, 1), MainActivity.RC_SELECT_PLAYERS);
                    getMainActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });

        SelectorLayout.Button pendingButton = mSelectorLayout.getButtons()[2];
        pendingButton.setColor(getResources().getColor(R.color.select_pending_invites));
        pendingButton.setText(R.string.online_selection_button_pending);
        pendingButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                try {
                    // Network is async, no promise that we won't lose
                    // connectivity
                    getMainActivity().startActivityForResult(Games.TurnBasedMultiplayer.getInboxIntent(getMainActivity().getClient()), MainActivity.RC_INVITATION_INBOX);
                    getMainActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
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