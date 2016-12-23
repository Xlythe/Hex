package com.sam.hex;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * @author Will Harmon
 **/
public class Stats {
    private static final String KEY_TIME_PLAYED = "time_played";
    private static final String KEY_GAMES_PLAYED = "games_played";
    private static final String KEY_GAMES_WON = "games_won";
    private static final String KEY_DONATION_AMOUNT = "donation_amount";

    public static long getTimePlayed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_TIME_PLAYED, 0);
    }

    public static void incrementTimePlayed(Context context, long time) {
        time = Math.max(time, 0);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_TIME_PLAYED, getTimePlayed(context) + time).apply();
    }

    public static void setTimePlayed(Context context, long time) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_TIME_PLAYED, time).apply();
    }

    public static long getGamesPlayed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_GAMES_PLAYED, 0);
    }

    public static void incrementGamesPlayed(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_GAMES_PLAYED, getGamesPlayed(context) + 1).apply();
    }

    public static void setGamesPlayed(Context context, long games) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_GAMES_PLAYED, games).apply();
    }

    public static long getGamesWon(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_GAMES_WON, 0);
    }

    public static void incrementGamesWon(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_GAMES_WON, getGamesWon(context) + 1).apply();
    }

    public static void setGamesWon(Context context, long games) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(KEY_GAMES_WON, games).apply();
    }

    public static int getDonationRank(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_DONATION_AMOUNT, 0);
    }

    public static void incrementDonationRank(Context context, int amount) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_DONATION_AMOUNT, getDonationRank(context) + amount).apply();
    }

    public static void setDonationRank(Context context, int amount) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_DONATION_AMOUNT, amount).apply();
    }
}
