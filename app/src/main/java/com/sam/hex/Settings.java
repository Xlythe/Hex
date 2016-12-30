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
    static final String TAG = "Hex";

    public static final int MAX_BOARD_SIZE = 30;
    public static final int MIN_BOARD_SIZE = 4;

    private static final String NUM_TIMES_OPENED = "num_times_app_opened_review";
    public static final String GAME_SIZE = "gameSizePref";
    public static final String CUSTOM_GAME_SIZE = "customGameSizePref";
    private static final String SWAP = "swapPref";
    private static final String AUTOSAVE = "autosavePref";
    public static final String TIMER_TYPE = "timerTypePref";
    public static final String TIMER = "timerPref";
    public static final String TIMER_OPTIONS = "timerOptionsPref";
    public static final String DIFFICULTY = "comDifficulty";

    public static int getNumTimesOpened(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(NUM_TIMES_OPENED, 0);
    }

    public static void incrementNumTimesOpened(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int numTimesOpened = prefs.getInt(NUM_TIMES_OPENED, 0);
        prefs.edit().putInt(NUM_TIMES_OPENED, numTimesOpened + 1).apply();
    }

    public static void setTimesOpened(@NonNull Context context, int times) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(NUM_TIMES_OPENED, times).apply();
    }

    public static int getGridSize(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int gridSize = Integer.valueOf(prefs.getString(GAME_SIZE, Integer.toString(context.getResources().getInteger(R.integer.DEFAULT_BOARD_SIZE))));
        if (gridSize == 0) {
            gridSize = Integer.valueOf(prefs.getString(CUSTOM_GAME_SIZE, Integer.toString(context.getResources().getInteger(R.integer.DEFAULT_BOARD_SIZE))));
        }

        // We don't want 0x0 games
        if (gridSize <= 0) gridSize = 1;
        return gridSize;
    }

    public static boolean getSwap(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SWAP, context.getResources().getBoolean(R.bool.DEFAULT_SWAP_ENABLED));
    }

    public static boolean getAutosave(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AUTOSAVE,
                context.getResources().getBoolean(R.bool.DEFAULT_AUTOSAVE_ENABLED));
    }

    public static int getTimerType(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(TIMER_TYPE, String.valueOf(Timer.NO_TIMER)));
    }

    public static int getTimeAmount(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(TIMER, "0"));
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
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(DIFFICULTY,
                String.valueOf(context.getResources().getInteger(R.integer.DEFAULT_AI_DIFFICULTY))));
    }
}
