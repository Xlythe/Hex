package com.sam.hex;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.hex.core.Timer;

/**
 * @author Will Harmon
 **/
public class Settings {
    public static int getGridSize(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int gridSize = Integer.valueOf(prefs.getString("gameSizePref", Integer.toString(context.getResources().getInteger(R.integer.DEFAULT_BOARD_SIZE))));
        if (gridSize == 0)
            gridSize = Integer.valueOf(prefs.getString("customGameSizePref", Integer.toString(context.getResources().getInteger(R.integer.DEFAULT_BOARD_SIZE))));

        // We don't want 0x0 games
        if (gridSize <= 0) gridSize = 1;
        return gridSize;
    }

    public static boolean getSwap(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("swapPref", context.getResources().getBoolean(R.bool.DEFAULT_SWAP_ENABLED));
    }

    public static boolean getAutosave(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autosavePref",
                context.getResources().getBoolean(R.bool.DEFAULT_AUTOSAVE_ENABLED));
    }

    public static int getTimerType(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("timerTypePref", String.valueOf(Timer.NO_TIMER)));
    }

    public static int getTimeAmount(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("timerPref", "0"));
    }

    public static String getPlayer1Name(@NonNull Context context, @NonNull GoogleApiClient gamesClient) {
        if (gamesClient.isConnected())
            return Games.Players.getCurrentPlayer(gamesClient).getDisplayName().split(" ")[0];
        return context.getString(R.string.DEFAULT_P1_NAME);
    }

    public static String getPlayer2Name(@NonNull Context context) {
        return context.getString(R.string.DEFAULT_P2_NAME);
    }

    public static int getPlayer1Color(@NonNull Context context) {
        return context.getResources().getInteger(R.integer.DEFAULT_P1_COLOR);
    }

    public static int getPlayer2Color(@NonNull Context context) {
        return context.getResources().getInteger(R.integer.DEFAULT_P2_COLOR);
    }

    public static int getComputerDifficulty(@NonNull Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("comDifficulty",
                String.valueOf(context.getResources().getInteger(R.integer.DEFAULT_AI_DIFFICULTY))));
    }
}
