package com.google.android.gms.games.multiplayer;

import com.google.android.gms.games.GameCompat;

public interface Invitation {
  int INVITATION_TYPE_TURN_BASED = 1;
  GameCompat getGame();
  String getInvitationId();
  int getInvitationType();
}
