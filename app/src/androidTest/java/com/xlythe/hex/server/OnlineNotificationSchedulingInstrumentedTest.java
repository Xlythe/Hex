package com.xlythe.hex.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public final class OnlineNotificationSchedulingInstrumentedTest {
    @Test
    public void backgroundGameSchedulesPersistentNetworkCheck() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        OnlineNotificationState.remember(context,
                new BoardRef("schedule-test", "hex1"),
                new UserSession("local", "Local", "session"), 7);
        try {
            OnlineNotificationState.background(context);
            List<WorkInfo> scheduled = WorkManager.getInstance(context)
                    .getWorkInfosForUniqueWork(OnlineNotificationState.WORK)
                    .get(10, TimeUnit.SECONDS);
            assertEquals(1, scheduled.size());
            assertFalse(scheduled.get(0).getState().isFinished());
        } finally {
            OnlineNotificationState.clear(context);
        }
    }
}
