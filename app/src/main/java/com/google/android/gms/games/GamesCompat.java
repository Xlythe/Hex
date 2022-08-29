package com.google.android.gms.games;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class GamesCompat {
  public static TurnBasedMultiplayerClient getTurnBasedMultiplayerClient(Context context, GoogleSignInAccount account) {
    return new TurnBasedMultiplayerClient();
  }

  public static InvitationsClient getInvitationsClient(Context context, GoogleSignInAccount account) {
    return new InvitationsClient();
  }
}
