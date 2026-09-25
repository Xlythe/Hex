package com.xlythe.hex.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import java.util.concurrent.TimeUnit;

/** Non-secret game cursor used by the process-resilient notification check. */
@SuppressLint("ApplySharedPref") // A killed process must not lose the board or event cursor.
public final class OnlineNotificationState {
    private static final String PREFS = "online_notification_state";
    static final String WORK = "online_hex_notifications";

    private OnlineNotificationState() {}

    public static synchronized void remember(Context context, BoardRef board,
                                             UserSession user, long cursor) {
        preferences(context).edit()
                .putString("sid", board.sid)
                .putString("server", board.server)
                .putString("uid", user.uid)
                .putLong("cursor", cursor)
                .commit();
    }

    public static synchronized void advance(Context context, String sid, long cursor) {
        SharedPreferences state = preferences(context);
        if (sid.equals(state.getString("sid", null))) {
            state.edit().putLong("cursor", Math.max(cursor, state.getLong("cursor", 0))).commit();
        }
    }

    static synchronized Snapshot read(Context context) {
        SharedPreferences state = preferences(context);
        String sid = state.getString("sid", null);
        String server = state.getString("server", null);
        String uid = state.getString("uid", null);
        if (sid == null || server == null || uid == null
                || !IgGameCenterProtocol.isValidServerName(server)) return null;
        return new Snapshot(new BoardRef(sid, server), uid,
                state.getLong("cursor", 0), state.getBoolean("background", false));
    }

    /** Only accept a fetched result while the same game remains backgrounded. */
    static synchronized boolean finishPoll(Context context, Snapshot fetched, long cursor) {
        Snapshot current = read(context);
        if (current == null || !current.background || !current.board.sid.equals(fetched.board.sid)
                || !current.uid.equals(fetched.uid) || current.cursor > fetched.cursor) return false;
        advance(context, fetched.board.sid, cursor);
        return true;
    }

    public static synchronized void foreground(Context context) {
        preferences(context).edit().putBoolean("background", false).commit();
        WorkManager.getInstance(context).cancelUniqueWork(WORK);
    }

    public static synchronized void background(Context context) {
        if (read(context) == null) return;
        preferences(context).edit().putBoolean("background", true).commit();
        Constraints network = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                OnlineNotificationWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(network).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    public static synchronized void clear(Context context) {
        preferences(context).edit().clear().commit();
        WorkManager.getInstance(context).cancelUniqueWork(WORK);
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static final class Snapshot {
        final BoardRef board;
        final String uid;
        final long cursor;
        final boolean background;

        Snapshot(BoardRef board, String uid, long cursor, boolean background) {
            this.board = board;
            this.uid = uid;
            this.cursor = cursor;
            this.background = background;
        }
    }
}
