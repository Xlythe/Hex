package com.google.android.gms.games.multiplayer.turnbased;

import android.os.Bundle;

import java.util.ArrayList;

public abstract class TurnBasedMatchConfig {
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Builder() {}

    public Builder addInvitedPlayers(ArrayList<String> playerIds) {
      return this;
    }

    public Builder setAutoMatchCriteria(Bundle autoMatchCriteria) {
      return this;
    }

    public TurnBasedMatchConfig build() {
      return null;
    }
  }
}
