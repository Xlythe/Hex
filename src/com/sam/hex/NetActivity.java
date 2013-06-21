/* Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sam.hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.hex.core.Game;
import com.hex.core.Game.GameOptions;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Timer;
import com.hex.network.Errors;
import com.hex.network.NetCommunication;
import com.hex.network.NetworkCallbacks;
import com.hex.network.NetworkPlayer;

/**
 * Button Clicker 2000. A minimalistic game showing the multiplayer features of
 * the Google Play game services API. The objective of this game is clicking a
 * button. Whoever clicks the button the most times within a 20 second interval
 * wins. It's that simple. This game can be played with 2, 3 or 4 players. The
 * code is organized in sections in order to make understanding as clear as
 * possible. We start with the integration section where we show how the game is
 * integrated with the Google Play game services API, then move on to
 * game-specific UI and logic. INSTRUCTIONS: To run this sample, please set up a
 * project in the Developer Console. Then, place your app ID on
 * res/values/ids.xml. Also, change the package name to the package name you
 * used to create the client ID in Developer Console. Make sure you sign the APK
 * with the certificate whose fingerprint you entered in Developer Console when
 * creating your Client Id.
 * 
 * @author Bruno Oliveira (btco), 2013-04-26
 */
public abstract class NetActivity extends BaseGameActivity implements RealTimeMessageReceivedListener, RoomStatusUpdateListener, RoomUpdateListener,
        OnInvitationReceivedListener, NetCommunication, NetworkCallbacks, OnCancelListener {

    /*
     * API INTEGRATION SECTION. This section contains the code that integrates
     * the game with the Google Play game services API.
     */

    // Debug tag
    final static String TAG = "Hex";

    // Request codes for the UIs that we show with startActivityForResult:
    public final static int RC_SELECT_PLAYERS = 10000;
    public final static int RC_INVITATION_INBOX = 10001;
    public final static int RC_WAITING_ROOM = 10002;
    public final static int RC_ACHIEVEMENTS = 10003;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // flag indicating whether we're dismissing the waiting room because the
    // game is starting
    boolean mWaitRoomDismissedFromCode = false;

    public NetActivity(int mode) {
        super(mode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Called by the base class (BaseGameActivity) when sign-in has failed. For
     * example, because the user hasn't authenticated yet. We react to this by
     * showing the sign-in button.
     */
    @Override
    public void onSignInFailed() {
        Log.d(TAG, "Sign-in failed.");
    }

    /**
     * Called by the base class (BaseGameActivity) when sign-in succeeded. We
     * react by going to our main screen.
     */
    @Override
    public void onSignInSucceeded() {
        Log.d(TAG, "Sign-in succeeded.");

        // install invitation listener so we get notified if we receive an
        // invitation to play
        // a game.
        getGamesClient().registerInvitationListener(this);

        // if we received an invite via notification, accept it; otherwise, go
        // to main screen
        if(getInvitationId() != null) {
            acceptInviteToRoom(getInvitationId());
            return;
        }
    }

    public void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS, MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        keepScreenOn();
        getGamesClient().createRoom(rtmConfigBuilder.build());
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        switch(requestCode) {
        case RC_SELECT_PLAYERS:
            // we got the result from the "select players" UI -- ready to create
            // the room
            handleSelectPlayersResult(responseCode, intent);
            break;
        case RC_INVITATION_INBOX:
            // we got the result from the "select invitation" UI (invitation
            // inbox). We're
            // ready to accept the selected invitation:
            handleInvitationInboxResult(responseCode, intent);
            break;
        case RC_WAITING_ROOM:
            // ignore result if we dismissed the waiting room from code:
            if(mWaitRoomDismissedFromCode) break;

            // we got the result from the "waiting room" UI.
            if(responseCode == Activity.RESULT_OK) {
                // player wants to start playing
                Log.d(TAG, "Starting game because user requested via waiting room UI.");

                // start the game!
                startGame(false);
            }
            else if(responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player actively indicated that they want to leave the room
                leaveRoom();
            }
            else if(responseCode == Activity.RESULT_CANCELED) {
                /*
                 * Dialog was cancelled (user pressed back key, for instance).
                 * In our game, this means leaving the room too. In more
                 * elaborate games,this could mean something else (like
                 * minimizing the waiting room UI but continue in the handshake
                 * process).
                 */
                leaveRoom();
            }

            break;
        }
    }

    // Handle the result of the "Select players UI" we launched when the user
    // clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if(response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if(minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if(autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        keepScreenOn();
        getGamesClient().createRoom(rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick
    // an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if(response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(GamesClient.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId).setMessageReceivedListener(this).setRoomStatusUpdateListener(this);
        keepScreenOn();
        getGamesClient().joinRoom(roomConfigBuilder.build());
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        super.onStop();
    }

    // Activity just got to the foreground. We switch to the wait screen because
    // we will now
    // go through the sign-in flow (remember that, yes, every time the Activity
    // comes back to the
    // foreground we go through the sign-in flow -- but if the user is already
    // authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {
        super.onStart();
    }

    // Leave the room.
    public void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if(mRoomId != null) {
            getGamesClient().leaveRoom(this, mRoomId);
            mRoomId = null;
        }
    }

    // Show the waiting room UI to track the progress of other players as they
    // enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        mWaitRoomDismissedFromCode = false;

        // minimum number of players required for our game
        final int MIN_PLAYERS = 2;
        Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    // Forcibly dismiss the waiting room UI (this is useful, for example, if we
    // realize the
    // game needs to start because someone else is starting to play).
    void dismissWaitingRoom() {
        mWaitRoomDismissedFromCode = true;
        finishActivity(RC_WAITING_ROOM);
    }

    // Called when we get an invitation to play a game. We react by showing that
    // to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        mIncomingInvitationId = invitation.getInvitationId();

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    acceptInviteToRoom(mIncomingInvitationId);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    // Do nothing
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.net_challeneger_approaches, invitation.getInviter().getDisplayName().split(" ")[0]))
                .setPositiveButton(getString(R.string.net_accept), dialogClickListener).setNegativeButton(getString(R.string.net_decline), dialogClickListener)
                .show();
    }

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    // Called when we are connected to the room. We're not ready to play yet!
    // (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(getGamesClient().getCurrentPlayerId());

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    // Called when we've successfully left the room (this happens a result of
    // voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get
    // onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
    }

    // Called when we get disconnected from the room. We return to the main
    // screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
    }

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if(statusCode != GamesClient.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if(statusCode != GamesClient.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if(statusCode != GamesClient.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // We treat most of the room update callbacks in the same way: we update our
    // list of
    // participants and update the display. In a real game we would also have to
    // check if that
    // change requires some action like removing the corresponding player avatar
    // from the screen,
    // etc.
    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateRoom(room);
        if(mNetworkPlayer != null && mGame != null) {
            mNetworkPlayer.forfeit();
            mGame.getCurrentPlayer().endMove();
        }
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
        if(mNetworkPlayer != null && mGame != null) {
            mNetworkPlayer.forfeit();
            mGame.getCurrentPlayer().endMove();
        }
    }

    void updateRoom(Room room) {
        mParticipants = room.getParticipants();
    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

    private NetworkPlayer mNetworkPlayer;
    private Game mGame;
    private boolean mShowingDialog;

    // Start the gameplay phase of the game.
    void startGame(boolean rematch) {
        Object[] players = mParticipants.toArray();
        Arrays.sort(players, new Comparator<Object>() {
            @Override
            public int compare(Object lhs, Object rhs) {
                return ((Participant) lhs).getParticipantId().compareTo(((Participant) rhs).getParticipantId());
            }
        });
        GameOptions go = new GameOptions();
        go.gridSize = 7;
        go.swap = true;
        go.timer = new Timer(0, 0, Timer.NO_TIMER);

        PlayingEntity p1;
        PlayingEntity p2;
        if(((Participant) players[0]).getParticipantId().equals(mMyId)) {
            p1 = new PlayerObject(1);
            p2 = mNetworkPlayer = new NetworkPlayer(2, this);
            mNetworkPlayer.setCallbacks(this);
        }
        else {
            p1 = mNetworkPlayer = new NetworkPlayer(1, this);
            p2 = new PlayerObject(2);
            mNetworkPlayer.setCallbacks(this);
        }
        p1.setColor(getResources().getInteger(R.integer.DEFAULT_P1_COLOR));
        p2.setColor(getResources().getInteger(R.integer.DEFAULT_P2_COLOR));
        p1.setName(((Participant) players[0]).getDisplayName().split(" ")[0]);
        p2.setName(((Participant) players[1]).getDisplayName().split(" ")[0]);

        mGame = new Game(go, p1, p2);
        switchToGame(mGame, !rematch);
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    // Called when we receive a real-time message from the network.
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        String data = new String(rtm.getMessageData());
        Log.d(TAG, "Message received: " + data);

        if(mNetworkPlayer != null) mNetworkPlayer.receivedMessage(data);
    }

    // Broadcast my score to everybody else.
    void broadcastMessage(byte[] message) {
        // Send to every other participant.
        for(Participant p : mParticipants) {
            if(p.getParticipantId().equals(mMyId)) continue;
            if(p.getStatus() != Participant.STATUS_JOINED) continue;
            getGamesClient().sendReliableRealTimeMessage(null, message, mRoomId, p.getParticipantId());
        }
    }

    // Broadcast a message indicating that we're starting to play. Everyone else
    // will react
    // by dismissing their waiting room UIs and starting to play too.
    void broadcastStart(byte[] message) {
        for(Participant p : mParticipants) {
            if(p.getParticipantId().equals(mMyId)) continue;
            if(p.getStatus() != Participant.STATUS_JOINED) continue;
            getGamesClient().sendReliableRealTimeMessage(null, message, mRoomId, p.getParticipantId());
        }
    }

    /*
     * MISC SECTION. Miscellaneous methods.
     */

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /*
     * UI Section
     */

    public abstract void switchToGame(Game game, boolean leaveRoom);

    @Override
    public void sendMessage(String msg) {
        broadcastMessage(msg.getBytes());
    }

    @Override
    public void kill() {
        leaveRoom();
    }

    @Override
    public void newGame(String gameData) {
        Log.d(TAG, "New game commanded from above");
        startGame(true);
    }

    @Override
    public String newGameReqest() {
        Log.d(TAG, "New game requested");
        final LinkedBlockingQueue<Boolean> reply = new LinkedBlockingQueue<Boolean>();
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    reply.add(true);
                    mShowingDialog = false;
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    reply.add(false);
                    mShowingDialog = false;
                    break;
                }
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.net_challeneger_requests_new_game, mNetworkPlayer.getName()))
                .setPositiveButton(getString(R.string.net_accept), dialogClickListener).setNegativeButton(getString(R.string.net_decline), dialogClickListener)
                .setOnCancelListener(this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!mShowingDialog) {
                    builder.show();
                    mShowingDialog = true;
                }
            }
        });

        try {
            if(reply.take()) {
                return "true";
            }
            else {
                return null;
            }
        }
        catch(InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean undoRequest(final int turnNumber) {
        Log.d(TAG, "Undo requested");
        final LinkedBlockingQueue<Boolean> reply = new LinkedBlockingQueue<Boolean>();
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    reply.add(true);
                    mShowingDialog = false;
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    reply.add(false);
                    mShowingDialog = false;
                    break;
                }
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.net_challeneger_requests_undo, mNetworkPlayer.getName(), turnNumber))
                .setPositiveButton(getString(R.string.net_accept), dialogClickListener).setNegativeButton(getString(R.string.net_decline), dialogClickListener)
                .setOnCancelListener(this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!mShowingDialog) {
                    builder.show();
                    mShowingDialog = true;
                }
            }
        });

        try {
            return reply.take();
        }
        catch(InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void undo(int turnNumber) {
        mGame.undo(turnNumber);
    }

    @Override
	public void error(Errors error) {
    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.error_version).
        setPositiveButton(getString(R.string.net_accept),null).setOnCancelListener(this);
       
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!mShowingDialog) {
                    builder.show();
                    mShowingDialog = true;
                }
            }
        });
        returnHome();
	}

    protected abstract void returnHome();

	@Override
    public void chat(String message) {}

    @Override
    public void onCancel(DialogInterface dialog) {
        mShowingDialog = false;
    }
}
