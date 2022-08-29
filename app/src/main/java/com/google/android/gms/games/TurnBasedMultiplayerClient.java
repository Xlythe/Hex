package com.google.android.gms.games;

import android.content.Intent;

import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchUpdateCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

public class TurnBasedMultiplayerClient {
  public Task<TurnBasedMatch> rematch(String matchId) {
    return Tasks.forException(new Exception("Stub!"));
  }

  public Task<TurnBasedMatch> acceptInvitation(String invitationId) {
    return Tasks.forException(new Exception("Stub!"));
  }

  public Task<TurnBasedMatch> createMatch(TurnBasedMatchConfig config) {
    return Tasks.forException(new Exception("Stub!"));
  }

  public Task<Intent> getSelectOpponentsIntent(int min, int max, boolean allowAutomatch) {
    return Tasks.forException(new Exception("Stub!"));
  }

  public Task<Intent> getInboxIntent() {
    return Tasks.forException(new Exception("Stub!"));
  }

  public Task<Void> registerTurnBasedMatchUpdateCallback(TurnBasedMatchUpdateCallback callback) {
    return Tasks.forException(new Exception("Stub!"));
  }

  public Task<Void> unregisterTurnBasedMatchUpdateCallback(TurnBasedMatchUpdateCallback callback) {
    return Tasks.forException(new Exception("Stub!"));
  }

  public Task<TurnBasedMatch> takeTurn(String matchId, byte[] data, String participantId) {
    return Tasks.forException(new Exception("Stub!"));
  }

  public Task<Void> finishMatch(String matchId, byte[] data, ParticipantResult... players) {
    return Tasks.forException(new Exception("Stub!"));
  }
}
