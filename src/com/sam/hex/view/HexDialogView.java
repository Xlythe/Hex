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
import android.view.ViewGroup;

import com.hex.core.Point;

/**
 * @author Will Harmon
 **/
public class HexDialogView extends View implements OnTouchListener {
    private HexDialogView.Button[] mButtons;
    private Paint mButtonTextPaint;
    private int mPressedColor;
    private int mDisabledColor;
    private int mBackgroundColor;
    private int mBorderColor;

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
        mButtons = new Button[3];
        for(int i = 0; i < 3; i++) {
            mButtons[i] = new Button(getContext());
            mButtons[i].setSideLength(100f);
        }
        mBackgroundColor = Color.WHITE;
        mPressedColor = Color.LTGRAY;
        mDisabledColor = Color.LTGRAY;
        mButtonTextPaint = new Paint();
        mButtonTextPaint.setColor(Color.WHITE);
        mButtonTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, dm));
        mBorderColor = Color.LTGRAY;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for(int i = 0; i < 3; i++) {
            canvas.save();
            Button b = mButtons[i];
            canvas.rotate(b.getRoation(), b.getCenter().x, b.getCenter().y);
            if(!b.isEnabled()) {
                b.getBackgroundDrawable().getPaint().setColor(mDisabledColor);
            }
            else if(b.isPressed()) {
                b.getBackgroundDrawable().getPaint().setColor(mPressedColor);
            }
            else {
                b.getBackgroundDrawable().getPaint().setColor(mBackgroundColor);
            }
            b.getBackgroundBorderDrawable().draw(canvas);
            b.getBackgroundDrawable().draw(canvas);
            canvas.restore();

            if(b.getView() != null) {
                canvas.save();
                canvas.translate(b.getCenter().x - b.getSideLength() / 2, b.getCenter().y - b.getSideLength() / 2);
                b.getView().draw(canvas);
                canvas.restore();
            }
        }

        mButtons[0].incrementRotation(-3f);
        mButtons[1].incrementRotation(2f);
        mButtons[2].incrementRotation(-3f);
        postInvalidateDelayed(50);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        for(int i = 0; i < 3; i++) {
            Button b = mButtons[i];

            b.setCenter(new Point((int) (b.getCenterXPercent() * w), (int) (b.getCenterYPercent() * h)));
            b.setSideLength(b.getSideLengthPercent() * w);

            Point[] corners = getHexagon(b.getCenter(), b.getSideLength());
            b.setHexagon(new Hexagon(b, corners[0], corners[1], corners[2], corners[3], corners[4], corners[5]));

            ShapeDrawable hexagon = getHexagonDrawable(corners, w, h);
            hexagon.getPaint().setColor(mBackgroundColor);
            b.setBackgroundDrawable(hexagon);

            ShapeDrawable hexagonBorder = getHexagonDrawable(getHexagon(b.getCenter(), (int) (b.getSideLength() * 1.2)), w, h);
            hexagonBorder.getPaint().setColor(mBorderColor);
            b.setBackgroundBorderDrawable(hexagonBorder);

            int width = b.getSideLength();
            if(b.getView() != null) {
                b.getView().measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
                layoutAllViews(b.getView(), 0, 0, width, width);
            }
        }
    }

    private void layoutAllViews(View v, int l, int t, int r, int b) {
        v.layout(l, t, r, b);
        if(ViewGroup.class.isAssignableFrom(v.getClass())) {
            ViewGroup fl = (ViewGroup) v;
            for(int i = 0; i < fl.getChildCount(); i++) {
                View child = fl.getChildAt(i);
                layoutAllViews(child, child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            }
        }
    }

    private Point[] getHexagon(Point center, int sideLength) {
        Point[] corners = new Point[6];
        corners[0] = new Point(center.x - sideLength / 2, (int) (center.y + sideLength * 0.866));
        corners[1] = new Point(center.x + sideLength / 2, (int) (center.y + sideLength * 0.866));
        corners[2] = new Point(center.x + sideLength, center.y);
        corners[3] = new Point(center.x + sideLength / 2, (int) (center.y - sideLength * 0.866));
        corners[4] = new Point(center.x - sideLength / 2, (int) (center.y - sideLength * 0.866));
        corners[5] = new Point(center.x - sideLength, center.y);
        return corners;
    }

    private ShapeDrawable getHexagonDrawable(Point[] corners, int w, int h) {
        // Shape of a hexagon
        Path hexagonPath = new Path();
        hexagonPath.moveTo(corners[0].x, corners[0].y);
        hexagonPath.lineTo(corners[1].x, corners[1].y);
        hexagonPath.lineTo(corners[2].x, corners[2].y);
        hexagonPath.lineTo(corners[3].x, corners[3].y);
        hexagonPath.lineTo(corners[4].x, corners[4].y);
        hexagonPath.lineTo(corners[5].x, corners[5].y);
        hexagonPath.close();

        ShapeDrawable sd = new ShapeDrawable(new PathShape(hexagonPath, w, h));
        sd.setBounds(0, 0, w, h);
        return sd;
    }

    private boolean wasPressed;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            wasPressed = false;
            for(Button b : mButtons) {
                if(b.getHexagon().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    b.setPressed(b.isEnabled() && b.getOnClickListener() != null);
                    wasPressed = true;
                }
                else {
                    b.setPressed(false);
                }
            }
        }
        else if(event.getAction() == MotionEvent.ACTION_UP) {
            boolean dismiss = !wasPressed;
            for(Button b : mButtons) {
                if(b.isPressed()) {
                    performClick();
                    b.performClick();
                    b.setPressed(false);
                }
            }
            if(dismiss) {
                mDialog.dismiss();
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

    public Button[] getButtons() {
        return mButtons;
    }

    private class Hexagon {
        private final Button button;
        private final Point a, b, c, d, e, f;
        private final Matrix m;
        private final float[] points;

        private Hexagon(Button button, Point a, Point b, Point c, Point d, Point e, Point f) {
            this.button = button;
            this.a = a;
            this.b = b;
            this.c = c;
            this.e = d;
            this.d = e;
            this.f = f;
            points = new float[2];
            m = new Matrix();
        }

        public boolean contains(Point p) {
            points[0] = p.x;
            points[1] = p.y;
            m.reset();
            m.postRotate(-button.getRoation(), a.x + (b.x - a.x) / 2, a.y + (e.y - a.y) / 2);
            m.mapPoints(points);
            p.x = (int) points[0];
            p.y = (int) points[1];

            Point AB = new Point(b.x - a.x, b.y - a.y);
            Point BC = new Point(c.x - b.x, c.y - b.y);
            Point CD = new Point(d.x - c.x, d.y - c.y);
            Point DE = new Point(e.x - d.x, e.y - d.y);
            Point EF = new Point(f.x - e.x, f.y - e.y);
            Point FA = new Point(a.x - f.x, a.y - f.y);

            Point AP = new Point(p.x - a.x, p.y - a.y);
            Point BP = new Point(p.x - b.x, p.y - b.y);
            Point CP = new Point(p.x - c.x, p.y - c.y);
            Point DP = new Point(p.x - d.x, p.y - d.y);
            Point EP = new Point(p.x - e.x, p.y - e.y);
            Point FP = new Point(p.x - f.x, p.y - f.y);

            int ABxAP = AB.x * AP.y - AP.x * AB.y;
            int BCxBP = BC.x * BP.y - BP.x * BC.y;
            int CDxCP = CD.x * CP.y - CP.x * CD.y;
            int DExDP = DE.x * DP.y - DP.x * DE.y;
            int EFxEP = EF.x * EP.y - EP.x * EF.y;
            int FAxFP = FA.x * FP.y - FP.x * FA.y;

            // temp fix
            DExDP *= -1;

            return (ABxAP >= 0 && BCxBP >= 0 && CDxCP >= 0 && DExDP >= 0 && EFxEP >= 0 && FAxFP >= 0)
                    || (ABxAP <= 0 && BCxBP <= 0 && CDxCP <= 0 && DExDP <= 0 && EFxEP <= 0 && FAxFP <= 0);
        }
    }

    public static class Button {
        private final Context context;
        private HexDialogView.Button.OnClickListener onClickListener;
        private Hexagon hexagon;
        private boolean pressed;
        private boolean enabled = true;
        private Point center;
        private float rotation;
        private ShapeDrawable backgroundDrawable;
        private ShapeDrawable backgroundBorderDrawable;
        private View view;
        private float sideLength;
        private float sideLengthPercent;
        private float centerXPercent;
        private float centerYPercent;

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

        private void setHexagon(Hexagon hexagon) {
            this.hexagon = hexagon;
        }

        private Hexagon getHexagon() {
            return hexagon;
        }

        public void performClick() {
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

        private Point getCenter() {
            return center;
        }

        private void setCenter(Point center) {
            this.center = center;
        }

        private float getRoation() {
            return rotation;
        }

        private void setRotation(float rotation) {
            this.rotation = rotation;
        }

        private void incrementRotation(float rotation) {
            setRotation(this.rotation + rotation);
        }

        private ShapeDrawable getBackgroundDrawable() {
            return backgroundDrawable;
        }

        private void setBackgroundDrawable(ShapeDrawable drawable) {
            this.backgroundDrawable = drawable;
        }

        private ShapeDrawable getBackgroundBorderDrawable() {
            return backgroundBorderDrawable;
        }

        private void setBackgroundBorderDrawable(ShapeDrawable drawable) {
            this.backgroundBorderDrawable = drawable;
        }

        private int getSideLength() {
            return (int) sideLength;
        }

        private void setSideLength(float sideLength) {
            this.sideLength = sideLength;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;

            if(getCenter() == null) return;
            int width = getSideLength();
            this.view.layout(getCenter().x - width / 2, getCenter().y - width / 2, getCenter().x + width / 2, getCenter().y + width / 2);
        }

        public void setView(int resId) {
            setView(View.inflate(context, resId, null));
        }

        public float getSideLengthPercent() {
            return sideLengthPercent;
        }

        public void setSideLengthPercent(float sideLengthPercent) {
            this.sideLengthPercent = sideLengthPercent;
        }

        public float getCenterXPercent() {
            return centerXPercent;
        }

        public void setCenterXPercent(float centerXPercent) {
            this.centerXPercent = centerXPercent;
        }

        public float getCenterYPercent() {
            return centerYPercent;
        }

        public void setCenterYPercent(float centerYPercent) {
            this.centerYPercent = centerYPercent;
        }
    }
}
