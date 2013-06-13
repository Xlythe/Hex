/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sam.hex;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.plus.PlusClient;

/**
 * Example base class for games. This implementation takes care of setting up
 * the GamesClient object and managing its lifecycle. Subclasses only need to
 * override the @link{#onSignInSucceeded} and @link{#onSignInFailed} abstract
 * methods. To initiate the sign-in flow, subclasses should call
 * 
 * @link{#beginUserInitiatedSignIn .
 * 
 * @author Bruno Oliveira (Google)
 * 
 */
public abstract class BaseGameActivity extends PurchaseActivity implements GameHelper.GameHelperListener {

    protected GameHelper mHelper;

    // expose these constants here because we don't want users of this class
    // to have to know about GameHelper at all
    public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
    public static final int CLIENT_APPSTATE = GameHelper.CLIENT_APPSTATE;
    public static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
    public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

    // requested clients. By default, just the games client.
    protected int mRequestedClients = CLIENT_GAMES;

    protected BaseGameActivity() {
        super();
        mHelper = new GameHelper(this);
    }

    protected BaseGameActivity(int requestedClients) {
        super();
        mRequestedClients = requestedClients;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        mHelper = new GameHelper(this);
        mHelper.setup(this, mRequestedClients);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHelper.onStop();
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        mHelper.onActivityResult(request, response, data);
    }

    public GamesClient getGamesClient() {
        return mHelper.getGamesClient();
    }

    public AppStateClient getAppStateClient() {
        return mHelper.getAppStateClient();
    }

    protected PlusClient getPlusClient() {
        return mHelper.getPlusClient();
    }

    public boolean isSignedIn() {
        return mHelper.isSignedIn();
    }

    public void beginUserInitiatedSignIn() {
        mHelper.beginUserInitiatedSignIn();
    }

    public void signOut() {
        mHelper.signOut();
    }

    protected void showAlert(String title, String message) {
        mHelper.showAlert(title, message);
    }

    protected void enableDebugLog(boolean enabled, String tag) {
        mHelper.enableDebugLog(enabled, tag);
    }

    protected String getInvitationId() {
        return mHelper.getInvitationId();
    }

    protected void reconnectClients(int whichClients) {
        mHelper.reconnectClients(whichClients);
    }

    protected String getScopes() {
        return mHelper.getScopes();
    }
}
