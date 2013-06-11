package com.sam.hex.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.hex.core.Game;
import com.hex.core.GameAction;
import com.hex.core.Point;
import com.sam.hex.BoardTools;

/**
 * @author Will Harmon
 **/
public class BoardView extends View {
    private ShapeDrawable[][] mDrawable;
    private ShapeDrawable[][] mDrawableOutline;
    private ShapeDrawable[][] mCell;
    private ShapeDrawable[][] mCellShadow;
    private Button[][] mButtons;

    private ShapeDrawable mPlayer1Background;
    private ShapeDrawable mPlayer2Background;

    public Game game;

    private float mMargin;

    private float mPieceMargin;
    private float mPieceWhiteBorder;
    private float mPieceLightBorder;
    private float mPieceShadowOffset;

    private String mWinText;
    private String mTurnText;
    private String mTimerText;
    private boolean mShowWinText;
    private boolean mShowTurnText;
    private boolean mShowTimerText;

    public BoardView(Context context) {
        super(context);
        setUp();
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp();
    }

    private void setUp() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, dm);
        mPieceMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, dm);
        mPieceWhiteBorder = mPieceMargin + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm);
        mPieceLightBorder = mPieceWhiteBorder + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);
        mPieceShadowOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, dm);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
    }

    public void setGame(Game game) {
        this.game = game;
        this.setOnTouchListener(new TouchListener(game));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(game == null) return;
        int n = game.gameOptions.gridSize;

        for(int x = 0; x < n; x++)
            for(int y = 0; y < n; y++) {
                int c = Color.TRANSPARENT;
                if(mButtons[x][y].isPressed()) {
                    c = Color.LTGRAY;
                }
                if(game.gamePieces[x][y].getTeam() == game.getPlayer1().getTeam()) c = game.getPlayer1().getColor();
                else if(game.gamePieces[x][y].getTeam() == game.getPlayer2().getTeam()) c = game.getPlayer2().getColor();
                if(game.gamePieces[x][y].isWinningPath()) c = getDarkerColor(c);
                mCellShadow[x][y].draw(canvas);
                mCell[x][y].draw(canvas);
                if(c != Color.TRANSPARENT) {
                    mDrawableOutline[x][y].getPaint().setColor(getLighterColor(c));
                    mDrawableOutline[x][y].draw(canvas);
                }
                mDrawable[x][y].getPaint().setColor(c);
                mDrawable[x][y].draw(canvas);
            }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(game == null) return;
        int n = game.gameOptions.gridSize;
        mDrawable = new ShapeDrawable[n][n];
        mDrawableOutline = new ShapeDrawable[n][n];
        mCell = new ShapeDrawable[n][n];
        mCellShadow = new ShapeDrawable[n][n];
        mButtons = new Button[n][n];
        int windowHeight = (int) (h - 2 * mMargin);
        int windowWidth = (int) (w);

        double radius = BoardTools.radiusCalculator(windowWidth, windowHeight, game.gameOptions.gridSize);
        double hrad = radius * Math.sqrt(3) / 2;
        int yOffset = (int) ((windowHeight - ((3 * radius / 2) * (game.gamePieces[0].length - 1) + 2 * radius)) / 2);
        int xOffset = (int) ((windowWidth - (hrad * game.gamePieces.length * 2 + hrad * (game.gamePieces.length - 1))) / 2);

        // Shape of a hexagon
        Path path = new Path();
        path.moveTo(0, (float) -radius);
        path.lineTo((float) hrad, (float) -radius / 2);
        path.lineTo((float) hrad, (float) radius / 2);
        path.lineTo(0, (float) radius);
        path.lineTo((float) -hrad, (float) radius / 2);
        path.lineTo((float) -hrad, (float) -radius / 2);
        path.close();

        for(int xc = 0; xc < n; xc++) {
            for(int yc = 0; yc < n; yc++) {
                double x = ((hrad + yc * hrad + 2 * hrad * xc) + hrad + xOffset);
                double y = (1.5 * radius * yc + radius) + yOffset;
                mDrawable[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mDrawable[xc][yc].setBounds((int) (x - hrad), (int) (y + mMargin), (int) (x + hrad - mPieceLightBorder),
                        (int) (y + mMargin + radius * 2 - (mPieceLightBorder * 1.1547)));
                mDrawableOutline[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mDrawableOutline[xc][yc].setBounds((int) (x - hrad), (int) (y + mMargin), (int) (x + hrad - mPieceWhiteBorder),
                        (int) (y + mMargin + radius * 2 - (mPieceWhiteBorder * 1.1547)));
                mDrawableOutline[xc][yc].getPaint().setAlpha(200);
                mCell[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mCell[xc][yc].setBounds((int) (x - hrad), (int) (y + mMargin), (int) (x + hrad - mPieceMargin),
                        (int) (y + mMargin + radius * 2 - (mPieceMargin * 1.1547)));
                mCell[xc][yc].getPaint().setColor(Color.WHITE);
                mCellShadow[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mCellShadow[xc][yc].setBounds((int) (x - hrad), (int) (y + mMargin + mPieceShadowOffset), (int) (x + hrad - mPieceMargin), (int) (y + mMargin
                        + radius * 2 - (mPieceMargin * 1.1547)));
                mCellShadow[xc][yc].getPaint().setColor(Color.BLACK);
                mCellShadow[xc][yc].getPaint().setAlpha(15);
                mButtons[xc][yc] = new Button();
                mButtons[xc][yc].hexagon = new Hexagon(x - hrad, y + mMargin, radius);
            }
        }
    }

    private int getLighterColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.1f;
        return Color.HSVToColor(hsv);
    }

    private int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.6f;
        return Color.HSVToColor(hsv);
    }

    public String getWinText() {
        return mWinText;
    }

    public void setWinText(String winText) {
        this.mWinText = winText;
    }

    public String getTurnText() {
        return mTurnText;
    }

    public void setTurnText(String turnText) {
        this.mTurnText = turnText;
    }

    public String getTimerText() {
        return mTimerText;
    }

    public void setTimerText(String timerText) {
        this.mTimerText = timerText;
    }

    public boolean isShowWinText() {
        return mShowWinText;
    }

    public void setShowWinText(boolean showWinText) {
        this.mShowWinText = showWinText;
    }

    public boolean isShowTurnText() {
        return mShowTurnText;
    }

    public void setShowTurnText(boolean showTurnText) {
        this.mShowTurnText = showTurnText;
    }

    public boolean isShowTimerText() {
        return mShowTimerText;
    }

    public void setShowTimerText(boolean showTimerText) {
        this.mShowTimerText = showTimerText;
    }

    private class TouchListener implements OnTouchListener {
        Game game;

        public TouchListener(Game game) {
            this.game = game;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                for(int x = 0; x < game.gamePieces.length; x++) {
                    for(int y = 0; y < game.gamePieces[0].length; y++) {
                        if(mButtons[x][y].hexagon.contains(new Point((int) event.getX(), (int) event.getY()))) {
                            mButtons[x][y].setPressed(mButtons[x][y].isEnabled());
                        }
                        else {
                            mButtons[x][y].setPressed(false);
                        }
                    }
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_UP) {
                for(int x = 0; x < game.gamePieces.length; x++) {
                    for(int y = 0; y < game.gamePieces[0].length; y++) {
                        if(mButtons[x][y].isPressed()) {
                            if(game.gamePieces[x][y].getTeam() == 0 || (game.gameOptions.swap && game.getMoveNumber() == 2)) {
                                performClick();
                                GameAction.setPiece(new Point(x, y), game);
                            }
                        }
                        mButtons[x][y].setPressed(false);
                    }
                }
            }
            else {
                boolean selectedPiece = false;
                for(int x = 0; x < game.gamePieces.length; x++) {
                    for(int y = 0; y < game.gamePieces[0].length; y++) {
                        if(!selectedPiece && mButtons[x][y].hexagon.contains(new Point((int) event.getX(), (int) event.getY()))) {
                            mButtons[x][y].setPressed(true);
                            selectedPiece = true;
                        }
                        else {
                            mButtons[x][y].setPressed(false);
                        }
                    }
                }
            }

            invalidate();

            return true;
        }
    }

    private class Hexagon {
        // Polygon coodinates.
        private final int[] polyY, polyX;
        // Number of sides in the polygon.
        private final int polySides = 6;

        public Hexagon(double x, double y, double r) {
            polyX = getXCoordinates(x, y, r, 6, Math.PI / 2);
            polyY = getYCoordinates(x, y, r, 6, Math.PI / 2);
        }

        public boolean contains(Point p) {
            boolean oddTransitions = false;
            for(int i = 0, j = polySides - 1; i < polySides; j = i++) {
                if((polyY[i] < p.y && polyY[j] >= p.y) || (polyY[j] < p.y && polyY[i] >= p.y)) {
                    if(polyX[i] + (p.y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < p.x) {
                        oddTransitions = !oddTransitions;
                    }
                }
            }
            return oddTransitions;
        }

        protected int[] getXCoordinates(double x, double y, double r, int vertexCount, double startAngle) {
            int res[] = new int[vertexCount];
            double addAngle = 2 * Math.PI / vertexCount;
            double angle = startAngle;
            for(int i = 0; i < vertexCount; i++) {
                res[i] = (int) (Math.round(r * Math.cos(angle)) + x);
                angle += addAngle;
            }
            return res;
        }

        protected int[] getYCoordinates(double x, double y, double r, int vertexCount, double startAngle) {
            int res[] = new int[vertexCount];
            double addAngle = 2 * Math.PI / vertexCount;
            double angle = startAngle;
            for(int i = 0; i < vertexCount; i++) {
                res[i] = (int) (Math.round(r * Math.sin(angle)) + y);
                angle += addAngle;
            }
            return res;
        }
    }

    public static class Button {
        private Hexagon hexagon;
        private boolean pressed;
        private boolean enabled = true;

        public static interface OnClickListener {
            public void onClick();
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
