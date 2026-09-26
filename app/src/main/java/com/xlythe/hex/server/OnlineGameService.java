package com.xlythe.hex.server;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.xlythe.hex.BuildConfig;
import com.xlythe.hex.MainActivity;
import com.xlythe.hex.R;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Keeps one authenticated event stream alive for a match after the UI is minimized. */
public final class OnlineGameService extends Service {
    private static final String TAG = "HexOnlineStream";
    private static final int NOTIFICATION_ID = 1003;
    private static final String CHANNEL = "hex_live_game";
    private static volatile ServerNetworkPlayer visiblePlayer;
    private static volatile boolean running;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean stopped;
    private volatile HttpURLConnection connection;

    public static void start(Context context) {
        ContextCompat.startForegroundService(context,
                new Intent(context, OnlineGameService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, OnlineGameService.class));
    }

    public static void setVisiblePlayer(@Nullable ServerNetworkPlayer player) {
        visiblePlayer = player;
        if (player != null) player.requestRefresh();
    }

    public static boolean isRunning() { return running; }

    @Override
    public void onCreate() {
        super.onCreate();
        OnlineNotifications.ensureChannel(this);
        if (Build.VERSION.SDK_INT >= 26) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    CHANNEL, "Live Hex match", android.app.NotificationManager.IMPORTANCE_LOW);
            getSystemService(android.app.NotificationManager.class).createNotificationChannel(channel);
        }
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, NOTIFICATION_ID, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Hex match in progress")
                .setContentText("Live turns and chat are connected")
                .setContentIntent(pending)
                .setOngoing(true)
                .build();
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTIFICATION_ID, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (RuntimeException denied) {
            // The dataSync time budget can be exhausted on Android 15+.
            Log.w(TAG, "Live service unavailable; periodic fallback remains", denied);
            stopped = true;
            stopSelf();
            return;
        }
        running = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (stopped) return START_NOT_STICKY;
        // Repeated starts must not create parallel connections.
        if (!stopped && !executor.isShutdown() && streamStarted) {
            HttpURLConnection current = connection;
            if (current != null) current.disconnect();
            return START_STICKY;
        }
        stopped = false;
        streamStarted = true;
        executor.execute(this::streamLoop);
        return START_STICKY;
    }

    private volatile boolean streamStarted;

    private void streamLoop() {
        long backoffMs = 1000;
        while (!stopped) {
            OnlineNotificationState.Snapshot state = OnlineNotificationState.read(this);
            ServerCredentials credentials = new ServerCredentialStore(this).load();
            if (state == null || credentials == null
                    || !state.uid.equals(credentials.session.uid)) {
                stopSelf();
                return;
            }
            try {
                String query = "?sid=" + URLEncoder.encode(state.board.sid, "UTF-8")
                        + "&after=" + state.cursor;
                HttpURLConnection stream = (HttpURLConnection) URI.create(
                        BuildConfig.HEX_API_BASE_URL + "/events" + query).toURL().openConnection();
                connection = stream;
                stream.setConnectTimeout(10000);
                stream.setReadTimeout(65000);
                stream.setRequestProperty("Accept", "text/event-stream");
                stream.setRequestProperty("User-Agent", "HexAndroid/1.0");
                stream.setRequestProperty("Authorization", "Bearer " + credentials.session.sessionId);
                stream.setRequestProperty("X-Hex-Uid", credentials.session.uid);
                int status = stream.getResponseCode();
                if (status == 401 || status == 403) {
                    stream.disconnect();
                    connection = null;
                    OnlineNotificationState.clear(this);
                    stopSelf();
                    return;
                }
                if (status != 200) {
                    // The legacy endpoint has no stream. The player and worker still poll.
                    throw new java.io.IOException("Event stream HTTP " + status);
                }
                backoffMs = 1000;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        stream.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    boolean refresh = false;
                    while (!stopped && (line = reader.readLine()) != null) {
                        if (line.equals("event: refresh")) refresh = true;
                        if (line.isEmpty()) {
                            if (refresh) onRefresh();
                            refresh = false;
                        }
                    }
                } finally {
                    stream.disconnect();
                    connection = null;
                }
            } catch (Exception error) {
                if (!stopped) Log.w(TAG, "Event stream reconnecting", error);
            }
            if (!stopped) {
                // Catch events that arrived during reconnect, including on a server without SSE.
                onRefresh();
                try { Thread.sleep(backoffMs); } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
                backoffMs = Math.min(backoffMs * 2, IgGameCenterProtocol.POLL_INTERVAL_MS);
            }
        }
    }

    private void onRefresh() {
        ServerNetworkPlayer player = visiblePlayer;
        if (player != null) {
            player.requestRefresh();
            return;
        }
        OnlineNotificationState.Snapshot state = OnlineNotificationState.read(this);
        if (state == null || !state.background) return;
        ServerCredentials credentials = new ServerCredentialStore(this).load();
        if (credentials == null || !state.uid.equals(credentials.session.uid)) return;
        try {
            HandlerResponse response = IgGameCenterClient.production("hex-live-service")
                    .refresh(state.board, credentials.session, state.cursor);
            OnlineNotificationWorker.Alerts alerts = OnlineNotificationWorker.alerts(
                    response, state.uid, state.cursor);
            if (!OnlineNotificationState.finishPoll(this, state,
                    response.latestEventId(state.cursor))) return;
            if (alerts.turn) OnlineNotifications.post(this, 1001,
                    "Your turn in Hex", "Your opponent has moved.");
            if (alerts.message != null) OnlineNotifications.post(this, 1002,
                    alerts.sender, alerts.message, state.board.sid);
            if ("FINISHED".equalsIgnoreCase(response.status)) {
                OnlineNotificationState.clear(this);
                stopSelf();
            }
        } catch (Exception error) {
            Log.w(TAG, "Live refresh failed; retrying", error);
        }
    }

    @Override
    public void onTimeout(int startId, int fgsType) {
        // Android 15+ dataSync budget. WorkManager remains the recovery path.
        stopSelf();
    }

    @Override
    public void onDestroy() {
        stopped = true;
        running = false;
        HttpURLConnection current = connection;
        if (current != null) current.disconnect();
        executor.shutdownNow();
        super.onDestroy();
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return null; }
}
