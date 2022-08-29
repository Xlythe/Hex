package com.google.android.gms.games.multiplayer.realtime;

import android.os.Bundle;

import com.google.android.gms.games.multiplayer.Multiplayer;

public abstract class RoomConfig {
  public static final class Builder {
    public RoomConfig build() {
      return null;
    }
  }

  public static Bundle createAutoMatchCriteria(int minAutoMatchPlayers,
                                               int maxAutoMatchPlayers, long exclusiveBitMask) {
    Bundle autoMatchData = new Bundle();
    autoMatchData.putInt(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, minAutoMatchPlayers);
    autoMatchData.putInt(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, maxAutoMatchPlayers);
    autoMatchData.putLong(Multiplayer.EXTRA_EXCLUSIVE_BIT_MASK, exclusiveBitMask);
    return autoMatchData;
  }
}
