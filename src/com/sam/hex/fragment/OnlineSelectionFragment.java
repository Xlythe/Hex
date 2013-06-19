package com.sam.hex.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sam.hex.MainActivity;
import com.sam.hex.R;
import com.sam.hex.view.SelectorLayout;

/**
 * @author Will Harmon
 **/
public class OnlineSelectionFragment extends HexFragment {
    private SelectorLayout mSelectorLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_online_selection, null);

        mSelectorLayout = (SelectorLayout) v.findViewById(R.id.buttons);

        SelectorLayout.Button quickGameButton = mSelectorLayout.getButtons()[0];
        quickGameButton.setColor(0xfff9db00);
        quickGameButton.setText(R.string.online_selection_button_quick);
        quickGameButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                getMainActivity().startQuickGame();
            }
        });

        SelectorLayout.Button inviteButton = mSelectorLayout.getButtons()[1];
        inviteButton.setColor(0xff5f6ec2);
        inviteButton.setText(R.string.online_selection_button_invite);
        inviteButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                System.out.println("code:" + MainActivity.RC_SELECT_PLAYERS);
                getMainActivity().startActivityForResult(getMainActivity().getGamesClient().getSelectPlayersIntent(1, 1), MainActivity.RC_SELECT_PLAYERS);
                getMainActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        SelectorLayout.Button pendingButton = mSelectorLayout.getButtons()[2];
        pendingButton.setColor(0xfff48935);
        pendingButton.setText(R.string.online_selection_button_pending);
        pendingButton.setOnClickListener(new SelectorLayout.Button.OnClickListener() {
            @Override
            public void onClick() {
                System.out.println("code:" + MainActivity.RC_INVITATION_INBOX);
                getMainActivity().startActivityForResult(getMainActivity().getGamesClient().getInvitationInboxIntent(), MainActivity.RC_INVITATION_INBOX);
                getMainActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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