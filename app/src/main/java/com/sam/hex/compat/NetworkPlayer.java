package com.sam.hex.compat;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.games.TurnBasedMultiplayerClient;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchUpdateCallback;
import com.google.android.gms.tasks.Tasks;
import com.hex.core.Game;
import com.hex.core.GameAction;
import com.hex.core.Move;
import com.hex.core.MoveList;
import com.hex.core.Player;
import com.hex.core.PlayingEntity;
import com.hex.core.Point;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

import static com.sam.hex.Settings.TAG;

/** A NetworkPlayer that relies on Google Play Games. */
public class NetworkPlayer implements PlayingEntity {
    private static final Point END_MOVE = new Point(-1, -1);

    private final String localParticipantId;
    @Nullable private String remoteParticipantId;
    private TurnBasedMatch match;
    private final TurnBasedMultiplayerClient turnBasedMultiplayerClient;
    private final Runnable rematcher;

    // The team we're on. Either 1 or 2.
    private final int team;
    // The name of this player.
    private String name;
    // The color of this player.
    @ColorInt private int color;

    // How much time this player has left to make their move.
    private long timeLeft;

    // If true, this player has forfeited the match.
    private boolean hasForfeited = false;

    // Update this queue with the current move when the remote side has reported where they want to place their piece.
    private final LinkedBlockingQueue<Point> currentMove = new LinkedBlockingQueue<>();

    private TurnBasedMatchUpdateCallback turnBasedMatchUpdateCallback = new TurnBasedMatchUpdateCallback() {
        @Override
        public void onTurnBasedMatchReceived(@NonNull TurnBasedMatch turnBasedMatch) {
            // Ignore matches without any data. That means no moves have been made yet.
            if (turnBasedMatch.getData() == null) {
                Log.w(TAG, "Ignoring match because there was no data inside. Expected game state.");
                return;
            }

            // Ignore data from matches other than the one we're currently in.
            if (!match.getMatchId().equals(turnBasedMatch.getMatchId())) {
                Log.w(TAG, String.format("Ignoring match because it's for a different match id. Got %s when expecting %s.", turnBasedMatch.getMatchId(), match.getMatchId()));
                return;
            }

            // Invalidate our match state.
            setMatch(turnBasedMatch);

            // Load the game state from the remote side and attempt to make the same move on this side.
            Game game = Game.load(new String(turnBasedMatch.getData()));
            Move lastMove = game.getMoveList().getMove();
            if (lastMove.getTeam() != team) {
                Log.w(TAG, "Ignoring match update because it wasn't my move");
                return;
            }

            currentMove.add(new Point(lastMove.getX(), lastMove.getY()));
            Log.d(TAG, String.format("Received %d, %d  for %d from the remote side.", lastMove.getX(), lastMove.getY(), lastMove.getTeam()));
        }

        @Override
        public void onTurnBasedMatchRemoved(@NonNull String matchId) {
            // Ignore data from matches other than the one we're currently in.
            if (!match.getMatchId().equals(matchId)) {
                Log.w(TAG, String.format("Ignoring match because it's for a different match id. Got %s when expecting %s.", matchId, match.getMatchId()));
                return;
            }

            Log.w(TAG, String.format("Match %s has been removed. Considering it forfeited.", matchId));
            hasForfeited = true;
            endMove();
        }
    };

    public NetworkPlayer(
            int team,
            String localParticipantId,
            @Nullable String remoteParticipantId,
            TurnBasedMatch match,
            Runnable rematcher,
            TurnBasedMultiplayerClient turnBasedMultiplayerClient) {
        this.team = team;
        this.localParticipantId = localParticipantId;
        this.remoteParticipantId = remoteParticipantId;
        this.match = match;
        this.rematcher = rematcher;
        this.turnBasedMultiplayerClient = turnBasedMultiplayerClient;
    }

    private void setMatch(TurnBasedMatch match) {
        this.match = match;

        // Attempt to find the remote participant. Can be null in automatch games.
        if (remoteParticipantId == null) {
            for (String participantId : match.getParticipantIds()) {
                if (localParticipantId.equals(participantId)) {
                    continue;
                }

                remoteParticipantId = participantId;
                Log.d(TAG, "Remote participant id updated to " + participantId);
                break;
            }
        }
    }

    /** The game has now started. State can be initialized here. */
    @Override
    public void startGame() {
        turnBasedMultiplayerClient.registerTurnBasedMatchUpdateCallback(turnBasedMatchUpdateCallback);
    }

    /**
     * It's our turn to make a move. We should block until we've determined which move to make.
     * When we've decided on our move, call GameAction.makeMove(PlayingEntity, Point, Game).
     * If GameAction.makeMove is not called when this method resolves, the player's turn is
     * considered skipped.
     */
    @Override
    public void getPlayerTurn(Game game) {
        // Ignore replays. We'll wait for the replay to finish before continuing to make moves.
        if (game.replayRunning) {
            return;
        }

        // As long as this wasn't the very first move, send the local move to the remote side before waiting for their response.
        MoveList moveList = game.getMoveList();
        if (moveList.size() > 0 && match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {
            try {
                match = Tasks.await(turnBasedMultiplayerClient.takeTurn(match.getMatchId(), game.save().getBytes(), remoteParticipantId));
                Log.d(TAG, "Successfully told the remote side what move I made.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currentMove.clear();
        while(true) {
            Point p;
            try {
                p = currentMove.take();
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                p = END_MOVE;
            }

            if (p.equals(END_MOVE)) {
                break;
            }

            if (GameAction.makeMove(this, p, game)) {
                break;
            }
        }
    }

    /** The player has passed their turn. This should interrupt getPlayerTurn(). */
    @Override
    public void endMove() {
        currentMove.add(END_MOVE);
    }

    /** The game has ended. Clean up state. */
    @Override
    public void quit() {
        endMove();
        turnBasedMultiplayerClient.unregisterTurnBasedMatchUpdateCallback(turnBasedMatchUpdateCallback);
    }

    /** Return true if the player has forfeited the match. */
    @Override
    public boolean giveUp() {
        return hasForfeited;
    }

    /** Called if this player has won the game. */
    @Override
    public void win() {
        // The remote side will report this match as finished.
    }

    /** Called if this player has lost the game. */
    @Override
    public void lose(Game game) {
        turnBasedMultiplayerClient.finishMatch(
                match.getMatchId(),
                game.save().getBytes(),
                new ParticipantResult(localParticipantId, ParticipantResult.MATCH_RESULT_WIN, 1),
                new ParticipantResult(remoteParticipantId, ParticipantResult.MATCH_RESULT_LOSS, 2))
        .addOnSuccessListener(match -> Log.d(TAG, "Informed the remote side that they lost the match."));
    }

    /** True if rematches are allowed in this game. */
    @Override
    public boolean supportsNewgame() {
        return true;
    }

    /** 'New Game' was called. Update local state. This should break out of getPlayerTurn(). */
    @Override
    public void newgameCalled() {
        endMove();
        rematcher.run();
    }

    /** True if undo is allowed in this game. */
    @Override
    public boolean supportsUndo(Game game) {
        return false;
    }

    /** 'Undo' was called. Update local state. */
    @Override
    public void undoCalled() {}

    @Override
    public boolean supportsSave() {
        return false;
    }

    @Override
    public Serializable getSaveState() {
        return null;
    }

    @Override
    public void setSaveState(Serializable state) {}

    /** Sets the player's name. Only allowed to be called once. */
    @Override
    public synchronized void setName(String name) {
        this.name = name;
    }

    /** Returns the player's name. */
    @Override
    public synchronized String getName() {
        return name;
    }

    /** Sets the player's color. */
    @Override
    public synchronized void setColor(int color) {
        this.color = color;
    }

    /** Returns the player's color. */
    @Override
    public synchronized int getColor() {
        return color;
    }

    @Override
    public synchronized void setTime(long time) {
        this.timeLeft = time;
    }

    @Override
    public synchronized long getTime() {
        return timeLeft;
    }

    @Override
    public byte getTeam() {
        return (byte) team;
    }

    @Override
    public Player getType() {
        return Player.Net;
    }
}
