package com.xlythe.hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hex.core.Point;
import com.xlythe.hex.view.HexagonLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

@RunWith(AndroidJUnit4.class)
public class WheelGestureInstrumentedTest {
    private HexagonLayout wheel;
    private Point center;
    private int clicks;
    private long down;

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        wheel = new HexagonLayout(context);
        wheel.setText("Hex");
        for (HexagonLayout.Button button : wheel.getButtons()) {
            button.setText("Play");
            button.setDrawableResource(R.drawable.play);
            button.setOnClickListener(() -> clicks++);
        }
        wheel.layout(0, 0, 640, 420);
        center = (Point) field("center").get(wheel);
    }

    private Field field(String name) throws Exception {
        Field result = HexagonLayout.class.getDeclaredField(name);
        result.setAccessible(true);
        return result;
    }

    private float rotation() throws Exception {
        return field("mRotation").getFloat(wheel);
    }

    private void send(int action, long time, float x, float y) {
        if (action == MotionEvent.ACTION_DOWN) down = time;
        MotionEvent event = MotionEvent.obtain(down, time, action, center.x + x, center.y + y, 0);
        try {
            wheel.dispatchTouchEvent(event);
        } finally {
            event.recycle();
        }
    }

    @Test @UiThreadTest
    public void tapWithinSlopPressesAndReleasesExactlyOneButton() throws Exception {
        float jitter = ViewConfiguration.get(wheel.getContext()).getScaledTouchSlop() / 2f;
        send(MotionEvent.ACTION_DOWN, 1000, 0, -130);
        ((ValueAnimator) field("mPressAnimator").get(wheel)).end();
        assertEquals(1f, field("mPressAmount").getFloat(wheel), .01f);
        send(MotionEvent.ACTION_MOVE, 1016, jitter, -130);
        assertEquals(0f, rotation(), .01f);
        send(MotionEvent.ACTION_UP, 1032, jitter, -130);
        assertEquals(1, clicks);
        ((ValueAnimator) field("mPressAnimator").get(wheel)).end();
        assertEquals(0f, field("mPressAmount").getFloat(wheel), .01f);
    }

    @Test @UiThreadTest
    public void crossingCenterReanchorsWithoutTurningHalfACircle() throws Exception {
        send(MotionEvent.ACTION_DOWN, 1000, -130, 0);
        send(MotionEvent.ACTION_MOVE, 1016, 130, 0);
        assertEquals(0f, rotation(), .01f);
        send(MotionEvent.ACTION_MOVE, 1032, 130, 28);
        assertTrue(rotation() > 0 && rotation() < 15);
        send(MotionEvent.ACTION_CANCEL, 1048, 130, 28);
        assertEquals(0, clicks);
    }

    @Test @UiThreadTest
    public void flickTravelsFartherThanADragReleasedAfterAPause() throws Exception {
        float slow = quarterTurn(150);
        float fast = quarterTurn(0);
        assertEquals(0f, slow % 60, .01f);
        assertEquals(0f, fast % 60, .01f);
        assertTrue(slow >= 60 && slow <= 120);
        assertTrue(fast > slow);
        assertEquals(0, clicks);
    }

    private float quarterTurn(int pause) throws Exception {
        wheel.setInitialRotation(0);
        send(MotionEvent.ACTION_DOWN, 1000, 0, -130);
        send(MotionEvent.ACTION_MOVE, 1032, 92, -92);
        send(MotionEvent.ACTION_MOVE, 1064, 130, 0);
        send(MotionEvent.ACTION_UP, 1064 + pause, 130, 0);
        ((ValueAnimator) field("mAnimator").get(wheel)).end();
        return rotation();
    }
}
