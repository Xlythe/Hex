package com.sam.hex;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Stats {
    public static long getTimePlayed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong("time_played", 0);
    }

    public static void incrementTimePlayed(Context context, long time) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong("time_played", getTimePlayed(context) + time).commit();
    }

    public static void setTimePlayed(Context context, long time) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong("time_played", time).commit();
    }

    public static long getGamesPlayed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong("games_played", 0);
    }

    public static void incrementGamesPlayed(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong("games_played", getGamesPlayed(context) + 1).commit();
    }

    public static void setGamesPlayed(Context context, long games) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong("games_played", games).commit();
    }

    public static long getGamesWon(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong("games_won", 0);
    }

    public static void incrementGamesWon(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong("games_won", getGamesWon(context) + 1).commit();
    }

    public static void setGamesWon(Context context, long games) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong("games_won", games).commit();
    }
}
