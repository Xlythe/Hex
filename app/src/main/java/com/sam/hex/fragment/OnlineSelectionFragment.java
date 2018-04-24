package com.sam.hex.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sam.hex.R;
import com.sam.hex.view.SelectorLayout;

/**
 * @author Will Harmon
 **/
public class OnlineSelectionFragment extends HexFragment {
    private SelectorLayout mSelectorLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_online_selection, container, false);

        mSelectorLayout = v.findViewById(R.id.buttons);

        SelectorLayout.Button quickGameButton = mSelectorLayout.getButtons()[0];
        quickGameButton.setColor(getResources().getColor(R.color.select_quick_game));
        quickGameButton.setText(R.string.online_selection_button_quick);
        quickGameButton.setOnClickListener(this::startQuickGame);

        SelectorLayout.Button inviteButton = mSelectorLayout.getButtons()[1];
        inviteButton.setColor(getResources().getColor(R.color.select_friends));
        inviteButton.setText(R.string.online_selection_button_invite);
        inviteButton.setOnClickListener(this::inviteFriends);

        SelectorLayout.Button pendingButton = mSelectorLayout.getButtons()[2];
        pendingButton.setColor(getResources().getColor(R.color.select_pending_invites));
        pendingButton.setText(R.string.online_selection_button_pending);
        pendingButton.setOnClickListener(this::checkInvites);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSelectorLayout.reset();
    }
}