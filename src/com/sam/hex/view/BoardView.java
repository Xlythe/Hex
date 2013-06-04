package com.sam.hex.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hex.core.Game;
import com.hex.core.GameAction;
import com.hex.core.Point;
import com.sam.hex.BoardTools;
import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class BoardView extends View {
    private ShapeDrawable[][] mDrawable;
    private ShapeDrawable[][] mDrawableOutline;
    private ShapeDrawable[][] mCell;
    private ShapeDrawable[][] mCellShadow;
    private Hexagon[][] mHexagon;
    public Game game;

    public BoardView(Context context) {
        super(context);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
        mHexagon = new Hexagon[n][n];
        int windowHeight = getHeight();
        int windowWidth = getWidth();

        // Size of border
        float margin = getContext().getResources().getDimension(R.dimen.hex_margin);
        float border = margin + getContext().getResources().getDimension(R.dimen.hex_border);
        float drawableBorder = border + getContext().getResources().getDimension(R.dimen.hex_drawable_border);
        float shadowOffset = getContext().getResources().getDimension(R.dimen.hex_shadow_offset);

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
                mDrawable[xc][yc].setBounds((int) (x - hrad), (int) (y), (int) (x + hrad - drawableBorder), (int) (y + radius * 2 - drawableBorder));
                mDrawableOutline[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mDrawableOutline[xc][yc].setBounds((int) (x - hrad), (int) (y), (int) (x + hrad - border), (int) (y + radius * 2 - border));
                mDrawableOutline[xc][yc].getPaint().setAlpha(200);
                mCell[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mCell[xc][yc].setBounds((int) (x - hrad), (int) (y), (int) (x + hrad - margin), (int) (y + radius * 2 - margin));
                mCell[xc][yc].getPaint().setColor(Color.WHITE);
                mCellShadow[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mCellShadow[xc][yc].setBounds((int) (x - hrad), (int) (y + shadowOffset), (int) (x + hrad - margin), (int) (y + radius * 2 - margin));
                mCellShadow[xc][yc].getPaint().setColor(Color.BLACK);
                mCellShadow[xc][yc].getPaint().setAlpha(15);
                mHexagon[xc][yc] = new Hexagon(x - hrad, y, radius);
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
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    private class TouchListener implements OnTouchListener {
        Game game;

        public TouchListener(Game game) {
            this.game = game;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eventaction = event.getAction();
            if(eventaction == MotionEvent.ACTION_UP) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                for(int xc = 0; xc < game.gamePieces.length; xc++) {
                    for(int yc = 0; yc < game.gamePieces[0].length; yc++) {
                        if(mHexagon[xc][yc].contains(x, y)) {
                            if(game != null && !game.replayRunning) GameAction.setPiece(new Point(xc, yc), game);
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    public class Hexagon {
        // Polygon coodinates.
        private final int[] polyY, polyX;
        // Number of sides in the polygon.
        private final int polySides = 6;

        public Hexagon(double x, double y, double r) {
            polyX = getXCoordinates(x, y, r, 6, Math.PI / 2);
            polyY = getYCoordinates(x, y, r, 6, Math.PI / 2);
        }

        public boolean contains(int x, int y) {
            boolean oddTransitions = false;
            for(int i = 0, j = polySides - 1; i < polySides; j = i++) {
                if((polyY[i] < y && polyY[j] >= y) || (polyY[j] < y && polyY[i] >= y)) {
                    if(polyX[i] + (y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < x) {
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
}
