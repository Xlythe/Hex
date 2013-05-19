package com.sam.hex;

import com.sam.hex.net.NetGlobal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Color;

/**
 * @author Will Harmon
 **/
public class BoardView extends View{
    private ShapeDrawable[][] mDrawable;
    private ShapeDrawable[][] mOutline;
    private ShapeDrawable backgroundTopBottom;
    private ShapeDrawable backgroundLeft;
    private ShapeDrawable backgroundRight;
    public GameObject game;
    public boolean replayRunning = false;
    
    public BoardView(Context context){
        super(context);
        getGame();
    }
    public BoardView(Context context, AttributeSet attrs){
        super(context, attrs);
        getGame();
    }
    public BoardView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        getGame();
    }
    
    private void getGame(){
        switch(Global.viewLocation){
        case(Global.GAME_LOCATION):
            game = Global.game;
            break;
        case(NetGlobal.GAME_LOCATION):
            game = NetGlobal.game;
            break;
        }
        this.setOnTouchListener(new TouchListener(game));
    }
    
    @Override
    protected void onDraw(Canvas canvas){
        int n = game.gridSize;
        
        colorBackground();
        backgroundTopBottom.draw(canvas);
        backgroundLeft.draw(canvas);
        backgroundRight.draw(canvas);
        for(int x=0;x<n;x++)
            for(int y=0;y<n;y++){
                mOutline[x][y].draw(canvas);
                mDrawable[x][y].getPaint().setColor(game.gamePiece[x][y].getColor());
                mDrawable[x][y].draw(canvas);
            }
        game.views.player1Icon.setColorFilter(game.player1.getColor());
        game.views.player2Icon.setColorFilter(game.player2.getColor());
        if(game.currentPlayer==1 && !game.gameOver){
            game.views.player1Icon.setAlpha(255);
            game.views.player2Icon.setAlpha(80);
        }
        else if(game.currentPlayer==2 && !game.gameOver){
            game.views.player1Icon.setAlpha(80);
            game.views.player2Icon.setAlpha(255);
        }
        else{
            game.views.player1Icon.setAlpha(80);
            game.views.player2Icon.setAlpha(80);
        }
    }
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        int n = game.gridSize;
        mDrawable = new ShapeDrawable[n][n];
        mOutline=new ShapeDrawable[n][n];
        Global.windowHeight=getHeight();
        Global.windowWidth=getWidth();
        //Size of border
        int border=1;
        
        double radius=BoardTools.radiusCalculator(Global.windowWidth, Global.windowHeight, game.gridSize); 
        double hrad = radius * Math.sqrt(3) / 2;
        int yOffset = (int) ((Global.windowHeight - ((3 * radius / 2)
                * (game.gamePiece[0].length - 1) + 2 * radius)) / 2);
        int xOffset = (int) ((Global.windowWidth - (hrad * game.gamePiece.length*2+hrad*(game.gamePiece.length-1))) / 2);
        
        
        //Shape of a hexagon
          Path path = new Path();
        path.moveTo(0, (float)-radius);
        path.lineTo((float)hrad, (float)-radius/2);
        path.lineTo((float)hrad, (float)radius/2);
        path.lineTo(0,(float)radius);
        path.lineTo((float)-hrad,(float)radius/2);
        path.lineTo((float)-hrad,(float)-radius/2);
        path.close();
        
        //Draw background
        if(Global.windowHeight>Global.windowWidth){
            int smallHeight = (int) (yOffset+2*hrad/3);
            int smallLength = (int) ((game.gridSize-1)*hrad);
            int largeHeight = (int) (Global.windowHeight-smallHeight-hrad);
            int largeLength = largeHeight*smallLength/smallHeight;
            
            Path left = new Path();
            left.moveTo(0, Global.windowHeight);
            left.lineTo(0, Global.windowHeight-largeHeight);
            left.lineTo(largeLength, Global.windowHeight-largeHeight);
            left.close();
            
            Path right = new Path();
            right.moveTo(Global.windowWidth, 0);
            right.lineTo(Global.windowWidth, largeHeight);
            right.lineTo(Global.windowWidth-largeLength, largeHeight);
            right.close();
            
            backgroundTopBottom = new ShapeDrawable(new RectShape());
            backgroundTopBottom.setBounds(0,0,Global.windowWidth,Global.windowHeight);
            backgroundLeft = new ShapeDrawable(new PathShape(left, Global.windowWidth, Global.windowHeight));
            backgroundLeft.setBounds(0,0,Global.windowWidth,Global.windowHeight);
            backgroundRight = new ShapeDrawable(new PathShape(right, Global.windowWidth, Global.windowHeight));
            backgroundRight.setBounds(0,0,Global.windowWidth,Global.windowHeight);
        }
        else{
            Path left = new Path();
            left.moveTo(xOffset-(float)hrad, 0);
            left.lineTo(Global.windowWidth-xOffset-((n-1)*(float)hrad), 0);
            left.lineTo(Global.windowWidth/2, Global.windowHeight/2);
            left.close();
            Path right = new Path();
            right.moveTo(Global.windowWidth-xOffset+(float)hrad, Global.windowHeight);
            right.lineTo(xOffset+((n-1)*(float)hrad), Global.windowHeight);
            right.lineTo(Global.windowWidth/2, Global.windowHeight/2);
            right.close();
            
            backgroundTopBottom = new ShapeDrawable(new RectShape());
            backgroundTopBottom.setBounds(0,0,Global.windowWidth,Global.windowHeight);
            backgroundLeft = new ShapeDrawable(new PathShape(left, Global.windowWidth, Global.windowHeight));
            backgroundLeft.setBounds(0,0,Global.windowWidth,Global.windowHeight);
            backgroundRight = new ShapeDrawable(new PathShape(right, Global.windowWidth, Global.windowHeight));
            backgroundRight.setBounds(0,0,Global.windowWidth,Global.windowHeight);
        }
              
        for(int xc=0;xc<n;xc++)
            for(int yc=0;yc<n;yc++){
                double x=((hrad + yc * hrad + 2 * hrad * xc) + hrad+xOffset);
                double y=(1.5* radius * yc + radius)+ yOffset;
                mDrawable[xc][yc] = new ShapeDrawable(new PathShape(path, (int)hrad*2, (int)radius*2));
                mDrawable[xc][yc].setBounds((int)(x-hrad),(int)(y),(int)(x+hrad)-border,(int)(y+radius*2)-border);
                mOutline[xc][yc] = new ShapeDrawable(new PathShape(path, (int)hrad*2, (int)radius*2));
                mOutline[xc][yc].setBounds((int)(x-hrad),(int)(y),(int)(x+hrad),(int)(y+radius*2));
                mOutline[xc][yc].getPaint().setColor(Color.BLACK);
                game.gamePiece[xc][yc].set(x-hrad, y, radius);
                
            }
    }
    
    private void colorBackground(){
        if(Global.windowHeight>Global.windowWidth){
            backgroundTopBottom.getPaint().setColor(game.player2.getColor());
            backgroundLeft.getPaint().setColor(game.player1.getColor());
            backgroundRight.getPaint().setColor(game.player1.getColor());
        }
        else{
            backgroundTopBottom.getPaint().setColor(game.player1.getColor());
            backgroundLeft.getPaint().setColor(game.player2.getColor());
            backgroundRight.getPaint().setColor(game.player2.getColor());
        }
    }
    
    private class TouchListener implements OnTouchListener{
        GameObject game;
        public TouchListener(GameObject game){
            this.game = game;
        }
        @Override
        public boolean onTouch(View v, MotionEvent event){
            int eventaction = event.getAction();
            if(eventaction==MotionEvent.ACTION_UP){
                int x = (int)event.getX();
                int y = (int)event.getY();
                for(int xc = 0; xc < game.gamePiece.length; xc++){
                    for(int yc=0; yc<game.gamePiece[0].length; yc++)
                        if(game.gamePiece[xc][yc].contains(x, y)){
                            if(game!=null && !replayRunning)GameAction.setPiece(new Point(xc,yc),game);
                            return false;
                        }
                }
            }
            
            return true;
        }
    }
}