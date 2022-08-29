package com.google.android.gms.games.multiplayer.turnbased;

import androidx.annotation.NonNull;

public interface TurnBasedMatchUpdateCallback {
  void onTurnBasedMatchReceived(@NonNull TurnBasedMatch turnBasedMatch);
  void onTurnBasedMatchRemoved(@NonNull String matchId);
}
