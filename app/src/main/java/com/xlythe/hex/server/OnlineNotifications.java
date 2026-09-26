package com.xlythe.hex.server;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.xlythe.hex.MainActivity;
import com.xlythe.hex.R;

/** Shared notification rendering for the activity poller and the persistent worker. */
public final class OnlineNotifications {
    private static final String CHANNEL = "hex_online";

    private OnlineNotifications() {}

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL, "Online Hex",
                NotificationManager.IMPORTANCE_DEFAULT);
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    public static void post(Context context, int id, String title, String message) {
        post(context, id, title, message, null);
    }

    public static void post(Context context, int id, String title, String message, String boardSid) {
        if (Build.VERSION.SDK_INT >= 33
                && ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return;
        ensureChannel(context);
        Intent open = new Intent(context, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, id, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pending);
        if (boardSid != null && id == 1002) {
            Intent reply = new Intent(context, OnlineReplyReceiver.class)
                    .setAction(OnlineReplyReceiver.ACTION_REPLY)
                    .putExtra(OnlineReplyReceiver.EXTRA_SID, boardSid);
            PendingIntent replyIntent = PendingIntent.getBroadcast(context,
                    boardSid.hashCode(), reply,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            RemoteInput input = new RemoteInput.Builder(OnlineReplyReceiver.KEY_TEXT)
                    .setLabel("Reply to opponent").build();
            notification.addAction(new NotificationCompat.Action.Builder(
                    R.drawable.icon, "Reply", replyIntent)
                    .addRemoteInput(input)
                    .setAllowGeneratedReplies(false)
                    .build());
        }
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(id, notification.build());
    }
}
