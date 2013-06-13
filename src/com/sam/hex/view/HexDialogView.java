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
        mBackgroundColor = Color.WHITE;
        mPressedColor = Color.LTGRAY;
        mDisabledColor = Color.LTGRAY;// getDarkerColor(mPressedColor);
        mButtonTextPaint = new Paint();
        mButtonTextPaint.setColor(Color.WHITE);
        mButtonTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, dm));
        mButtons = new Button[3];
        for(int i = 0; i < 3; i++) {
            mButtons[i] = new Button(getContext());
        }
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
            b.getBackgroundDrawable().draw(canvas);
            canvas.restore();

            if(b.getDrawable() != null) {
                b.getDrawable().draw(canvas);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        for(int i = 0; i < 3; i++) {
            Button b = mButtons[i];

            b.setSideLength(100f);
            b.setCenter(new Point((i + 1) * w / 4, (2 - i % 2) * h / 4));

            Point[] corners = new Point[6];
            corners[0] = new Point(b.getCenter().x - b.getSideLength() / 2, (int) (b.getCenter().y + b.getSideLength() * 0.866));
            corners[1] = new Point(b.getCenter().x + b.getSideLength() / 2, (int) (b.getCenter().y + b.getSideLength() * 0.866));
            corners[2] = new Point(b.getCenter().x + b.getSideLength(), b.getCenter().y);
            corners[3] = new Point(b.getCenter().x + b.getSideLength() / 2, (int) (b.getCenter().y - b.getSideLength() * 0.866));
            corners[4] = new Point(b.getCenter().x - b.getSideLength() / 2, (int) (b.getCenter().y - b.getSideLength() * 0.866));
            corners[5] = new Point(b.getCenter().x - b.getSideLength(), b.getCenter().y);

            b.hexagon = new Hexagon(b, corners[0], corners[1], corners[2], corners[3], corners[4], corners[5]);

            // Shape of a hexagon
            Path hexagonPath = new Path();
            hexagonPath.moveTo(corners[0].x, corners[0].y);
            hexagonPath.lineTo(corners[1].x, corners[1].y);
            hexagonPath.lineTo(corners[2].x, corners[2].y);
            hexagonPath.lineTo(corners[3].x, corners[3].y);
            hexagonPath.lineTo(corners[4].x, corners[4].y);
            hexagonPath.lineTo(corners[5].x, corners[5].y);
            hexagonPath.close();

            ShapeDrawable hexagon = new ShapeDrawable(new PathShape(hexagonPath, w, h));
            hexagon.getPaint().setColor(mBackgroundColor);
            hexagon.setBounds(0, 0, w, h);
            b.setBackgroundDrawable(hexagon);

            int drawableWidth = b.getSideLength();
            if(b.getDrawable() != null) b.getDrawable().setBounds(b.getCenter().x - drawableWidth / 2, b.getCenter().y - drawableWidth / 2,
                    b.getCenter().x + drawableWidth / 2, b.getCenter().y + drawableWidth / 2);
        }
    }

    private boolean wasPressed;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            wasPressed = false;
            for(Button b : mButtons) {
                if(b.getHexagon().contains(new Point((int) event.getX(), (int) event.getY()))) {
                    b.setPressed(b.isEnabled());
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
        private String text;
        private Hexagon hexagon;
        private boolean pressed;
        private boolean enabled = true;
        private Point center;
        private float rotation;
        private ShapeDrawable backgroundDrawable;
        private Drawable drawable;
        private float sideLength;

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

        public void setText(String text) {
            this.text = text;
        }

        public void setText(int resId) {
            setText(context.getString(resId));
        }

        public String getText() {
            return text;
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

        private ShapeDrawable getBackgroundDrawable() {
            return backgroundDrawable;
        }

        private void setBackgroundDrawable(ShapeDrawable drawable) {
            this.backgroundDrawable = drawable;
        }

        private int getSideLength() {
            return (int) sideLength;
        }

        private void setSideLength(float sideLength) {
            this.sideLength = sideLength;
        }

        public Drawable getDrawable() {
            return drawable;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
            int drawableWidth = getSideLength();
            if(getCenter() != null) getDrawable().setBounds(getCenter().x - drawableWidth / 2, getCenter().y - drawableWidth / 2,
                    getCenter().x + drawableWidth / 2, getCenter().y + drawableWidth / 2);
        }
    }
}
