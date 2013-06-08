package com.sam.hex.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.sam.hex.MainActivity;
import com.sam.hex.R;

public class OnlineSelectionFragment extends SherlockFragment {
    private String mRoomId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getSherlockActivity().getSupportActionBar().show();
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View v = inflater.inflate(R.layout.online_selection, null);

        Button quickGameButton = (Button) v.findViewById(R.id.button1);
        quickGameButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startQuickGame();
            }
        });

        Button inviteButton = (Button) v.findViewById(R.id.button2);
        inviteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivityForResult(getMainActivity().getGamesClient().getSelectPlayersIntent(1, 1), MainActivity.RC_SELECT_PLAYERS);
            }
        });

        Button pendingButton = (Button) v.findViewById(R.id.button3);
        pendingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivityForResult(getMainActivity().getGamesClient().getInvitationInboxIntent(), MainActivity.RC_SELECT_PLAYERS);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int request, int response, Intent intent) {
        if(request == MainActivity.RC_SELECT_PLAYERS) {
            if(response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // get the invitee list
            final ArrayList<String> invitees = intent.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);

            // get automatch criteria
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers = intent.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = intent.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if(minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            }
            else {
                autoMatchCriteria = null;
            }

            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            if(autoMatchCriteria != null) {
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            }
            RoomConfig roomConfig = roomConfigBuilder.build();
            getMainActivity().getGamesClient().createRoom(roomConfig);

            // prevent screen from sleeping during handshake
            getSherlockActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else if(request == MainActivity.RC_WAITING_ROOM) {
            if(response == Activity.RESULT_OK) {
                // (start game)
            }
            else if(response == Activity.RESULT_CANCELED || response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                getMainActivity().getGamesClient().leaveRoom(getMainActivity().getHexRoomUpdateListener(), mRoomId);
                getSherlockActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    private void startQuickGame() {
        // automatch criteria to invite 1 random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        getMainActivity().getGamesClient().createRoom(roomConfig);

        // prevent screen from sleeping during handshake
        getSherlockActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen
    }

    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(getMainActivity().getHexRoomUpdateListener())
                .setMessageReceivedListener(getMainActivity().getHexRealTimeMessageReceivedListener())
                .setRoomStatusUpdateListener(getMainActivity().getHexRoomStatusUpdateListener());
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getSherlockActivity();
    }
}
