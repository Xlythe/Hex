package com.sam.hex;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

public abstract class BaseGameActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean mSignedIn = false;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mSignedIn = true;
        onSignInSucceeded(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mSignedIn = false;
        onSignInFailed();
    }

    public GoogleApiClient getClient() {
        return mGoogleApiClient;
    }

    public boolean isSignedIn() {
        return mSignedIn;
    }

    public void beginUserInitiatedSignIn() {
        mGoogleApiClient.connect();
    }

    public void signOut() {
        Games.signOut(mGoogleApiClient);
    }

    public void onSignInSucceeded(@Nullable Bundle bundle) {

    }

    public void onSignInFailed() {

    }
}
