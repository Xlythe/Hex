package com.sam.hex;

import com.sam.hex.net.NetGlobal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
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
	
	public BoardView(Context context){
		super(context);
		if(Global.viewLocation==Global.GAME_LOCATION){
			game = Global.game;
		}
		else if(Global.viewLocation==NetGlobal.GAME_LOCATION){
			game = NetGlobal.game;
		}
		calculateGrid();
	}
	public BoardView(Context context, AttributeSet attrs){
        super(context, attrs);
        if(Global.viewLocation==Global.GAME_LOCATION){
			game = Global.game;
		}
		else if(Global.viewLocation==NetGlobal.GAME_LOCATION){
			game = NetGlobal.game;
		}
        calculateGrid();
    }
    public BoardView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        if(Global.viewLocation==Global.GAME_LOCATION){
			game = Global.game;
		}
		else if(Global.viewLocation==NetGlobal.GAME_LOCATION){
			game = NetGlobal.game;
		}
        calculateGrid();
    }
	
	protected void onDraw(Canvas canvas){
		int n = game.gridSize;
		
		colorBackground();
		backgroundTopBottom.draw(canvas);
		backgroundLeft.draw(canvas);
		backgroundRight.draw(canvas);
		for(int xc=0;xc<n;xc++)
			for(int yc=0;yc<n;yc++){
				mOutline[xc][yc].draw(canvas);
				mDrawable[xc][yc].getPaint().setColor(game.gamePiece[xc][yc].getColor());
				mDrawable[xc][yc].draw(canvas);
			}
		game.player1Icon.setColorFilter(game.player1.getColor());
		game.player2Icon.setColorFilter(game.player2.getColor());
		if(game.currentPlayer==1 && !game.gameOver){
			game.player1Icon.setAlpha(255);
			game.player2Icon.setAlpha(80);
		}
		else if(game.currentPlayer==2 && !game.gameOver){
			game.player1Icon.setAlpha(80);
			game.player2Icon.setAlpha(255);
		}
		else{
			game.player1Icon.setAlpha(80);
			game.player2Icon.setAlpha(80);
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
        path.moveTo((float)0, (float)-radius);
        path.lineTo((float)hrad, (float)-radius/2);
        path.lineTo((float)hrad, (float)radius/2);
        path.lineTo(0,(float)radius);
        path.lineTo((float)-hrad,(float)radius/2);
        path.lineTo((float)-hrad,(float)-radius/2);
        path.close();
        
		//Draw background
        if(Global.windowHeight>Global.windowWidth){
        	Path left = new Path();
	        left.moveTo(0, 0);
	        left.lineTo((float) (game.gridSize*hrad*2), 0);
	        left.lineTo(0, Global.windowHeight);
	        left.close();
	        Path right = new Path();
	        right.moveTo(Global.windowWidth, Global.windowHeight);
	        right.lineTo((float) (Global.windowWidth-(game.gridSize*hrad*2)), Global.windowHeight);
	        right.lineTo(Global.windowWidth, 0);
	        right.close();
	        
	        backgroundTopBottom = new ShapeDrawable(new RectShape());
	        backgroundTopBottom.setBounds(0,0,Global.windowWidth,Global.windowHeight);
	        backgroundLeft = new ShapeDrawable(new PathShape(left, Global.windowWidth, Global.windowHeight));
	        backgroundLeft.setBounds(0,(int) (yOffset+hrad),Global.windowWidth,Global.windowHeight);
	        backgroundRight = new ShapeDrawable(new PathShape(right, Global.windowWidth, Global.windowHeight));
	        backgroundRight.setBounds(0,0,Global.windowWidth,(int) (Global.windowHeight-(yOffset+hrad)));
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
	
	public void calculateGrid(){
		if(game.gamePiece[0][0]==null)
		for(int xc=0;xc<game.gridSize;xc++)
			for(int yc=0;yc<game.gridSize;yc++)
				game.gamePiece[xc][yc]=new RegularPolygonGameObject();
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
}