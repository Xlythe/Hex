package com.xlythe.hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.xlythe.hex.server.IgGameCenterClient;
import com.xlythe.hex.server.IgGameCenterProtocol;
import com.xlythe.hex.server.OnlineGameService;
import com.xlythe.hex.server.OnlineNotificationState;
import com.xlythe.hex.server.ServerCredentialStore;
import com.xlythe.hex.server.ServerCredentials;
import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.util.LinkedHashMap;

/** Device-level POST/XML smoke test against the live mirror. */
@RunWith(AndroidJUnit4.class)
public final class LiveMirrorInstrumentedTest {
    @Rule public GrantPermissionRule notifications = GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS);

    @Test
    public void twoAndroidClientsCanPlayAndChat() throws Exception {
        String suffix = Long.toString(System.currentTimeMillis(), 36);
        IgGameCenterClient first = IgGameCenterClient.production("device-first-" + suffix);
        IgGameCenterClient second = IgGameCenterClient.production("device-second-" + suffix);
        String password = "test password 2026";
        String firstName = "AndroidA" + suffix;
        String secondName = "AndroidB" + suffix;
        first.register(firstName, password, null);
        second.register(secondName, password, null);
        UserSession alice = first.login(firstName, password);
        UserSession bob = second.login(secondName, password);

        BoardRef board = first.createBoard(alice, "1");
        assertFalse(board.sid.isEmpty());
        LinkedHashMap<String, String> setup = new LinkedHashMap<>();
        setup.put("boardSize", "11");
        first.handle(board, alice, 0, "SETUP", setup);
        assertTrue(second.fetchLobby(bob).stream().anyMatch(room -> board.sid.equals(room.sid)));
        second.handle(board, bob, 0, null, null);
        second.command(board, bob, 0, "PLACE", "place", "2");
        first.command(board, alice, 0, "START");
        HandlerResponse started = second.command(board, bob, 0, "START");
        assertEquals("ACTIVE", started.status);
        assertFalse(started.localActive);

        first.command(board, alice, 0, "MOVE", "move", "A1");
        HandlerResponse seen = second.refresh(board, bob, 0);
        assertEquals('1', seen.board.charAt(0));
        assertTrue(seen.localActive);
        second.command(board, bob, seen.latestEventId(0), "MSG", "message", "Android chat");
        assertTrue(first.refresh(board, alice, 0).events.stream()
                .anyMatch(event -> "MSG".equals(event.type) && "Android chat".equals(event.data)));
    }

    @Test
    public void backgroundStreamDeliversChatAndInlineReply() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        String suffix = Long.toString(System.currentTimeMillis(), 36);
        String password = "test password 2026";
        IgGameCenterClient first = IgGameCenterClient.production("live-first-" + suffix);
        IgGameCenterClient second = IgGameCenterClient.production("live-second-" + suffix);
        String firstName = "StreamA" + suffix;
        String secondName = "StreamB" + suffix;
        first.register(firstName, password, null);
        second.register(secondName, password, null);
        UserSession alice = first.login(firstName, password);
        UserSession bob = second.login(secondName, password);
        BoardRef board = first.createBoard(alice, "1");
        second.handle(board, bob, 0, null, null);
        second.command(board, bob, 0, "PLACE", "place", "2");
        first.command(board, alice, 0, "START");
        HandlerResponse started = second.command(board, bob, 0, "START");
        long cursor = started.latestEventId(0);
        new ServerCredentialStore(context).save(new ServerCredentials(
                secondName, IgGameCenterProtocol.md5(password), bob));
        OnlineNotificationState.remember(context, board, bob, cursor);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        try {
            try (ActivityScenario<MainActivity> activity = ActivityScenario.launch(MainActivity.class)) {
                activity.onActivity(OnlineGameService::start);
                assertTrue(await(() -> OnlineGameService.isRunning(), 5000));
            }
            OnlineNotificationState.background(context);
            first.command(board, alice, 0, "MOVE", "move", "A1");
            assertTrue("The live stream did not post a turn within five seconds",
                    await(() -> findNotification(manager, 1001) != null, 5000));
            first.command(board, alice, 0, "MSG", "message", "Live background message");
            assertTrue("The live stream did not post a message within five seconds",
                    await(() -> findNotification(manager, 1002) != null, 5000));
            Notification message = findNotification(manager, 1002);
            assertEquals("Live background message",
                    message.extras.getCharSequence(Notification.EXTRA_TEXT).toString());
            assertTrue(message.actions != null && message.actions.length > 0);
            assertTrue(message.actions[0].getRemoteInputs() != null);

            Intent reply = new Intent();
            Bundle text = new Bundle();
            text.putCharSequence("reply", "Inline Android reply");
            android.app.RemoteInput.addResultsToIntent(
                    message.actions[0].getRemoteInputs(), reply, text);
            message.actions[0].actionIntent.send(context, 0, reply);
            assertTrue("The inline reply did not reach the opponent",
                    await(() -> {
                        try {
                            return first.refresh(board, alice, 0).events.stream().anyMatch(
                                    event -> "MSG".equals(event.type)
                                            && "Inline Android reply".equals(event.data));
                        } catch (Exception ignored) { return false; }
                    }, 10000));
        } finally {
            OnlineGameService.stop(context);
            OnlineNotificationState.clear(context);
            new ServerCredentialStore(context).clear();
            manager.cancel(1002);
            manager.cancel(1001);
        }
    }

    private static Notification findNotification(NotificationManager manager, int id) {
        for (StatusBarNotification notification : manager.getActiveNotifications()) {
            if (notification.getId() == id) return notification.getNotification();
        }
        return null;
    }

    private static boolean await(java.util.concurrent.Callable<Boolean> check, long timeoutMs)
            throws Exception {
        long deadline = SystemClock.elapsedRealtime() + timeoutMs;
        while (SystemClock.elapsedRealtime() < deadline) {
            if (check.call()) return true;
            SystemClock.sleep(100);
        }
        return check.call();
    }
}
