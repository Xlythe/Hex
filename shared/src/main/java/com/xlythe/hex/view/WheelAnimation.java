package com.xlythe.hex.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import androidx.annotation.RequiresApi;
import android.os.Build;
import android.view.animation.DecelerateInterpolator;
import android.view.View;
import android.view.animation.AnimationUtils;

/** Uses the platform animation clock where available and the view's message queue on Eclair. */
@SuppressLint("ObsoleteSdkInt") // Shared by the API 7 and API 24 applications.
public abstract class WheelAnimation {
    interface Frame { void draw(float value); }
    public abstract void cancel();
    public abstract void end();

    static WheelAnimation start(View view, float from, float to, long duration, Frame frame) {
        if (Build.VERSION.SDK_INT >= 11) return Platform.start(from, to, duration, frame);
        Legacy animation = new Legacy(view, from, to, duration, frame);
        animation.run();
        return animation;
    }

    @RequiresApi(11)
    private static class Platform {
        static WheelAnimation start(float from, float to, long duration, Frame frame) {
            ValueAnimator animator = ValueAnimator.ofFloat(from, to);
            animator.setDuration(duration);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(value -> frame.draw((Float) value.getAnimatedValue()));
            animator.start();
            return new WheelAnimation() {
                public void cancel() { animator.cancel(); }
                public void end() { animator.end(); }
            };
        }
    }

    private static final class Legacy extends WheelAnimation implements Runnable {
        private final View view;
        private final Frame frame;
        private final float from, to;
        private final long start = AnimationUtils.currentAnimationTimeMillis();
        private final long duration;

        Legacy(View view, float from, float to, long duration, Frame frame) {
            this.view = view;
            this.from = from;
            this.to = to;
            this.duration = duration;
            this.frame = frame;
        }

        @Override public void run() {
            float remaining = Math.max(0f, 1f - (float) (AnimationUtils.currentAnimationTimeMillis() - start) / duration);
            frame.draw(to + (from - to) * remaining * remaining);
            if (remaining > 0f) view.postDelayed(this, 16);
        }

        @Override public void cancel() { view.removeCallbacks(this); }
        @Override public void end() { cancel(); frame.draw(to); }
    }
}
