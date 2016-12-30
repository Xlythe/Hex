package com.sam.hex;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.util.Locale;

import static com.sam.hex.Settings.TAG;

public abstract class BaseGameActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLUTION = 1001;

    private GoogleApiClient mGoogleApiClient;
    private boolean mSignedIn = false;
    private boolean mConnecting = false;

    @Nullable
    private ConnectionResult mConnectionResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mConnecting) {
            mConnecting = true;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mSignedIn = true;
        mConnecting = false;
        onSignInSucceeded(bundle);
    }

    @Override
    public void onConnectionSuspended(int reason) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        mSignedIn = false;
        mConnecting = false;
        Log.w(TAG, String.format("Connection failed: %s", toString(result)));
        if (result.hasResolution()) {
            Log.v(TAG, "Resolution is available");
            mConnectionResult = result;
            beginUserInitiatedSignIn();
        }
        onSignInFailed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_RESOLUTION) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Resolution succeeded");
                mConnecting = true;
                mGoogleApiClient.connect();
            } else {
                Log.w(TAG, "Resolution failed: " + resultCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public GoogleApiClient getClient() {
        return mGoogleApiClient;
    }

    public boolean isSignedIn() {
        return mSignedIn;
    }

    public void beginUserInitiatedSignIn() {
        Log.v(TAG, "User initiated sign in");
        mConnecting = true;
        if (mConnectionResult != null) {
            Log.v(TAG, "Attempting resolution");
            try {
                mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Failed to start resolution", e);
            }
        } else {
            mGoogleApiClient.connect();
        }
    }

    public void signOut() {
        Games.signOut(mGoogleApiClient);
    }

    public void onSignInSucceeded(@Nullable Bundle bundle) {}

    public void onSignInFailed() {}

    private static String toString(ConnectionResult result) {
        return String.format(Locale.US, "[%d][%s]", result.getErrorCode(), result.getErrorMessage() == null
                ? CommonStatusCodes.getStatusCodeString(result.getErrorCode())
                : result.getErrorMessage());
    }
}
