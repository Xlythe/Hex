package com.sam.hex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Will Harmon
 **/
public class BoardView extends View {
    private ShapeDrawable[][] mDrawable;
    private ShapeDrawable[][] mDrawableOutline;
    private ShapeDrawable[][] mCell;
    private ShapeDrawable[][] mCellShadow;
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
                mCellShadow[x][y].draw(canvas);
                mCell[x][y].draw(canvas);
                mDrawable[x][y].getPaint().setColor(game.gamePiece[x][y].getColor());
                mDrawable[x][y].draw(canvas);
                mDrawableOutline[x][y].getPaint().setColor(game.gamePiece[x][y].getColor());
                mDrawableOutline[x][y].draw(canvas);
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
        int windowHeight = getHeight();
        int windowWidth = getWidth();
        // Size of border
        int margin = 10;
        int border = 2 + margin;
        int drawableBorder = 5 + border;
        int shadowOffset = 3;

        double radius = BoardTools.radiusCalculator(windowWidth, windowHeight, game.gameOptions.gridSize);
        double hrad = radius * Math.sqrt(3) / 2;
        int yOffset = (int) ((windowHeight - ((3 * radius / 2) * (game.gamePiece[0].length - 1) + 2 * radius)) / 2);
        int xOffset = (int) ((windowWidth - (hrad * game.gamePiece.length * 2 + hrad * (game.gamePiece.length - 1))) / 2);

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
                mDrawable[xc][yc].setBounds((int) (x - hrad), (int) (y), (int) (x + hrad) - drawableBorder, (int) (y + radius * 2) - drawableBorder);
                mDrawableOutline[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mDrawableOutline[xc][yc].setBounds((int) (x - hrad), (int) (y), (int) (x + hrad) - border, (int) (y + radius * 2) - border);
                mDrawableOutline[xc][yc].getPaint().setAlpha(80);
                mCell[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mCell[xc][yc].setBounds((int) (x - hrad), (int) (y), (int) (x + hrad) - margin, (int) (y + radius * 2) - margin);
                mCell[xc][yc].getPaint().setColor(Color.WHITE);
                mCellShadow[xc][yc] = new ShapeDrawable(new PathShape(path, (int) hrad * 2, (int) radius * 2));
                mCellShadow[xc][yc].setBounds((int) (x - hrad), (int) (y) + shadowOffset, (int) (x + hrad) - margin, (int) (y + radius * 2) - margin);
                mCellShadow[xc][yc].getPaint().setColor(Color.BLACK);
                mCellShadow[xc][yc].getPaint().setAlpha(15);
                game.gamePiece[xc][yc].set(x - hrad, y, radius);
            }
        }
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
                for(int xc = 0; xc < game.gamePiece.length; xc++) {
                    for(int yc = 0; yc < game.gamePiece[0].length; yc++) {
                        if(game.gamePiece[xc][yc].contains(x, y)) {
                            if(game != null && !game.replayRunning) GameAction.setPiece(new Point(xc, yc), game);
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }
}