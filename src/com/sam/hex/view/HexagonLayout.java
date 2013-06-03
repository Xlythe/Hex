package com.sam.hex.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
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
public class HexagonLayout extends View implements OnTouchListener {
    private int mRotation;
    private double mShortRadius;
    private Point[] corners;
    private Point center;

    private ShapeDrawable mHexagon;
    private int mBackgroundColor;

    private String mTitle;
    private float mTextX;
    private float mTextY;
    private float mTextSize;
    private float mTextPadding;
    private Paint mTextPaint;
    private ShapeDrawable mTextBackground;

    private HexagonLayout.Button[] mButtons;
    private ShapeDrawable[] mBorder;
    private float mBorderWidth;
    private float mBorderShadowWidth;
    private ShapeDrawable[] mBorderShadow;
    private ShapeDrawable[] mPressedState;
    private Paint mLinePaint;
    private Paint mButtonTextPaint;
    private int mPressedColor;

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
        for(int i = 0; i < 6; i++) {
            mButtons[i] = new Button(getContext());
        }
        mBorderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dm);
        mBorderShadowWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);
        mPressedColor = Color.LTGRAY;
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 44, dm);
        mTitle = "Hex";
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm);
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.LTGRAY);
        mButtonTextPaint = new Paint();
        mButtonTextPaint.setColor(Color.WHITE);
        mButtonTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, dm));
    }

    private void layoutText() {
        final int screenWidth = center.x * 2;
        final int screenHeight = center.y * 2;

        Paint paint = mTextPaint;
        if(mTextSize != 0f) paint.setTextSize(mTextSize);
        float textWidth = paint.measureText(mTitle);
        float width = screenWidth;
        float textSize = mTextSize;
        if(textWidth > width) {
            paint.setTextSize(textSize * width / textWidth);
            mTextX = getPaddingLeft();
            mTextSize = textSize;
        }
        else {
            mTextX = (screenHeight - textWidth) / 2;
        }
        mTextY = (screenWidth - paint.ascent() - paint.descent()) / 2;

        mTextBackground = new ShapeDrawable(new OvalShape());
        mTextBackground.getPaint().setColor(mBackgroundColor);
        mTextBackground.setBounds((int) (center.x - textWidth / 2 - mTextPadding), (int) (center.y - textSize / 2 - mTextPadding), (int) (center.x + textWidth
                / 2 + mTextPadding), (int) (center.y + textSize / 2 + mTextPadding));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(mRotation, center.x, center.y);
        mHexagon.draw(canvas);

        canvas.drawLine(corners[0].x, corners[0].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[1].x, corners[1].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[2].x, corners[2].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[3].x, corners[3].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[4].x, corners[4].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[5].x, corners[5].y, center.x, center.y, mLinePaint);

        mTextBackground.draw(canvas);

        for(int i = 0; i < 6; i++) {
            if(mButtons[i].isPressed()) {
                mPressedState[i].draw(canvas);
            }
        }

        canvas.drawText(mTitle, mTextX, mTextY, mTextPaint);

        canvas.save();
        for(int i = 0; i < 6; i++) {
            canvas.rotate(60, center.x, center.y);
            mBorderShadow[i].draw(canvas);
            mBorder[i].draw(canvas);
            mButtons[i].getDrawable().draw(canvas);
            canvas.drawText(mButtons[i].getText(), center.x - mButtonTextPaint.measureText(mButtons[i].getText()) / 2, center.y - (int) mShortRadius
                    + mButtonTextPaint.getTextSize(), mButtonTextPaint);
        }
        canvas.restore();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Create a box
        int boxLength = Math.min(w, h);
        center = new Point(boxLength / 2, boxLength / 2);

        // Get the length of a side of the hexagon
        int s = boxLength / 2;
        mShortRadius = ((boxLength / 4) * Math.sqrt(3));
        double radiusDiffence = boxLength / 2 - mShortRadius;

        // Create an array of the corners
        corners = new Point[6];
        corners[0] = new Point(boxLength / 4, (int) radiusDiffence);
        corners[1] = new Point(3 * boxLength / 4, (int) radiusDiffence);
        corners[2] = new Point(boxLength, (int) (boxLength / 2));
        corners[3] = new Point(3 * boxLength / 4, (int) (boxLength - radiusDiffence));
        corners[4] = new Point(boxLength / 4, (int) (boxLength - radiusDiffence));
        corners[5] = new Point(0, boxLength / 2);

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
        edgePath.lineTo((int) (corners[1].x - mBorderWidth / 1.732), center.y - (int) mShortRadius + mBorderWidth);
        edgePath.lineTo((int) (corners[0].x + mBorderWidth / 1.732), center.y - (int) mShortRadius + mBorderWidth);
        edgePath.close();
        // Shape of an edge
        Path shadowEdgePath = new Path();
        shadowEdgePath.moveTo(corners[0].x, corners[0].y);
        shadowEdgePath.lineTo(corners[1].x, corners[1].y);
        shadowEdgePath.lineTo((int) (corners[1].x - (mBorderWidth + mBorderShadowWidth) / 1.732),
                (center.y - (int) mShortRadius + mBorderWidth + mBorderShadowWidth));
        shadowEdgePath.lineTo((int) (corners[0].x + (mBorderWidth + mBorderShadowWidth) / 1.732),
                (center.y - (int) mShortRadius + mBorderWidth + mBorderShadowWidth));
        shadowEdgePath.close();
        for(int i = 0; i < 6; i++) {
            Triangle t = new Triangle(new Point(corners[(i + 1) % 6].x, corners[(i + 1) % 6].y), new Point(corners[(i + 2) % 6].x, corners[(i + 2) % 6].y),
                    new Point(center.x, center.y));
            // Shape of a pressed state
            Path pressedStatePath = new Path();
            pressedStatePath.moveTo(t.a.x, t.a.y);
            pressedStatePath.lineTo(t.b.x, t.b.y);
            pressedStatePath.lineTo(t.c.x, t.c.y);
            pressedStatePath.close();

            mPressedState[i] = new ShapeDrawable(new PathShape(pressedStatePath, w, h));
            mPressedState[i].getPaint().setColor(mPressedColor);
            mPressedState[i].setBounds(0, 0, w, h);

            mButtons[i].setTriangle(t);

            mButtons[i].getDrawable().setBounds(center.x - s / 6, (int) (s * 0.866 / 2), center.x + s / 6, (int) (s * 0.866 / 2 + s / 3));

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
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            for(Button b : mButtons) {
                if(b.getTriangle().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    b.preformClick();
                }
                b.setPressed(false);
            }
        }
        else {
            for(Button b : mButtons) {
                if(b.getTriangle().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    b.setPressed(true);
                }
                else {
                    b.setPressed(false);
                }
            }
        }

        invalidate();
        return true;
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

    private class Triangle {
        private Point a, b, c;

        private Triangle(Point a, Point b, Point c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public boolean contains(Point p) {
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
        private Context context;
        private HexagonLayout.Button.OnClickListener onClickListener;
        private Drawable drawable;
        private String text;
        private int color;
        private Triangle triangle;
        private boolean pressed;

        public Button(Context context) {
            this.context = context;
        }

        public static interface OnClickListener {
            public void onClick();
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

        public void preformClick() {
            if(onClickListener != null) onClickListener.onClick();
        }

        public boolean isPressed() {
            return pressed;
        }

        public void setPressed(boolean pressed) {
            this.pressed = pressed;
        }
    }
}
