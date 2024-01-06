package com.xlythe.hex.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;

import com.hex.core.Point;

import androidx.annotation.NonNull;

/**
 * @author Will Harmon
 **/
public class SelectorLayout extends View implements OnTouchListener {
    private SelectorLayout.Button[] mButtons;
    private Paint mButtonTextPaint;
    private int mDisabledColor;
    private int mFocusedButton = -1;

    private int mWidth;
    private int mIndentHeight;
    private int mMargin;
    private float mRotation;
    private Rect[] mOldRect;
    private Rect[] mOldMirrorRect;
    private Point[] mOldTextPos;
    private boolean mIsLaidOut = false;

    public SelectorLayout(Context context) {
        super(context);
        setUp();
    }

    public SelectorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public SelectorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp();
    }

    public void setUp() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        setOnTouchListener(this);
        mButtons = new Button[3];
        for (int i = 0; i < mButtons.length; i++) {
            mButtons[i] = new Button(getContext());
        }
        mDisabledColor = getDarkerColor(Color.LTGRAY);
        mButtonTextPaint = new Paint();
        mButtonTextPaint.setColor(Color.WHITE);
        mButtonTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, dm));

        mWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, dm);
        mIndentHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, dm);
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, dm);
        mRotation = 45f;
        setOnClickListener(v -> {
            for (final Button b : mButtons) {
                if (b.isSelected() || b.isPressed()) {
                    final float initialTextX = b.textX;
                    final Rect initialButtonBounds = b.buttonDrawable.copyBounds();
                    final Rect initialMirrorButtonBounds = b.mirrorButtonDrawable.copyBounds();

                    ValueAnimator animator = ValueAnimator.ofInt(0, 3 * getHeight() / 2);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.addUpdateListener((valueAnimator) -> {
                        int value = (Integer) valueAnimator.getAnimatedValue();
                        b.buttonDrawable.setBounds(initialButtonBounds.left, initialButtonBounds.top - value,
                                initialButtonBounds.right, initialButtonBounds.bottom - value);
                        b.mirrorButtonDrawable.setBounds(initialMirrorButtonBounds.left, initialMirrorButtonBounds.top + value,
                                initialMirrorButtonBounds.right, initialMirrorButtonBounds.bottom + value);
                        b.textX = initialTextX + value;
                        invalidate();
                    });
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {}

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            b.performClick();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {}

                        @Override
                        public void onAnimationRepeat(Animator animator) {}
                    });
                    animator.start();
                }
            }
            invalidate();
        });
        setFocusable(true);
        setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mFocusedButton = 0;
                mButtons[0].setSelected(true);
                invalidate();
            } else {
                if (mFocusedButton != -1) {
                    mButtons[mFocusedButton].setSelected(false);
                    invalidate();
                }
            }
        });
    }

    @Override
    public View focusSearch(int direction) {
        mButtons[mFocusedButton].setSelected(false);
        switch (direction) {
            case View.FOCUS_RIGHT:
                switch (mFocusedButton) {
                    case 0:
                        mFocusedButton = 1;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 1:
                        mFocusedButton = 2;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                }
                break;
            case View.FOCUS_LEFT:
                switch (mFocusedButton) {
                    case 1:
                        mFocusedButton = 0;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 2:
                        mFocusedButton = 1;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                }
                break;
            case View.FOCUS_UP:
                break;
            case View.FOCUS_DOWN:
                break;
            case View.FOCUS_FORWARD:
                break;
            case View.FOCUS_BACKWARD:
                break;
        }
        return super.focusSearch(direction);
    }

    @Override
    public boolean isLaidOut() {
        if (Build.VERSION.SDK_INT < 19) {
            return mIsLaidOut;
        }
        return super.isLaidOut();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (!isLaidOut()) {
            return;
        }

        canvas.rotate(mRotation);
        for (Button button : mButtons) {
            if (!button.isEnabled()) {
                button.buttonDrawable.getPaint().setColor(mDisabledColor);
                button.mirrorButtonDrawable.getPaint().setColor(mDisabledColor);
            } else if (button.isPressed()) {
                button.buttonDrawable.getPaint().setColor(getDarkerColor(button.getColor()));
                button.mirrorButtonDrawable.getPaint().setColor(getDarkerColor(button.getColor()));
            } else if (button.isSelected()) {
                button.buttonDrawable.getPaint().setColor(getDarkerColor(button.getColor()));
                button.mirrorButtonDrawable.getPaint().setColor(getDarkerColor(button.getColor()));
            } else {
                button.buttonDrawable.getPaint().setColor(button.getColor());
                button.mirrorButtonDrawable.getPaint().setColor(button.getColor());
            }

            button.buttonDrawable.draw(canvas);
            button.mirrorButtonDrawable.draw(canvas);
            canvas.save();
            canvas.rotate(-90f, button.getHexagon().b.x, button.getHexagon().d.y / 2f);
            canvas.drawText(button.getText(), button.textX, button.textY, mButtonTextPaint);
            canvas.restore();
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        int diagonal = (int) Math.sqrt(w * w + h * h);
        int margin = (w - mWidth * 3) / 4;
        int offset = margin;
        // Create the buttons
        mOldRect = new Rect[mButtons.length];
        mOldMirrorRect = new Rect[mButtons.length];
        mOldTextPos = new Point[mButtons.length];

        for (int i = 0; i < mButtons.length; i++) {
            Hexagon hex = new Hexagon(new Point(offset, mIndentHeight - 3 * offset), new Point(mWidth / 2 + offset, -3 * offset), new Point(mWidth + offset,
                    mIndentHeight - 3 * offset), new Point(mWidth + offset, h - offset), new Point(mWidth / 2 + offset, h - mIndentHeight - offset), new Point(
                    offset, h - offset));

            // Shape of a pressed state
            Path buttonPath = new Path();
            buttonPath.moveTo(hex.a.x, Math.max(hex.a.y, -diagonal));
            buttonPath.lineTo(hex.b.x, Math.max(hex.b.y, -diagonal));
            buttonPath.lineTo(hex.c.x, Math.max(hex.c.y, -diagonal));
            buttonPath.lineTo(hex.d.x, hex.d.y);
            buttonPath.lineTo(hex.e.x, hex.e.y);
            buttonPath.lineTo(hex.f.x, hex.f.y);
            buttonPath.close();

            Hexagon mirrorHex = new Hexagon(new Point(offset, mIndentHeight - offset), new Point(mWidth / 2 + offset, -offset), new Point(mWidth + offset,
                    mIndentHeight - offset), new Point(mWidth + offset, h - offset), new Point(mWidth / 2 + offset, h - mIndentHeight - offset), new Point(
                    offset, h - offset));

            // Shape of a pressed state
            Path mirrorButtonPath = new Path();
            mirrorButtonPath.moveTo(mirrorHex.a.x, mirrorHex.a.y);
            mirrorButtonPath.lineTo(mirrorHex.b.x, mirrorHex.b.y);
            mirrorButtonPath.lineTo(mirrorHex.c.x, mirrorHex.c.y);
            mirrorButtonPath.lineTo(mirrorHex.d.x, mirrorHex.d.y);
            mirrorButtonPath.lineTo(mirrorHex.e.x, mirrorHex.e.y);
            mirrorButtonPath.lineTo(mirrorHex.f.x, mirrorHex.f.y);
            mirrorButtonPath.close();

            int heightOffset = (int) (3.6 * mIndentHeight);
            mButtons[i].buttonDrawable = new ShapeDrawable(new PathShape(buttonPath, w, h));
            mButtons[i].buttonDrawable.setBounds(0, -heightOffset + h / 2, w, h - heightOffset + h / 2);
            mButtons[i].mirrorButtonDrawable = new ShapeDrawable(new PathShape(mirrorButtonPath, w, h));
            mButtons[i].mirrorButtonDrawable.setBounds(0, (h - mIndentHeight) + mMargin - heightOffset + h / 2, w, (2 * h - mIndentHeight) + mMargin - heightOffset + h
                    / 2);

            mButtons[i].setHexagon(hex);

            mButtons[i].textX = mButtons[i].getHexagon().b.x * 2 - mButtonTextPaint.measureText(mButtons[i].getText()) / 2 - (int) (1.7 * i * margin);
            mButtons[i].textY = mButtons[i].getHexagon().d.y / 2f + mButtonTextPaint.getTextSize() / 4;

            mOldRect[i] = mButtons[i].buttonDrawable.copyBounds();
            mOldMirrorRect[i] = mButtons[i].mirrorButtonDrawable.copyBounds();
            mOldTextPos[i] = new Point((int) mButtons[i].textX, (int) mButtons[i].textY);

            offset += margin + mWidth;
        }

        mIsLaidOut = true;
    }

    @Override
    public boolean onTouch(View v, @NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (Button b : mButtons) {
                if (b.getHexagon().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    b.setPressed(b.isEnabled());
                } else {
                    b.setPressed(false);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            for (Button b : mButtons) {
                if (b.isPressed()) {
                    performClick();
                }
                b.setPressed(false);
            }
        } else {
            for (Button b : mButtons) {
                if (b.isPressed()) {
                    if (!b.getHexagon().contains(new Point((int) event.getX(), (int) event.getY()))) {
                        b.setPressed(false);
                    }
                }
            }
        }

        invalidate();
        return true;
    }

    private int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public Button[] getButtons() {
        return mButtons;
    }

    public void reset() {
        if (mOldRect != null) {
            for (int i = 0; i < mButtons.length; i++) {
                mButtons[i].buttonDrawable.setBounds(mOldRect[i]);
                mButtons[i].mirrorButtonDrawable.setBounds(mOldMirrorRect[i]);
                mButtons[i].textX = mOldTextPos[i].x;
                mButtons[i].textY = mOldTextPos[i].y;
            }
            invalidate();
        }
    }

    private class Hexagon {
        private final Point a, b, c, d, e, f;
        @NonNull
        private final Matrix m;
        @NonNull
        private final float[] points;

        private Hexagon(Point a, Point b, Point c, Point d, Point e, Point f) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
            points = new float[2];
            m = new Matrix();
            m.postRotate(-mRotation);
        }

        public boolean contains(@NonNull Point p) {
            points[0] = p.x;
            points[1] = p.y;
            m.mapPoints(points);
            p.x = (int) points[0];
            p.y = (int) points[1];

            return p.x > a.x && p.x < c.x;
        }
    }

    public static class Button {
        private final Context context;
        private SelectorLayout.Button.OnClickListener onClickListener;
        private String text;
        private int color;
        private Hexagon hexagon;
        private boolean pressed = false;
        private boolean enabled = true;
        private float textX;
        private float textY;
        private boolean selected;
        private ShapeDrawable buttonDrawable;
        private ShapeDrawable mirrorButtonDrawable;

        public Button(Context context) {
            this.context = context;
        }

        public interface OnClickListener {
            void onClick();
        }

        public void setOnClickListener(SelectorLayout.Button.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        public OnClickListener getOnClickListener() {
            return onClickListener;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setText(int resId) {
            setText(context.getString(resId));
        }

        public String getText() {
            return text;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        private void setHexagon(Hexagon hexagon) {
            this.hexagon = hexagon;
        }

        private Hexagon getHexagon() {
            return hexagon;
        }

        public void performClick() {
            if (onClickListener != null) onClickListener.onClick();
        }

        protected boolean isPressed() {
            return pressed;
        }

        protected void setPressed(boolean pressed) {
            this.pressed = pressed;
        }

        protected boolean isSelected() {
            return selected;
        }

        protected void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
