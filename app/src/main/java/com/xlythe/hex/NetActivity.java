package com.xlythe.hex;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.PlayerCompat;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.xlythe.hex.compat.Game;
import com.xlythe.hex.compat.GameOptions;
import com.xlythe.hex.compat.NetworkPlayer;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.xlythe.hex.Settings.TAG;

public abstract class NetActivity extends BaseGameActivity {
    private final static int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;

    private static final int REQUEST_CODE_SELECT_OPPONENT = 10001;
    private static final int REQUEST_CODE_INBOX = 10002;
    private static final int REQUEST_CODE_ACHIEVEMENTS = 10003;

    // The local player id for ourselves. Null if not signed in.
    private String mPlayerId;

    // Switches to a new game for a rematch.
    private final NetworkPlayer.Rematcher rematcher = matchId -> {
            getTurnBasedMultiplayerClient().rematch(matchId)
                    .addOnSuccessListener(this::startGame)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to request rematch", e);
                        checkInvites();
                    });
    };

    public abstract void switchToGame(Game game);

    @Override
    public void onSignInSucceeded(GoogleSignInAccount googleSignInAccount) {
        super.onSignInSucceeded(googleSignInAccount);
        getPlayersClient().getCurrentPlayer().addOnSuccessListener(player -> mPlayerId = player.getPlayerId());
        getGamesClient().getActivationHint().addOnSuccessListener(bundle -> {
           if (bundle == null) {
               return;
           }

            if (bundle.containsKey(Multiplayer.EXTRA_INVITATION)) {
                Invitation invitation = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);
                if (invitation.getInvitationType() == Invitation.INVITATION_TYPE_TURN_BASED) {
                    getTurnBasedMultiplayerClient().acceptInvitation(invitation.getInvitationId()).addOnSuccessListener(this::startGame);
                    return;
                }
            }

           if (bundle.containsKey(Multiplayer.EXTRA_TURN_BASED_MATCH)) {
               TurnBasedMatch match = bundle.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
               startGame(match);
               return;
           }
        });
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
            // Note: Must call startActivityForResult or the activity won't launch.
            startActivityForResult(intent, REQUEST_CODE_ACHIEVEMENTS);
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

    private void startGame(@NonNull TurnBasedMatch match) {
        PlayingEntity[] players = getPlayers(match);

        Game game;
        if (match.getData() != null) {
            game = Game.load(new String(match.getData()), players[0], players[1]);
            Log.d(TAG, String.format("Resuming match %s.", match.getMatchId()));
        } else {
            GameOptions gameOptions = new GameOptions.Builder()
                    .setGridSize(Settings.getGridSize(this))
                    .setSwapEnabled(Settings.getSwap(this))
                    .setNoTimer()
                    .build();

            game = new Game(gameOptions, players[0], players[1]);
            Log.d(TAG, String.format("New match %s.", match.getMatchId()));
        }

        // Set names / colors here. Note that loading a game from a remote side will flip the names,
        // so this must occur after Game.load.
        String localPlayerName = getLocalPlayerName(match, mPlayerId);
        String remotePlayerName = getRemotePlayerName(this, match, mPlayerId);
        if (isLocalPlayer1(match)) {
            players[0].setName(localPlayerName);
            players[1].setName(remotePlayerName);
        } else {
            players[0].setName(remotePlayerName);
            players[1].setName(localPlayerName);
        }
        players[0].setColor(getResources().getInteger(R.integer.DEFAULT_P1_COLOR));
        players[1].setColor(getResources().getInteger(R.integer.DEFAULT_P2_COLOR));

        Log.d(TAG, String.format("%s vs %s in match %s.", players[0].getName(), players[1].getName(), match.getMatchId()));

        switch (match.getTurnStatus()) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                Log.d(TAG, "It's my turn");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                Log.d(TAG, "It's their turn");
                break;
        }

        switchToGame(game);
    }

    private PlayingEntity[] getPlayers(TurnBasedMatch match) {
        String localParticipantId = getLocalParticipantId(match, mPlayerId);
        String remoteParticipantId = getRemoteParticipantId(match, mPlayerId);
        Log.d(TAG, String.format("Local id %s, remote id %s in match %s.", localParticipantId, remoteParticipantId, match.getMatchId()));

        PlayingEntity[] players = new PlayingEntity[2];
        if (isLocalPlayer1(match)) {
            players[0] = new PlayerObject(1);
            players[1] = new NetworkPlayer(
                    2,
                    localParticipantId,
                    remoteParticipantId,
                    match,
                    rematcher,
                    getTurnBasedMultiplayerClient());
        } else {
            players[0] = new NetworkPlayer(
                    1,
                    localParticipantId,
                    remoteParticipantId,
                    match,
                    rematcher,
                    getTurnBasedMultiplayerClient());
            players[1] = new PlayerObject(2);
        }

        return players;
    }

    private static List<PlayerCompat> getPlayerList(TurnBasedMatch match) {
        List<PlayerCompat> players = new ArrayList<>(match.getParticipantIds().size());
        for (String participantId : match.getParticipantIds()) {
            @Nullable PlayerCompat player = match.getParticipant(participantId).getPlayer();
            if (player == null) {
                continue;
            }

            players.add(player);
        }
        return players;
    }

    private static String getLocalPlayerName(TurnBasedMatch match, String playerId) {
        return getShortName(getLocalPlayer(playerId, getPlayerList(match)));
    }

    private static String getRemotePlayerName(Context context, TurnBasedMatch match, String playerId) {
        @Nullable PlayerCompat remotePlayer = getRemotePlayer(playerId, getPlayerList(match));
        if (remotePlayer == null) {
            Log.d(TAG, "Unable to get remote player name. No player found.");
            return context.getString(R.string.player_automatch);
        }
        return getShortName(remotePlayer);
    }

    private static String getLocalParticipantId(TurnBasedMatch match, String playerId) {
        return match.getParticipantId(getLocalPlayer(playerId, getPlayerList(match)).getPlayerId());
    }

    // May return null in automatched games.
    @Nullable
    private static String getRemoteParticipantId(TurnBasedMatch match, String playerId) {
        // For automatch games, the remote player is null. Therefore, we need to look up our
        // participant id, and then find out who the remote participant is.
        String localParticipantId = getLocalParticipantId(match, playerId);
        for (String participantId : match.getParticipantIds()) {
            if (localParticipantId.equals(participantId)) {
                continue;
            }

            return participantId;
        }

        return null;
    }

    @NonNull
    private static PlayerCompat getLocalPlayer(String localPlayerId, List<PlayerCompat> playerList) {
        for (PlayerCompat player : playerList) {
            if (player.getPlayerId().equals(localPlayerId)) {
                return player;
            }
        }
        throw new IllegalStateException(String.format("Local player %s was not found within the list of players: %s", localPlayerId, playerList));
    }

    @Nullable
    private static PlayerCompat getRemotePlayer(String localPlayerId, List<PlayerCompat> playerList) {
        for (PlayerCompat player : playerList) {
            if (player.getPlayerId().equals(localPlayerId)) {
                continue;
            }

            return player;
        }
        return null;
    }

    private static String getShortName(PlayerCompat player) {
        String name = player.getDisplayName().split(" ")[0];
        if (name.length() > 10) {
            return name.substring(0, 10);
        }
        return name;
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
