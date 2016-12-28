package com.sam.hex.view;

import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.hex.core.Game;
import com.hex.core.GameAction;
import com.hex.core.Player;
import com.hex.core.PlayingEntity;
import com.hex.core.Point;
import com.hex.core.Timer;
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
    @NonNull
    private Point mFocusedButton = new Point(-1, -1);

    private ShapeDrawable mPlayer1Background;
    private ShapeDrawable mPlayer2Background;
    private ShapeDrawable mBorderBackground;
    private int mBackgroundColor;

    private Game mGame;

    private float mMargin;

    private float mPieceMargin;
    private float mPieceWhiteBorder;
    private float mPieceLightBorder;
    private float mPieceShadowOffset;

    private float mTextMargin;
    private String mTitleText;
    private String mActionText;
    private String mTimerText;
    private Paint mTextPaint;
    private Paint mLargeTextPaint;

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
        mBackgroundColor = 0xFFD2D2D2;
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, dm));
        mLargeTextPaint = new Paint(mTextPaint);
        mLargeTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 32, dm));
        mLargeTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, dm);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int x = 0; x < mGame.gameOptions.gridSize; x++) {
                    for (int y = 0; y < mGame.gameOptions.gridSize; y++) {
                        Button b = mButtons[x][y];
                        if (b.isSelected() || b.isPressed()) {
                            GameAction.setPiece(new Point(x, y), mGame);
                        }
                    }
                }
            }
        });
        setFocusable(true);
    }

    @Override
    public View focusSearch(int direction) {
        mButtons[mFocusedButton.x][mFocusedButton.y].setSelected(false);
        switch (direction) {
            case View.FOCUS_RIGHT:
                if (mFocusedButton.x < mGame.gameOptions.gridSize - 1) {
                    mFocusedButton = new Point(mFocusedButton.x + 1, mFocusedButton.y);
                    mButtons[mFocusedButton.x][mFocusedButton.y].setSelected(true);
                    invalidate();
                    return this;
                }
                break;
            case View.FOCUS_LEFT:
                if (mFocusedButton.x > 0) {
                    mFocusedButton = new Point(mFocusedButton.x - 1, mFocusedButton.y);
                    mButtons[mFocusedButton.x][mFocusedButton.y].setSelected(true);
                    invalidate();
                    return this;
                }
                break;
            case View.FOCUS_UP:
                if (mFocusedButton.y > 0) {
                    mFocusedButton = new Point(mFocusedButton.x, mFocusedButton.y - 1);
                    mButtons[mFocusedButton.x][mFocusedButton.y].setSelected(true);
                    invalidate();
                    return this;
                }
                break;
            case View.FOCUS_DOWN:
                if (mFocusedButton.y < mGame.gameOptions.gridSize - 1) {
                    mFocusedButton = new Point(mFocusedButton.x, mFocusedButton.y + 1);
                    mButtons[mFocusedButton.x][mFocusedButton.y].setSelected(true);
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

    public void setGame(@NonNull Game game) {
        this.mGame = game;
        mButtons = new Button[game.gameOptions.gridSize][game.gameOptions.gridSize];
        for (int x = 0; x < game.gameOptions.gridSize; x++) {
            for (int y = 0; y < game.gameOptions.gridSize; y++) {
                mButtons[x][y] = new Button();
            }
        }
        onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
        this.setOnTouchListener(new TouchListener(game));
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFocusedButton = new Point(0, 0);
                    mButtons[mFocusedButton.x][mFocusedButton.y].setSelected(true);
                    invalidate();
                } else {
                    if (!mFocusedButton.equals(new Point(-1, -1))) {
                        mButtons[mFocusedButton.x][mFocusedButton.y].setSelected(false);
                        invalidate();
                    }
                }
            }
        });
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (mGame == null) return;
        int n = mGame.gameOptions.gridSize;

        if (!mGame.replayRunning) {
            PlayingEntity player;
            if (mGame.isGameOver()) {
                player = mGame.getWinner();
            } else {
                player = mGame.getCurrentPlayer();
            }
            if (player == null) return;

            ShapeDrawable background = (player.getTeam() == 1) ? mPlayer1Background : mPlayer2Background;
            background.getPaint().setColor(player.getColor());
            background.draw(canvas);
            mBorderBackground.draw(canvas);

            if (mTitleText != null && mActionText != null) {
                String titleText = String.format(mTitleText, player.getName());
                int textLength = (int) Math.max(mTextPaint.measureText(titleText), mLargeTextPaint.measureText(mActionText));
                float posX = player.getTeam() == 1 ? mTextMargin + textLength : getWidth() - mTextMargin;
                float posY = player.getTeam() == 1 ? getHeight() / 2 : mTextMargin + mTextPaint.getTextSize();
                canvas.drawText(titleText, posX - mTextPaint.measureText(titleText), posY, mTextPaint);
                canvas.drawText(mActionText, posX - mLargeTextPaint.measureText(mActionText), posY + mLargeTextPaint.getTextSize(), mLargeTextPaint);
            }
        } else {
            mPlayer1Background.getPaint().setColor(mGame.getPlayer1().getColor());
            mPlayer1Background.draw(canvas);
            mPlayer2Background.getPaint().setColor(mGame.getPlayer2().getColor());
            mPlayer2Background.draw(canvas);
            mBorderBackground.draw(canvas);

            if (mTitleText != null) {
                String p1TitleText = mGame.getPlayer1().getName();
                String p2TitleText = mGame.getPlayer2().getName();
                int p1TextLength = (int) mTextPaint.measureText(p1TitleText);
                float p1PosX = mTextMargin + p1TextLength;
                float p1PosY = getHeight() / 2;
                float p2PosX = getWidth() - mTextMargin;
                float p2PosY = mTextMargin + mTextPaint.getTextSize();
                canvas.drawText(p1TitleText, p1PosX - mTextPaint.measureText(p1TitleText), p1PosY, mTextPaint);
                canvas.drawText(p2TitleText, p2PosX - mTextPaint.measureText(p2TitleText), p2PosY, mTextPaint);
            }
        }

        if (mGame.gameOptions.timer.type != Timer.NO_TIMER && !mGame.replayRunning && !mGame.isGameOver()) {
            float posX = getWidth() - mTextMargin;
            float posY = getHeight() / 2;
            long minutesLeft = mGame.getCurrentPlayer().getTime() / (60 * 1000);
            long secondsLeft = (long) (Math.ceil(((double) mGame.getCurrentPlayer().getTime()) / 1000) - minutesLeft * 60);
            String time = String.format(Locale.getDefault(), "%d:%02d", minutesLeft, secondsLeft);
            canvas.drawText(mTimerText, posX - mTextPaint.measureText(mTimerText), posY, mTextPaint);
            canvas.drawText(time, posX - mLargeTextPaint.measureText(time), posY + mLargeTextPaint.getTextSize(), mLargeTextPaint);
        }

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                int c = Color.WHITE;
                if (mButtons[x][y].isPressed() && mGame.getCurrentPlayer().getType().equals(Player.Human)) {
                    c = Color.LTGRAY;
                }
                if (mGame.gamePieces[x][y].getTeam() == mGame.getPlayer1().getTeam())
                    c = mGame.getPlayer1().getColor();
                else if (mGame.gamePieces[x][y].getTeam() == mGame.getPlayer2().getTeam())
                    c = mGame.getPlayer2().getColor();
                if (mGame.gamePieces[x][y].isWinningPath()) c = getDarkerColor(c);
                mCellShadow[x][y].draw(canvas);

                if (mButtons[x][y].isSelected()) {
                    mCell[x][y].getPaint().setColor(Color.YELLOW);
                } else {
                    mCell[x][y].getPaint().setColor(Color.WHITE);
                }
                mCell[x][y].draw(canvas);

                if (c != Color.WHITE) {
                    mDrawableOutline[x][y].getPaint().setColor(getLighterColor(c));
                    mDrawableOutline[x][y].draw(canvas);
                }
                mDrawable[x][y].getPaint().setColor(c);
                mDrawable[x][y].draw(canvas);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mGame == null) return;
        int n = mGame.gameOptions.gridSize;
        mDrawable = new ShapeDrawable[n][n];
        mDrawableOutline = new ShapeDrawable[n][n];
        mCell = new ShapeDrawable[n][n];
        mCellShadow = new ShapeDrawable[n][n];
        int windowHeight = (int) (h - 2 * mMargin);
        int windowWidth = (w);

        double radius = BoardTools.radiusCalculator(windowWidth, windowHeight, mGame.gameOptions.gridSize);
        double hrad = radius * Math.sqrt(3) / 2;
        int yOffset = (int) ((windowHeight - ((3 * radius / 2) * (mGame.gamePieces[0].length - 1) + 2 * radius)) / 2);
        int xOffset = (int) ((windowWidth - (hrad * mGame.gamePieces.length * 2 + hrad * (mGame.gamePieces.length - 1))) / 2);

        // Shape of a hexagon
        Path path = new Path();
        path.moveTo(0, (float) -radius);
        path.lineTo((float) hrad, (float) -radius / 2);
        path.lineTo((float) hrad, (float) radius / 2);
        path.lineTo(0, (float) radius);
        path.lineTo((float) -hrad, (float) radius / 2);
        path.lineTo((float) -hrad, (float) -radius / 2);
        path.close();

        for (int xc = 0; xc < n; xc++) {
            for (int yc = 0; yc < n; yc++) {
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
                mButtons[xc][yc].hexagon = new Hexagon(x - hrad, y + mMargin, radius);
            }
        }

        // Shape of a rectangle
        Path p1Path = new Path();
        p1Path.moveTo(0, h / 3);
        p1Path.lineTo(w, h / 3);
        p1Path.lineTo(w, 2 * h / 3);
        p1Path.lineTo(0, 2 * h / 3);
        p1Path.close();
        mPlayer1Background = new ShapeDrawable(new PathShape(p1Path, w, h));
        mPlayer1Background.setBounds(0, 0, w, h);

        // Shape of a rectangle
        float centerTop = (xOffset + h / 3);
        float centerBottom = windowWidth - centerTop;
        Path p2Path = new Path();
        p2Path.moveTo(centerTop - h / 6, 0);
        p2Path.lineTo(centerTop + h / 6, 0);
        p2Path.lineTo(centerBottom + h / 6, h);
        p2Path.lineTo(centerBottom - h / 6, h);
        p2Path.close();
        mPlayer2Background = new ShapeDrawable(new PathShape(p2Path, w, h));
        mPlayer2Background.setBounds(0, 0, w, h);

        // Shape of a rectangle
        Path borderPath = new Path();
        borderPath.moveTo((float) (xOffset + hrad), (float) (mMargin + radius));
        borderPath.lineTo((float) (xOffset + hrad * 2 * n - hrad), (float) (mMargin + radius));
        borderPath.lineTo(w - (float) (xOffset + hrad), h - (float) (mMargin + radius));
        borderPath.lineTo(w - (float) (xOffset + hrad * 2 * n - hrad), h - (float) (mMargin + radius));
        borderPath.close();
        mBorderBackground = new ShapeDrawable(new PathShape(borderPath, w, h));
        mBorderBackground.setBounds(0, 0, w, h);
        mBorderBackground.getPaint().setColor(mBackgroundColor);
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

    public String getTitleText() {
        return mTitleText;
    }

    public void setTitleText(String titleText) {
        this.mTitleText = titleText;
    }

    public String getTimerText() {
        return mTimerText;
    }

    public void setTimerText(String timerText) {
        this.mTimerText = timerText;
    }

    public String getActionText() {
        return mActionText;
    }

    public void setActionText(String mActionText) {
        this.mActionText = mActionText;
    }

    private class TouchListener implements OnTouchListener {
        Game game;

        public TouchListener(Game game) {
            this.game = game;
        }

        @Override
        public boolean onTouch(View v, @NonNull MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (game.getCurrentPlayer().getType().equals(Player.Human)) {
                    for (int x = 0; x < game.gamePieces.length; x++) {
                        for (int y = 0; y < game.gamePieces[0].length; y++) {
                            if (mButtons[x][y].hexagon.contains(new Point((int) event.getX(), (int) event.getY()))) {
                                mButtons[x][y].setPressed(mButtons[x][y].isEnabled());
                            } else {
                                mButtons[x][y].setPressed(false);
                            }
                        }
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                for (int x = 0; x < game.gamePieces.length; x++) {
                    for (int y = 0; y < game.gamePieces[0].length; y++) {
                        if (mButtons[x][y].isPressed()) {
                            if (game.gamePieces[x][y].getTeam() == 0 || (game.gameOptions.swap && game.getMoveNumber() == 2)) {
                                performClick();
                            }
                        }
                        mButtons[x][y].setPressed(false);
                    }
                }
            } else {
                boolean selectedPiece = false;
                if (game.getCurrentPlayer().getType().equals(Player.Human)) {
                    for (int x = 0; x < game.gamePieces.length; x++) {
                        for (int y = 0; y < game.gamePieces[0].length; y++) {
                            if (!selectedPiece && mButtons[x][y].hexagon.contains(new Point((int) event.getX(), (int) event.getY()))) {
                                mButtons[x][y].setPressed(true);
                                selectedPiece = true;
                            } else {
                                mButtons[x][y].setPressed(false);
                            }
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
        @NonNull
        private final int[] polyY, polyX;
        // Number of sides in the polygon.
        private final int polySides = 6;

        public Hexagon(double x, double y, double r) {
            polyX = getXCoordinates(x, y, r, 6, Math.PI / 2);
            polyY = getYCoordinates(x, y, r, 6, Math.PI / 2);
        }

        public boolean contains(@NonNull Point p) {
            boolean oddTransitions = false;
            for (int i = 0, j = polySides - 1; i < polySides; j = i++) {
                if ((polyY[i] < p.y && polyY[j] >= p.y) || (polyY[j] < p.y && polyY[i] >= p.y)) {
                    if (polyX[i] + (p.y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < p.x) {
                        oddTransitions = !oddTransitions;
                    }
                }
            }
            return oddTransitions;
        }

        @NonNull
        protected int[] getXCoordinates(double x, double y, double r, int vertexCount, double startAngle) {
            int res[] = new int[vertexCount];
            double addAngle = 2 * Math.PI / vertexCount;
            double angle = startAngle;
            for (int i = 0; i < vertexCount; i++) {
                res[i] = (int) (Math.round(r * Math.cos(angle)) + x);
                angle += addAngle;
            }
            return res;
        }

        @NonNull
        protected int[] getYCoordinates(double x, double y, double r, int vertexCount, double startAngle) {
            int res[] = new int[vertexCount];
            double addAngle = 2 * Math.PI / vertexCount;
            double angle = startAngle;
            for (int i = 0; i < vertexCount; i++) {
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
        private boolean selected;

        public interface OnClickListener {
            void onClick();
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
