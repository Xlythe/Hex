package com.sam.hex;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.games.GamesClient;
import com.hex.core.Timer;

/**
 * @author Will Harmon
 **/
public class Settings {
    public static int getGridSize(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int gridSize = Integer.valueOf(prefs.getString("gameSizePref", context.getString(R.integer.DEFAULT_BOARD_SIZE)));
        if(gridSize == 0) gridSize = Integer.valueOf(prefs.getString("customGameSizePref", context.getString(R.integer.DEFAULT_BOARD_SIZE)));

        // We don't want 0x0 games
        if(gridSize <= 0) gridSize = 1;
        return gridSize;
    }

    public static boolean getSwap(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("swapPref", context.getResources().getBoolean(R.bool.DEFAULT_SWAP_ENABLED));
    }

    public static boolean getAutosave(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autosavePref",
                context.getResources().getBoolean(R.bool.DEFAULT_AUTOSAVE_ENABLED));
    }

    public static int getTimerType(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("timerTypePref", String.valueOf(Timer.NO_TIMER)));
    }

    public static int getTimeAmount(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("timerPref", "0"));
    }

    public static String getPlayer1Name(Context context, GamesClient gamesClient) {
        if(gamesClient.isConnected()) return gamesClient.getCurrentPlayer().getDisplayName().split(" ")[0];
        return context.getString(R.string.DEFAULT_P1_NAME);
    }

    public static String getPlayer2Name(Context context) {
        return context.getString(R.string.DEFAULT_P2_NAME);
    }

    public static int getPlayer1Color(Context context) {
        return context.getResources().getInteger(R.integer.DEFAULT_P1_COLOR);
    }

    public static int getPlayer2Color(Context context) {
        return context.getResources().getInteger(R.integer.DEFAULT_P2_COLOR);
    }

    public static int getComputerDifficulty(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("comDifficulty",
                String.valueOf(context.getResources().getInteger(R.integer.DEFAULT_AI_DIFFICULTY))));
    }
}
