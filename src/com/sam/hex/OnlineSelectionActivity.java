package com.sam.hex;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

public class OnlineSelectionActivity extends BaseGameActivity implements RoomUpdateListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener {
    private final static int RC_SELECT_PLAYERS = 1001;
    private final static int RC_WAITING_ROOM = 1002;

    private String mRoomId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.online_selection);

        Button quickGameButton = (Button) findViewById(R.id.button1);
        quickGameButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startQuickGame();
            }
        });

        Button inviteButton = (Button) findViewById(R.id.button2);
        inviteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = getGamesClient().getSelectPlayersIntent(1, 1);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
            }
        });

        Button pendingButton = (Button) findViewById(R.id.button3);
        pendingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int request, int response, Intent intent) {
        if(request == RC_SELECT_PLAYERS) {
            if(response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // get the invitee list
            Bundle extras = intent.getExtras();
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
            getGamesClient().createRoom(roomConfig);

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else if(request == RC_WAITING_ROOM) {
            if(response == Activity.RESULT_OK) {
                // (start game)
            }
            else if(response == Activity.RESULT_CANCELED) {
                // Waiting room was dismissed with the back button. The meaning
                // of this
                // action is up to the game. You may choose to leave the room
                // and cancel the
                // match, or do something else like minimize the waiting room
                // and
                // continue to connect in the background.

                // in this example, we take the simple approach and just leave
                // the room:
                getGamesClient().leaveRoom(this, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else if(response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                getGamesClient().leaveRoom(this, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    @Override
    public void onSignInSucceeded() {}

    @Override
    public void onSignInFailed() {}

    private void startQuickGame() {
        // automatch criteria to invite 1 random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        getGamesClient().createRoom(roomConfig);

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen
    }

    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this).setMessageReceivedListener(this).setRoomStatusUpdateListener(this);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if(statusCode != GamesClient.STATUS_OK) {
            // display error
            return;
        }

        mRoomId = room.getRoomId();

        // get waiting room intent
        Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, 1);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onLeftRoom(int arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomConnected(int arg0, Room arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if(statusCode != GamesClient.STATUS_OK) {
            // display error
            return;
        }

        mRoomId = room.getRoomId();

        // get waiting room intent
        Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, Integer.MAX_VALUE);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectedToRoom(Room arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDisconnectedFromRoom(Room arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerDeclined(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerInvitedToRoom(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerJoined(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerLeft(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeersConnected(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeersDisconnected(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomAutoMatching(Room arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomConnecting(Room arg0) {
        // TODO Auto-generated method stub

    }
}
