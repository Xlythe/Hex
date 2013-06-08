package com.sam.hex;

import android.content.Intent;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

public class HexRoomUpdateListener implements RoomUpdateListener {
    private MainActivity mMainActivity;
    private String mRoomId;

    public HexRoomUpdateListener(MainActivity activity) {
        mMainActivity = activity;
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if(statusCode != GamesClient.STATUS_OK) {
            // display error
            return;
        }

        mRoomId = room.getRoomId();

        // get waiting room intent
        Intent i = mMainActivity.getGamesClient().getRealTimeWaitingRoomIntent(room, 1);
        mMainActivity.startActivityForResult(i, MainActivity.RC_WAITING_ROOM);
    }

    @Override
    public void onLeftRoom(int arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomConnected(int arg0, Room arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if(statusCode != GamesClient.STATUS_OK) {
            // display error
            return;
        }

        mRoomId = room.getRoomId();

        // get waiting room intent
        Intent i = mMainActivity.getGamesClient().getRealTimeWaitingRoomIntent(room, Integer.MAX_VALUE);
        mMainActivity.startActivityForResult(i, MainActivity.RC_WAITING_ROOM);
    }

}
