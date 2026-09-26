package com.xlythe.hex.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.RemoteInput;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

/** Notification inline replies are queued durably before the receiver returns. */
public final class OnlineReplyReceiver extends BroadcastReceiver {
    static final String ACTION_REPLY = "com.xlythe.hex.REPLY";
    static final String EXTRA_SID = "sid";
    static final String KEY_TEXT = "reply";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_REPLY.equals(intent.getAction())) return;
        String sid = intent.getStringExtra(EXTRA_SID);
        Bundle input = RemoteInput.getResultsFromIntent(intent);
        CharSequence text = input == null ? null : input.getCharSequence(KEY_TEXT);
        String message = text == null ? "" : text.toString().trim();
        OnlineNotificationState.Snapshot state = OnlineNotificationState.read(context);
        if (state == null || sid == null || !sid.equals(state.board.sid)
                || message.isEmpty() || message.length() > GameChatStore.MAX_MESSAGE_LENGTH) return;
        Data data = new Data.Builder()
                .putString(EXTRA_SID, sid)
                .putString(KEY_TEXT, message)
                .build();
        WorkManager.getInstance(context).enqueue(new OneTimeWorkRequest.Builder(
                OnlineReplyWorker.class).setInputData(data)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build());
        OnlineNotifications.post(context, 1002, "Hex", "Sending reply…", sid);
    }
}
