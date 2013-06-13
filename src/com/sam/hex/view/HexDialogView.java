package com.sam.hex.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.hex.core.Point;

/**
 * @author Will Harmon
 **/
public class HexDialogView extends View implements OnTouchListener {
    private boolean mAllowRotation;

    private float mRotation;
    private Point[] corners;
    private Point center;

    private ShapeDrawable mHexagon;
    private int mBackgroundColor;

    private HexDialogView.Button[] mButtons;
    private ShapeDrawable[] mBorder;
    private float mBorderWidth;
    private float mBorderShadowWidth;
    private ShapeDrawable[] mBorderShadow;
    private ShapeDrawable[] mPressedState;
    private Paint mLinePaint;
    private Paint mButtonTextPaint;
    private int mPressedColor;
    private int mDisabledColor;

    private HexDialog mDialog;

    public HexDialogView(Context context) {
        super(context);
        setUp();
    }

    public HexDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public HexDialogView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp();
    }

    protected HexDialogView(Context context, HexDialog dialog) {
        super(context);
        mDialog = dialog;
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
        mPressedColor = Color.LTGRAY;
        mDisabledColor = getDarkerColor(mPressedColor);
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.LTGRAY);
        mButtonTextPaint = new Paint();
        mButtonTextPaint.setColor(Color.WHITE);
        mButtonTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, dm));
        mAllowRotation = false;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mAllowRotation) {
            if(spinSign * spinVelocity > 0) {
                spinVelocity /= 2;
                spinVelocity -= spinSign * 5;
                mRotation += spinSign * 5f;
                postInvalidateDelayed(30);
            }
            canvas.rotate(mRotation, center.x, center.y);
        }

        mHexagon.draw(canvas);

        canvas.drawLine(corners[0].x, corners[0].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[1].x, corners[1].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[2].x, corners[2].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[3].x, corners[3].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[4].x, corners[4].y, center.x, center.y, mLinePaint);
        canvas.drawLine(corners[5].x, corners[5].y, center.x, center.y, mLinePaint);

        // mTextBackground.draw(canvas);

        for(int i = 0; i < 6; i++) {
            if(mButtons[i].isPressed()) {
                mPressedState[i].getPaint().setColor(mPressedColor);
                mPressedState[i].draw(canvas);
            }
            if(!mButtons[i].isEnabled()) {
                mPressedState[i].getPaint().setColor(mDisabledColor);
                mPressedState[i].draw(canvas);
            }
        }

        // canvas.drawText(mText, mTextX, mTextY, mTextPaint);

        canvas.save();
        for(int i = 0; i < 6; i++) {
            canvas.rotate(60, center.x, center.y);
            mBorderShadow[i].draw(canvas);
            mBorder[i].draw(canvas);
            // mButtons[i].getDrawable().draw(canvas);
            // canvas.drawText(mButtons[i].getText(), center.x -
            // mButtonTextPaint.measureText(mButtons[i].getText()) / 2,
            // (mBorderWidth + mButtonTextPaint.getTextSize()) / 2,
            // mButtonTextPaint);
        }
        canvas.restore();
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
        if(widthMode == MeasureSpec.EXACTLY) {
            // Must be this size
            width = widthSize;
        }
        else if(widthMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        }
        else {
            // Be whatever you want
            width = desiredWidth;
        }

        // Measure Height
        if(heightMode == MeasureSpec.EXACTLY) {
            // Must be this size
            height = heightSize;
        }
        else if(heightMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        }
        else {
            // Be whatever you want
            height = desiredHeight;
        }

        // MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(mAllowRotation) {
            h = (int) (h * 1.4);
        }
        w = (int) (h * 1.1547);
        mBorderShadowWidth = h / 2 * 0.1828f;
        mBorderWidth = mBorderShadowWidth * 0.882f;
        mBorderShadowWidth -= mBorderWidth;

        // Create a box

        center = new Point(w / 2, h / 2);

        // Get the length of a side of the hexagon
        int s = w / 2;

        // Create an array of the corners
        corners = new Point[6];
        corners[0] = new Point(w / 4, 0);
        corners[1] = new Point(3 * w / 4, 0);
        corners[2] = new Point(w, h / 2);
        corners[3] = new Point(3 * w / 4, h);
        corners[4] = new Point(w / 4, h);
        corners[5] = new Point(0, h / 2);

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
        edgePath.lineTo((int) (corners[1].x - mBorderWidth / 1.732), mBorderWidth);
        edgePath.lineTo((int) (corners[0].x + mBorderWidth / 1.732), mBorderWidth);
        edgePath.close();
        // Shape of an edge
        Path shadowEdgePath = new Path();
        shadowEdgePath.moveTo(corners[0].x, corners[0].y);
        shadowEdgePath.lineTo(corners[1].x, corners[1].y);
        shadowEdgePath.lineTo((int) (corners[1].x - (mBorderWidth + mBorderShadowWidth) / 1.732), (mBorderWidth + mBorderShadowWidth));
        shadowEdgePath.lineTo((int) (corners[0].x + (mBorderWidth + mBorderShadowWidth) / 1.732), (mBorderWidth + mBorderShadowWidth));
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
            mPressedState[i].setBounds(0, 0, w, h);

            mButtons[i].setTriangle(t);

            mBorder[i] = new ShapeDrawable(new PathShape(edgePath, w, h));
            mBorder[i].getPaint().setColor(mButtons[i].getColor());
            mBorder[i].setBounds(0, 0, w, h);

            mBorderShadow[i] = new ShapeDrawable(new PathShape(shadowEdgePath, w, h));
            mBorderShadow[i].getPaint().setColor(getDarkerColor(mButtons[i].getColor()));
            mBorderShadow[i].setBounds(0, 0, w, h);
        }
    }

    private float rotationOffset;
    private float oldRotation;
    private float spinVelocity;
    private int spinSign;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int sign = (event.getX() > center.x) ? -1 : 1;
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            rotationOffset = sign * cosineInverse(center, new Point(center.x, 0), new Point((int) event.getX(), (int) event.getY()));
            oldRotation = mRotation;
            for(Button b : mButtons) {
                if(b.getTriangle().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    b.setPressed(b.isEnabled());
                }
                else {
                    b.setPressed(false);
                }
            }
        }
        else if(event.getAction() == MotionEvent.ACTION_UP) {
            boolean dismiss = true;
            for(Button b : mButtons) {
                if(b.getTriangle().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    dismiss = false;
                }
            }
            if(dismiss) {
                mDialog.dismiss();
            }
        }
        else {
            mRotation = oldRotation + rotationOffset - sign * cosineInverse(center, new Point(center.x, 0), new Point((int) event.getX(), (int) event.getY()));
            for(Button b : mButtons) {
                if(b.isPressed()) {
                    if(!b.getTriangle().contains(new Point((int) event.getX(), (int) event.getY()))) {
                        b.setPressed(false);
                    }
                    else if(Math.abs(Math.abs(mRotation) - Math.abs(oldRotation) % 360) > 10f) {
                        b.setPressed(false);
                    }
                }
            }
        }

        invalidate();
        return true;
    }

    private float cosineInverse(Point a, Point b, Point c) {
        double top = distanceSqr(a, b) + distanceSqr(a, c) - distanceSqr(b, c);
        double bottom = 2 * Math.sqrt(distanceSqr(a, b)) * Math.sqrt(distanceSqr(a, c));
        return (float) (Math.acos(top / bottom) * 180 / Math.PI);
    }

    private double distanceSqr(Point a, Point b) {
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

    private class Triangle {
        private final Point a, b, c;
        private final Matrix m;
        private final float[] points;

        private Triangle(Point a, Point b, Point c) {
            this.a = a;
            this.b = b;
            this.c = c;
            points = new float[2];
            m = new Matrix();
        }

        public boolean contains(Point p) {
            if(mAllowRotation) {
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
        private HexDialogView.Button.OnClickListener onClickListener;
        private Drawable drawable;
        private String text;
        private int color;
        private Triangle triangle;
        private boolean pressed;
        private boolean enabled = true;

        public Button(Context context) {
            this.context = context;
        }

        public static interface OnClickListener {
            public void onClick();
        }

        public void setOnClickListener(HexDialogView.Button.OnClickListener onClickListener) {
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
