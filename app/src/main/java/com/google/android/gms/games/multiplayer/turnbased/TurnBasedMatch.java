package com.google.android.gms.games.multiplayer.turnbased;

import com.google.android.gms.games.GameCompat;
import com.google.android.gms.games.multiplayer.Participant;

import java.util.ArrayList;

public interface TurnBasedMatch {
  int MATCH_TURN_STATUS_MY_TURN = 1;
  int MATCH_TURN_STATUS_THEIR_TURN = 2;

  GameCompat getGame();
  String getMatchId();
  int getTurnStatus();
  byte[] getData();
  int getVersion();
  String getRematchId();
  ArrayList<String> getParticipantIds();
  String getParticipantId(String playerId);
  Participant getParticipant(String participantId);
}