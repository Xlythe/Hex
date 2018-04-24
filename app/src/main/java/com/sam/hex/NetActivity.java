package com.sam.hex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.sam.hex.compat.Game;
import com.sam.hex.compat.GameOptions;
import com.sam.hex.compat.NetworkPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sam.hex.Settings.TAG;

public abstract class NetActivity extends BaseGameActivity {
    private final static int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;

    private static final int REQUEST_CODE_SELECT_OPPONENT = 10001;
    private static final int REQUEST_CODE_INBOX = 10002;

    // The local player id for ourselves. Null if not signed in.
    private String mPlayerId;

    public abstract void switchToGame(Game game);

    @Override
    public void onSignInSucceeded(GoogleSignInAccount googleSignInAccount) {
        super.onSignInSucceeded(googleSignInAccount);
        getPlayersClient().getCurrentPlayer().addOnSuccessListener(player -> mPlayerId = player.getPlayerId());
    }

    @Override
    public void startQuickGame() {
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS, MAX_OPPONENTS, 0);

        TurnBasedMatchConfig config = TurnBasedMatchConfig.builder()
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        // Start the match
        getTurnBasedMultiplayerClient().createMatch(config).addOnSuccessListener(this::startGame);
    }

    @Override
    public void inviteFriends() {
        getTurnBasedMultiplayerClient().getSelectOpponentsIntent(MIN_OPPONENTS, MAX_OPPONENTS, true).addOnSuccessListener(intent -> {
            startActivityForResult(intent, REQUEST_CODE_SELECT_OPPONENT);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    public void checkInvites() {
        getTurnBasedMultiplayerClient().getInboxIntent().addOnSuccessListener(intent -> {
            startActivityForResult(intent, REQUEST_CODE_INBOX);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    public void openAchievements() {
        getAchievementsClient().getAchievementsIntent().addOnSuccessListener(intent -> {
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_CODE_SELECT_OPPONENT:
                onOpponentSelected(resultCode, intent);
                break;
            case REQUEST_CODE_INBOX:
                onInboxSelected(resultCode, intent);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, intent);
                break;
        }
    }

    private void onOpponentSelected(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "Failed to select an opponent");
            return;
        }

        ArrayList<String> participants = intent.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        TurnBasedMatchConfig config = TurnBasedMatchConfig.builder()
                .addInvitedPlayers(participants)
                .setAutoMatchCriteria(RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS, MAX_OPPONENTS, 0))
                .build();

        // Start the game
        getTurnBasedMultiplayerClient().createMatch(config)
                .addOnSuccessListener(this::startGame)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to create a match", e));
    }

    private void onInboxSelected(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "Failed to select a match from the inbox");
            return;
        }

        TurnBasedMatch match = intent.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
        if (match == null) {
            Log.e(TAG, "A match was selected from the inbox, but the match was null");
            return;
        }

        startGame(match);
    }

    private void startGame(TurnBasedMatch match) {
        PlayingEntity[] players = getPlayers(match);

        Game game;
        if (match.getData() != null) {
            game = Game.load(new String(match.getData()), players[0], players[1]);
        } else {
            GameOptions gameOptions = new GameOptions.Builder()
                    .setGridSize(7)
                    .setSwapEnabled(true)
                    .setNoTimer()
                    .build();

            game = new Game(gameOptions, players[0], players[1]);
            Log.d(TAG, "Starting a new game for match " + match.getMatchId());
        }

        switch (match.getTurnStatus()) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                Log.d(TAG, "I'm going first");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                Log.d(TAG, "They're going first");
                break;
        }

        switchToGame(game);
    }

    private PlayingEntity[] getPlayers(TurnBasedMatch match) {
        List<Player> playerList = getPlayerList(mPlayerId, match);
        String localParticipantId = match.getParticipantId(getLocalPlayer(mPlayerId, playerList).getPlayerId());
        String remoteParticipantId = match.getParticipantId(getRemotePlayer(mPlayerId, playerList).getPlayerId());
        Log.d(TAG, "Participants: " + playerList);
        Log.d(TAG, "Local PlayerId: " + mPlayerId);

        PlayingEntity[] players = new PlayingEntity[2];
        if (playerList.get(0).getPlayerId().equals(mPlayerId)) {
            players[0] = new PlayerObject(1);
            players[1] = new NetworkPlayer(
                    2,
                    localParticipantId,
                    remoteParticipantId,
                    match,
                    getTurnBasedMultiplayerClient());
        } else {
            players[0] = new NetworkPlayer(
                    1,
                    localParticipantId,
                    remoteParticipantId,
                    match,
                    getTurnBasedMultiplayerClient());
            players[1] = new PlayerObject(2);
        }

        players[0].setColor(getResources().getInteger(R.integer.DEFAULT_P1_COLOR));
        players[1].setColor(getResources().getInteger(R.integer.DEFAULT_P2_COLOR));

        players[0].setName(getShortName(playerList.get(0)));
        players[1].setName(getShortName(playerList.get(1)));

        return players;
    }

    private static List<Player> getPlayerList(String localPlayerId, TurnBasedMatch match) {
        List<Player> players = new ArrayList<>(match.getParticipantIds().size());
        for (String participantId : match.getParticipantIds()) {
            players.add(match.getParticipant(participantId).getPlayer());
        }
        Collections.sort(players, (lhs, rhs) -> {
            if (lhs.getPlayerId().equals(localPlayerId)) {
                if (isLocalPlayer1(match)) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (rhs.getPlayerId().equals(localPlayerId)) {
                if (isLocalPlayer1(match)) {
                    return 1;
                } else {
                    return -1;
                }
            }
            Log.w(TAG, "Neither player's turn");
            return 0;
        });

        return players;
    }

    private static Player getLocalPlayer(String localPlayerId, List<Player> playerList) {
        for (Player player : playerList) {
            if (player.getPlayerId().equals(localPlayerId)) {
                return player;
            }
        }
        return null;
    }

    private static Player getRemotePlayer(String localPlayerId, List<Player> playerList) {
        for (Player player : playerList) {
            if (player.getPlayerId().equals(localPlayerId)) {
                continue;
            }

            return player;
        }
        return null;
    }

    private static String getShortName(Player player) {
        return player.getDisplayName().split(" ")[0];
    }

    // Returns true if the local device is player 1.
    private static boolean isLocalPlayer1(TurnBasedMatch match) {
        // If there's no game state yet, then whoever's turn it is is player 1.
        if (match.getData() == null) {
            return isMyMove(match);
        }

        // Otherwise, if there is already game state, open up a local copy of the game to decide
        // if its player1's move or player2's move. If it's player1's turn and it's our turn,
        // we're player1. If it's player2's turn and it's our turn, we're player2.
        Game game = Game.load(new String(match.getData()));
        switch (game.getCurrentPlayer().getTeam()) {
            case 1:
                return isMyMove(match);
            case 2:
                return !isMyMove(match);
            default:
                throw new IllegalStateException("Cannot parse game. Current player has team " + game.getCurrentPlayer().getTeam());
        }
    }

    private static boolean isMyMove(TurnBasedMatch match) {
        return match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
    }
}
