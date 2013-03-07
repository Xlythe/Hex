package com.sam.hex.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sam.hex.GameObject;

public class GameActivity extends DefaultActivity {
    public static final String GAME = "game";
    protected GameObject game;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.get(GAME) != null) {
            game = (GameObject) savedInstanceState.get(GAME);
        }
        else {
            Intent intent = getIntent();
            if(intent.getExtras() == null) {
                finish();
                return;
            }
            game = (GameObject) intent.getExtras().get(GAME);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(GAME, game);
    }

    protected Context getContext() {
        return this;
    }

    public GameObject getGame() {
        return game;
    }
}
