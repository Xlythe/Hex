package com.xlythe.hex.server;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.xlythe.hex.server.IgGameCenterModels.Event;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.Member;

/** Periodic fallback for turns and chat after Android stops the activity process. */
public final class OnlineNotificationWorker extends Worker {
    public OnlineNotificationWorker(@NonNull Context context,
                                    @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        OnlineNotificationState.Snapshot state = OnlineNotificationState.read(context);
        if (state == null || !state.background) return Result.success();
        ServerCredentials credentials = new ServerCredentialStore(context).load();
        if (credentials == null || !credentials.session.uid.equals(state.uid)) return Result.success();

        try {
            HandlerResponse response = IgGameCenterClient.production("hex-background-check")
                    .refresh(state.board, credentials.session, state.cursor);
            Alerts alerts = alerts(response, state.uid, state.cursor);
            if (!OnlineNotificationState.finishPoll(context, state,
                    response.latestEventId(state.cursor))) return Result.success();
            if (alerts.turn) OnlineNotifications.post(context, 1001,
                    "Your turn in Hex", "Your opponent has moved.");
            if (alerts.message != null) OnlineNotifications.post(context, 1002,
                    alerts.sender, alerts.message);
            if ("FINISHED".equalsIgnoreCase(response.status)) {
                OnlineNotificationState.clear(context);
            }
            return Result.success();
        } catch (Exception error) {
            return Result.retry();
        }
    }

    static Alerts alerts(HandlerResponse response, String localUid, long cursor) {
        boolean opponentMoved = false;
        String sender = null;
        String message = null;
        for (Event event : response.events) {
            if (event.eid <= cursor || localUid.equals(event.uid)) continue;
            if ("MOVE".equals(event.type)) opponentMoved = true;
            if ("MSG".equals(event.type)) {
                message = event.data;
                sender = "Hex message";
                for (Member member : response.players) {
                    if (event.uid.equals(member.uid)) {
                        sender = member.name;
                        break;
                    }
                }
            }
        }
        return new Alerts(opponentMoved && response.localActive, sender, message);
    }

    static final class Alerts {
        final boolean turn;
        final String sender;
        final String message;

        Alerts(boolean turn, String sender, String message) {
            this.turn = turn;
            this.sender = sender;
            this.message = message;
        }
    }
}
