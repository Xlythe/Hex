package com.xlythe.hex.server;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/** Sends a single notification reply; failure is visible and never auto-retried twice. */
public final class OnlineReplyWorker extends Worker {
    public OnlineReplyWorker(@NonNull Context context,
                             @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    @NonNull @Override
    public Result doWork() {
        Context context = getApplicationContext();
        String sid = getInputData().getString(OnlineReplyReceiver.EXTRA_SID);
        String message = getInputData().getString(OnlineReplyReceiver.KEY_TEXT);
        OnlineNotificationState.Snapshot state = OnlineNotificationState.read(context);
        ServerCredentials credentials = new ServerCredentialStore(context).load();
        if (state == null || credentials == null || sid == null
                || !sid.equals(state.board.sid) || !state.uid.equals(credentials.session.uid)
                || message == null || message.isEmpty()
                || message.length() > GameChatStore.MAX_MESSAGE_LENGTH) return Result.failure();
        try {
            IgGameCenterClient.production("hex-inline-reply").command(
                    state.board, credentials.session, state.cursor,
                    "MSG", "message", message);
            OnlineNotifications.post(context, 1002, "Hex", "Reply sent", sid);
            return Result.success();
        } catch (Exception error) {
            OnlineNotifications.post(context, 1002, "Hex", "Reply failed. Open the game to try again.", sid);
            return Result.failure();
        }
    }
}
