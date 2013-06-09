package com.sam.hex.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.hex.core.Point;

/**
 * @author Will Harmon
 **/
public class SelectorLayout extends View implements OnTouchListener {
    private SelectorLayout.Button[] mButtons;
    private ShapeDrawable[] mButtonDrawable;
    private ShapeDrawable[] mMirrorButtonDrawable;
    private Point[] mButtonLabelPos;
    private Paint mButtonTextPaint;
    private int mDisabledColor;

    private int mWidth;
    private int mIndentHeight;
    private int mMargin;
    private float mRotation;

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
        for(int i = 0; i < 3; i++) {
            mButtons[i] = new Button(getContext());
        }
        mDisabledColor = getDarkerColor(Color.LTGRAY);
        mButtonTextPaint = new Paint();
        mButtonTextPaint.setColor(Color.WHITE);
        mButtonTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, dm));

        mWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, dm);
        mIndentHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, dm);
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dm);
        mRotation = 45f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(mRotation);
        for(int i = 0; i < 3; i++) {
            if(!mButtons[i].isEnabled()) {
                mButtonDrawable[i].getPaint().setColor(mDisabledColor);
                mMirrorButtonDrawable[i].getPaint().setColor(mDisabledColor);
            }
            else if(mButtons[i].isPressed()) {
                mButtonDrawable[i].getPaint().setColor(getDarkerColor(mButtons[i].getColor()));
                mMirrorButtonDrawable[i].getPaint().setColor(getDarkerColor(mButtons[i].getColor()));
            }
            else {
                mButtonDrawable[i].getPaint().setColor(mButtons[i].getColor());
                mMirrorButtonDrawable[i].getPaint().setColor(mButtons[i].getColor());
            }
            mButtonDrawable[i].draw(canvas);
            mMirrorButtonDrawable[i].draw(canvas);
            canvas.save();
            canvas.rotate(-90f, mButtonLabelPos[i].x, mButtonLabelPos[i].y);
            canvas.drawText(mButtons[i].getText(), mButtonLabelPos[i].x, mButtonLabelPos[i].y, mButtonTextPaint);
            canvas.restore();
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        int margin = (w - mWidth * 3) / 4;
        int offset = margin;
        // Create the buttons
        mButtonDrawable = new ShapeDrawable[3];
        mMirrorButtonDrawable = new ShapeDrawable[3];
        mButtonLabelPos = new Point[3];

        Hexagon hex = new Hexagon(new Point(0, mIndentHeight), new Point(mWidth / 2, 0), new Point(mWidth, mIndentHeight), new Point(mWidth, h), new Point(
                mWidth / 2, h - mIndentHeight), new Point(0, h));

        // Shape of a pressed state
        Path buttonPath = new Path();
        buttonPath.moveTo(hex.a.x, hex.a.y);
        buttonPath.lineTo(hex.b.x, hex.b.y);
        buttonPath.lineTo(hex.c.x, hex.c.y);
        buttonPath.lineTo(hex.d.x, hex.d.y);
        buttonPath.lineTo(hex.e.x, hex.e.y);
        buttonPath.lineTo(hex.f.x, hex.f.y);
        buttonPath.close();

        for(int i = 0; i < 3; i++) {
            int heightOffset = offset + mIndentHeight * 3;
            mButtonDrawable[i] = new ShapeDrawable(new PathShape(buttonPath, w, h));
            mButtonDrawable[i].setBounds(offset, -heightOffset, w + offset, h - heightOffset);
            mMirrorButtonDrawable[i] = new ShapeDrawable(new PathShape(buttonPath, w, h));
            mMirrorButtonDrawable[i].setBounds(offset, (h - mIndentHeight) + mMargin - heightOffset, w + offset, (2 * h - mIndentHeight) + mMargin
                    - heightOffset);
            mButtonLabelPos[i] = new Point((int) (offset + mWidth / 2 + mButtonTextPaint.getTextSize() / 2),
                    (int) (h / 2 + mButtonTextPaint.measureText(mButtons[i].getText()) / 2));
            mButtons[i].setHexagon(hex);
            offset += margin + mWidth;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            for(Button b : mButtons) {
                if(b.getHexagon().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    b.setPressed(b.isEnabled());
                }
                else {
                    b.setPressed(false);
                }
            }
        }
        else if(event.getAction() == MotionEvent.ACTION_UP) {
            for(Button b : mButtons) {
                if(b.isPressed()) {
                    b.preformClick();
                }
                b.setPressed(false);
            }
        }
        else {
            for(Button b : mButtons) {
                if(b.isPressed()) {
                    if(!b.getHexagon().contains(new Point((int) event.getX(), (int) event.getY()))) {
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

    private class Hexagon {
        private final Point a, b, c, d, e, f;

        private Hexagon(Point a, Point b, Point c, Point d, Point e, Point f) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
        }

        public boolean contains(Point p) {
            float[] points = new float[2];
            points[0] = p.x;
            points[1] = p.y;
            Matrix m = new Matrix();
            m.postRotate(-mRotation);
            m.mapPoints(points);
            Point rotatedP = new Point((int) points[0], (int) points[1]);

            return rotatedP.x > a.x && rotatedP.x < c.x;
        }
    }

    public static class Button {
        private final Context context;
        private SelectorLayout.Button.OnClickListener onClickListener;
        private String text;
        private int color;
        private Hexagon hexagon;
        private boolean pressed;
        private boolean enabled = true;

        public Button(Context context) {
            this.context = context;
        }

        public static interface OnClickListener {
            public void onClick();
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

        public void preformClick() {
            if(onClickListener != null) onClickListener.onClick();
        }

        protected boolean isPressed() {
            return pressed;
        }

        protected void setPressed(boolean pressed) {
            this.pressed = pressed;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
