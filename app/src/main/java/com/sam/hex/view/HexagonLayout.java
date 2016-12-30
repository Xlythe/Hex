package com.sam.hex.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import com.hex.core.Point;

import static com.sam.hex.Settings.TAG;

/**
 * @author Will Harmon
 **/
public class HexagonLayout extends View implements OnTouchListener {
    // Rotation variables
    private boolean mAllowRotation;
    private float mRotation;
    private float mRotationOffset;
    private float mOldRotation;
    private float[] mRotationHistory;
    private ValueAnimator mAnimator;

    // Size and shape variables
    private Point[] corners;
    private Point center;
    private float mTopMargin;

    // Background variables
    private ShapeDrawable mHexagon;
    private int mBackgroundColor;

    // Title text variables
    private String mText;
    private float mTextX;
    private float mTextY;
    private float mTextSize;
    private float mTextPadding;
    private Paint mTextPaint;
    private ShapeDrawable mTextBackground;

    // Button variables
    private HexagonLayout.Button[] mButtons;
    private ShapeDrawable[] mBorder;
    private float mBorderWidth;
    private float mBorderShadowWidth;
    private ShapeDrawable[] mBorderShadow;
    private ShapeDrawable[] mPressedState;
    private Paint mLinePaint;
    private Paint mShadowLinePaint;
    private Paint mButtonTextPaint;
    private float mLineOffset;
    private int mPressedColor;
    private int mDisabledColor;
    private int mFocusedButton = -1;

    public HexagonLayout(Context context) {
        super(context);
        setUp();
    }

    public HexagonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public HexagonLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp();
    }

    public void setUp() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        setOnTouchListener(this);
        mRotation = 0;
        mBackgroundColor = 0xfff1f1f1;
        mButtons = new Button[6];
        for (int i = 0; i < 6; i++) {
            mButtons[i] = new Button(getContext());
        }
        mPressedColor = Color.LTGRAY;
        mDisabledColor = getDarkerColor(mPressedColor);
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 44, dm);
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm);
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.LTGRAY);
        mShadowLinePaint = new Paint();
        mShadowLinePaint.setColor(Color.WHITE);
        mLineOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, dm);
        mButtonTextPaint = new Paint();
        mButtonTextPaint.setColor(Color.WHITE);
        mButtonTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, dm));
        mAllowRotation = true;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Button b : mButtons) {
                    if (b.isSelected() || b.isPressed()) {
                        b.performClick();
                    }
                }
            }
        });
        setFocusable(true);
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFocusedButton = 3;
                    mButtons[3].setSelected(true);
                    invalidate();
                } else {
                    if (mFocusedButton != -1) {
                        mButtons[mFocusedButton].setSelected(false);
                        invalidate();
                    }
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
                    case 2:
                        mFocusedButton = 1;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 3:
                        mFocusedButton = 2;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 4:
                        mFocusedButton = 5;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 5:
                        mFocusedButton = 0;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                }
                break;
            case View.FOCUS_LEFT:
                switch (mFocusedButton) {
                    case 0:
                        mFocusedButton = 5;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 1:
                        mFocusedButton = 2;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 2:
                        mFocusedButton = 3;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 5:
                        mFocusedButton = 4;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                }
                break;
            case View.FOCUS_UP:
                switch (mFocusedButton) {
                    case 1:
                        mFocusedButton = 0;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 2:
                        mFocusedButton = 5;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 3:
                        mFocusedButton = 4;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                }
                break;
            case View.FOCUS_DOWN:
                switch (mFocusedButton) {
                    case 0:
                        mFocusedButton = 1;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 5:
                        mFocusedButton = 2;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                    case 4:
                        mFocusedButton = 3;
                        mButtons[mFocusedButton].setSelected(true);
                        invalidate();
                        return this;
                }
                break;
            case View.FOCUS_FORWARD:
                break;
            case View.FOCUS_BACKWARD:
                break;
        }
        return super.focusSearch(direction);
    }

    private void layoutText() {
        final int screenWidth = center.x * 2;
        final int screenHeight = center.y * 2;

        Paint paint = mTextPaint;
        if (mTextSize != 0f) paint.setTextSize(mTextSize);
        float textWidth = paint.measureText(mText);
        float width = screenWidth;
        float textSize = mTextSize;
        if (textWidth > width) {
            paint.setTextSize(textSize * width / textWidth);
            mTextX = getPaddingLeft();
            mTextSize = textSize;
        } else {
            mTextX = (screenWidth - textWidth) / 2;
        }
        mTextY = (screenHeight - paint.ascent() - paint.descent()) / 2;

        mTextBackground = new ShapeDrawable(new OvalShape());
        mTextBackground.getPaint().setColor(mBackgroundColor);
        mTextBackground.setBounds((int) (center.x - textWidth / 2 - mTextPadding), (int) (center.y - textSize / 2 - mTextPadding), (int) (center.x + textWidth
                / 2 + mTextPadding), (int) (center.y + textSize / 2 + mTextPadding));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.save();
        if (mAllowRotation) {
            canvas.rotate(mRotation, center.x, center.y);
        }

        mHexagon.draw(canvas);

        canvas.drawLine(corners[0].x + mLineOffset, corners[0].y, center.x + mLineOffset, center.y, mShadowLinePaint);
        canvas.drawLine(corners[1].x - mLineOffset, corners[1].y, center.x - mLineOffset, center.y, mShadowLinePaint);
        canvas.drawLine(corners[2].x, corners[2].y + mLineOffset, center.x, center.y + mLineOffset, mShadowLinePaint);
        canvas.drawLine(corners[3].x - mLineOffset, corners[3].y, center.x - mLineOffset, center.y, mShadowLinePaint);
        canvas.drawLine(corners[4].x + mLineOffset, corners[4].y, center.x + mLineOffset, center.y, mShadowLinePaint);
        canvas.drawLine(corners[5].x, corners[5].y + mLineOffset, center.x, center.y + mLineOffset, mShadowLinePaint);

        canvas.drawLine(corners[0].x, corners[0].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[1].x, corners[1].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[2].x, corners[2].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[3].x, corners[3].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[4].x, corners[4].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[5].x, corners[5].y, center.x, center.y, mLinePaint);

        mTextBackground.draw(canvas);

        for (int i = 0; i < 6; i++) {
            if (mButtons[i].isPressed()) {
                mPressedState[i].getPaint().setColor(mPressedColor);
                mPressedState[i].draw(canvas);
            }
            if (!mButtons[i].isEnabled()) {
                mPressedState[i].getPaint().setColor(mDisabledColor);
                mPressedState[i].draw(canvas);
            }
            if (mButtons[i].isSelected()) {
                mPressedState[i].getPaint().setColor(mPressedColor);
                mPressedState[i].draw(canvas);
            }
        }

        canvas.save();
        for (int i = 0; i < 6; i++) {
            canvas.rotate(60, center.x, center.y);
            mBorderShadow[i].draw(canvas);
            mBorder[i].draw(canvas);
            mButtons[i].getDrawable().draw(canvas);
            canvas.drawText(mButtons[i].getText(), center.x - mButtonTextPaint.measureText(mButtons[i].getText()) / 2,
                    (mBorderWidth + mButtonTextPaint.getTextSize()) / 2 + (int) mTopMargin, mButtonTextPaint);
        }
        canvas.restore();

        canvas.restore();

        canvas.drawText(mText, mTextX, mTextY, mTextPaint);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int desiredHeight = MeasureSpec.getSize(heightMeasureSpec) - lp.topMargin - lp.bottomMargin;
        int desiredWidth = (int) (desiredHeight * 1.1547);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        // Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            // Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            // Be whatever you want
            width = desiredWidth;
        }

        // Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            // Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            // Be whatever you want
            height = desiredHeight;
        }

        // MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        h -= mTopMargin;
        if (mAllowRotation) {
            h = (int) (h * 1.2);
        }
        w = (int) (h * 1.1547);
        mBorderShadowWidth = h / 2 * 0.1828f;
        mBorderWidth = mBorderShadowWidth * 0.882f;
        mBorderShadowWidth -= mBorderWidth;

        // Create a box

        center = new Point(w / 2, (int) (h / 2 + mTopMargin));

        // Get the length of a side of the hexagon
        int s = w / 2;

        // Create an array of the corners
        corners = new Point[6];
        corners[0] = new Point(w / 4, (int) mTopMargin);
        corners[1] = new Point(3 * w / 4, (int) mTopMargin);
        corners[2] = new Point(w, h / 2 + (int) mTopMargin);
        corners[3] = new Point(3 * w / 4, h + (int) mTopMargin);
        corners[4] = new Point(w / 4, h + (int) mTopMargin);
        corners[5] = new Point(0, h / 2 + (int) mTopMargin);

        // Shape of a hexagon
        Path hexagonPath = new Path();
        hexagonPath.moveTo(corners[0].x, corners[0].y);
        hexagonPath.lineTo(corners[1].x, corners[1].y);
        hexagonPath.lineTo(corners[2].x, corners[2].y);
        hexagonPath.lineTo(corners[3].x, corners[3].y);
        hexagonPath.lineTo(corners[4].x, corners[4].y);
        hexagonPath.lineTo(corners[5].x, corners[5].y);
        hexagonPath.close();

        mHexagon = new ShapeDrawable(new PathShape(hexagonPath, w, h));
        mHexagon.getPaint().setColor(mBackgroundColor);
        mHexagon.setBounds(0, 0, w, h);

        // Create the buttons
        mPressedState = new ShapeDrawable[6];
        mBorder = new ShapeDrawable[6];
        mBorderShadow = new ShapeDrawable[6];
        // Shape of an edge
        Path edgePath = new Path();
        edgePath.moveTo(corners[0].x, corners[0].y);
        edgePath.lineTo(corners[1].x, corners[1].y);
        edgePath.lineTo((int) (corners[1].x - mBorderWidth / 1.732), mBorderWidth + (int) mTopMargin);
        edgePath.lineTo((int) (corners[0].x + mBorderWidth / 1.732), mBorderWidth + (int) mTopMargin);
        edgePath.close();
        // Shape of an edge
        Path shadowEdgePath = new Path();
        shadowEdgePath.moveTo(corners[0].x, corners[0].y);
        shadowEdgePath.lineTo(corners[1].x, corners[1].y);
        shadowEdgePath.lineTo((int) (corners[1].x - (mBorderWidth + mBorderShadowWidth) / 1.732), (mBorderWidth + mBorderShadowWidth + (int) mTopMargin));
        shadowEdgePath.lineTo((int) (corners[0].x + (mBorderWidth + mBorderShadowWidth) / 1.732), (mBorderWidth + mBorderShadowWidth + (int) mTopMargin));
        shadowEdgePath.close();
        for (int i = 0; i < 6; i++) {
            Triangle t = new Triangle(new Point(corners[(i + 1) % 6].x, corners[(i + 1) % 6].y), new Point(corners[(i + 2) % 6].x, corners[(i + 2) % 6].y),
                    new Point(center.x, center.y));
            // Shape of a pressed state
            Path pressedStatePath = new Path();
            pressedStatePath.moveTo(t.a.x, t.a.y);
            pressedStatePath.lineTo(t.b.x, t.b.y);
            pressedStatePath.lineTo(t.c.x, t.c.y);
            pressedStatePath.close();

            mPressedState[i] = new ShapeDrawable(new PathShape(pressedStatePath, w, h));
            mPressedState[i].setBounds(0, 0, w, h);

            mButtons[i].setTriangle(t);

            int drawableWidth = s / 4;
            mButtons[i].getDrawable().setBounds(center.x - drawableWidth / 2, center.y / 2 - drawableWidth / 2, center.x + drawableWidth / 2,
                    center.y / 2 + drawableWidth / 2);

            mBorder[i] = new ShapeDrawable(new PathShape(edgePath, w, h));
            mBorder[i].getPaint().setColor(mButtons[i].getColor());
            mBorder[i].setBounds(0, 0, w, h);

            mBorderShadow[i] = new ShapeDrawable(new PathShape(shadowEdgePath, w, h));
            mBorderShadow[i].getPaint().setColor(getDarkerColor(mButtons[i].getColor()));
            mBorderShadow[i].setBounds(0, 0, w, h);
        }

        layoutText();
    }

    @Override
    public boolean onTouch(View v, @NonNull MotionEvent event) {
        int sign = (event.getX() > center.x) ? -1 : 1;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mRotationOffset = sign * cosineInverse(center, new Point(center.x, 0), new Point((int) event.getX(), (int) event.getY()));
            mOldRotation = mRotation;
            for (Button b : mButtons) {
                if (b.getTriangle().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    b.setPressed(b.isEnabled());
                } else {
                    b.setPressed(false);
                }
            }
            if (mAnimator != null) {
                mAnimator.cancel();
            }
            mRotationHistory = new float[4];
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            boolean performClick = false;
            for (Button b : mButtons) {
                if (b.isPressed()) {
                    performClick();
                    performClick = true;
                }
                b.setPressed(false);
            }
            if (!performClick) {
                mRotationHistory[3] = mRotationHistory[2];
                mRotationHistory[2] = mRotationHistory[1];
                mRotationHistory[1] = mRotationHistory[0];
                mRotationHistory[0] = mRotation % 360;
                spin(mRotation - getAverage(mRotationHistory));
            }
        } else {
            mRotation = mOldRotation + mRotationOffset - sign
                    * cosineInverse(center, new Point(center.x, 0), new Point((int) event.getX(), (int) event.getY()));
            mRotation = mRotation % 360;
            for (Button b : mButtons) {
                if (b.isPressed()) {
                    if (!b.getTriangle().contains(new Point((int) event.getX(), (int) event.getY()))) {
                        b.setPressed(false);
                    } else if (Math.abs(Math.abs(mRotation) - Math.abs(mOldRotation)) > 10f) {
                        b.setPressed(false);
                    }
                }
            }
            mRotationHistory[3] = mRotationHistory[2];
            mRotationHistory[2] = mRotationHistory[1];
            mRotationHistory[1] = mRotationHistory[0];
            mRotationHistory[0] = mRotation % 360;
        }

        invalidate();
        return true;
    }

    private float getAverage(@NonNull float[] data) {
        float sum = 0;
        for (float f : data) {
            sum += f;
        }
        return sum / data.length;
    }

    private void spin(float velocity) {
        spinExactly(2.3f * velocity, false);
    }

    private void spinExactly(final float rotation, final boolean constantDuration) {
        if (getWidth() == 0) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT <= 16) {
                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    spinExactly(rotation, constantDuration);
                }
            });
            return;
        }

        if (mAnimator != null) {
            mAnimator.cancel();
        }

        final float initialRotation = mRotation;
        Log.d(TAG, "Spinning " + rotation + " degrees");

        mAnimator = ValueAnimator.ofFloat(0, rotation);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.setDuration(constantDuration ? 300 : (long) (Math.abs(rotation) / 50 * 300));
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (Float) valueAnimator.getAnimatedValue();
                mRotation = initialRotation + value;
                invalidate();
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                // We're done rotating. Snap to whatever side we landed on.
                float offset = Math.abs(mRotation % 60);
                if (offset > 30) {
                    offset = 60f - offset;
                }
                Log.d(TAG, "We ended our spin " + offset + " degrees away from where we need to be");
                if (offset < 1f) {
                    Log.d(TAG, "We're close enough that we'll just jump to the position");
                    mRotation -= mRotation % 60;
                    invalidate();
                } else if ((mRotation + offset) % 60 == 0) {
                    spinExactly(offset, true);
                } else {
                    spinExactly(-offset, true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        mAnimator.start();
    }

    private float cosineInverse(@NonNull Point a, @NonNull Point b, @NonNull Point c) {
        double top = distanceSqr(a, b) + distanceSqr(a, c) - distanceSqr(b, c);
        double bottom = 2 * Math.sqrt(distanceSqr(a, b)) * Math.sqrt(distanceSqr(a, c));
        return (float) (Math.acos(top / bottom) * 180 / Math.PI);
    }

    private double distanceSqr(@NonNull Point a, @NonNull Point b) {
        return ((a.x - b.x) * (a.x - b.x)) + ((a.y - b.y) * (a.y - b.y));
    }

    private int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f;
        return Color.HSVToColor(hsv);
    }

    public Button[] getButtons() {
        return mButtons;
    }

    public void setText(String text) {
        mText = text;
    }

    public void setText(int resId) {
        setText(getContext().getString(resId));
    }

    public String getText() {
        return mText;
    }

    public void setTopMargin(float margin) {
        mTopMargin = margin;
    }

    public void setInitialSpin(float spin) {
        spin(spin);
    }

    public void setInitialRotation(float initialRotation) {
        Log.v(TAG, "Initial rotation set to " + initialRotation);
        mRotation = initialRotation;
    }

    private class Triangle {
        private final Point a, b, c;
        @NonNull
        private final Matrix m;
        @NonNull
        private final float[] points;

        private Triangle(Point a, Point b, Point c) {
            this.a = a;
            this.b = b;
            this.c = c;
            points = new float[2];
            m = new Matrix();
        }

        public boolean contains(@NonNull Point p) {
            if (mAllowRotation) {
                points[0] = p.x;
                points[1] = p.y;
                m.reset();
                m.postRotate(-mRotation, center.x, center.y);
                m.mapPoints(points);
                p.x = (int) points[0];
                p.y = (int) points[1];
            }

            Point AB = new Point(b.x - a.x, b.y - a.y);
            Point BC = new Point(c.x - b.x, c.y - b.y);
            Point CA = new Point(a.x - c.x, a.y - c.y);
            Point AP = new Point(p.x - a.x, p.y - a.y);
            Point BP = new Point(p.x - b.x, p.y - b.y);
            Point CP = new Point(p.x - c.x, p.y - c.y);

            int ABxAP = AB.x * AP.y - AP.x * AB.y;
            int BCxBP = BC.x * BP.y - BP.x * BC.y;
            int CAxCP = CA.x * CP.y - CP.x * CA.y;

            return (ABxAP >= 0 && BCxBP >= 0 && CAxCP >= 0) || (ABxAP <= 0 && BCxBP <= 0 && CAxCP <= 0);
        }
    }

    public static class Button {
        private final Context context;
        private HexagonLayout.Button.OnClickListener onClickListener;
        private Drawable drawable;
        private String text;
        private int color;
        private Triangle triangle;
        private boolean pressed;
        private boolean enabled = true;
        private boolean selected;

        public Button(Context context) {
            this.context = context;
        }

        public interface OnClickListener {
            void onClick();
        }

        public void setOnClickListener(HexagonLayout.Button.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        public OnClickListener getOnClickListener() {
            return onClickListener;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        public void setDrawableResource(int id) {
            setDrawable(context.getResources().getDrawable(id));
        }

        public Drawable getDrawable() {
            return drawable;
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

        private void setTriangle(Triangle triangle) {
            this.triangle = triangle;
        }

        private Triangle getTriangle() {
            return triangle;
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
