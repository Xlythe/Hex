package com.sam.hex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.DisplayMetrics;
import android.view.View;

public class BoardView extends View{
	private ShapeDrawable[][] mDrawable;
	
	public BoardView(Context context){
		super(context);
		calculateGrid(context);
	}
	
	protected void onDraw(Canvas canvas){
		int n = Global.getN();
		
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(BoardTools.teamGrid()[i][j]==1){
					mDrawable[i][j].getPaint().setColor(0xffff0000);//Red
				}
				else if(BoardTools.teamGrid()[i][j]==2){
					mDrawable[i][j].getPaint().setColor(0xff00ffff);//Blue
				}
				else if(BoardTools.teamGrid()[i][j]==3){
					mDrawable[i][j].getPaint().setColor(0xffffff00);//Yellow
				}
				else if(BoardTools.teamGrid()[i][j]==4){
					mDrawable[i][j].getPaint().setColor(0xffffff00);//Yellow
				}
				else{
					mDrawable[i][j].getPaint().setColor(0xff74AC23);//Green
				}
				mDrawable[i][j].draw(canvas);
			}
		}
	}
	
	public void calculateGrid(Context context){
		int n = Global.getN();
		mDrawable = new ShapeDrawable[n][n];
		
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		switch(metrics.densityDpi) {
			case DisplayMetrics.DENSITY_HIGH:
				screenHeight -= 48;
				break;
			case DisplayMetrics.DENSITY_MEDIUM:
				screenHeight -= 32;
				break;
			case DisplayMetrics.DENSITY_LOW:
				screenHeight -= 24;
				break;
		}
		
		int spacing_width = 2;
		int spacing_height = 2;
		int L = (int) Math.min(((screenWidth / (n + (n-1)/2)) - spacing_width)/Math.sqrt(3), (screenHeight-(n+1)*spacing_height)/((n+1)+(n+1)/2+1/2));
		int width = (int) (Math.sqrt(3)*L);
		int height = (int) (Math.sqrt(3)*L);//2*L;
		Global.setHexLength(L);
		
		int x=width;
		int y=L;
		int spacing=0;
		
		//Shape of a hexagon
		Path path = new Path();
        path.moveTo(0, 0+L/2);
        path.lineTo(0, L+L/2);
        path.lineTo((float) (-L*Math.sqrt(3)/2),L+L*1/2+L/2);
        path.lineTo((float) (-L*Math.sqrt(3)),L+L/2);
        path.lineTo((float) (-L*Math.sqrt(3)),0+L/2);
        path.lineTo((float) (-L*Math.sqrt(3)/2),-L*1/2+L/2);
        path.close();
        
		
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				mDrawable[i][j] = new ShapeDrawable(new PathShape(path, width, height));
				mDrawable[i][j].setBounds(x,y,x+width,y+height);
				
				BoardTools.setPolyXY(i, j, new Posn(x-2*L,y));
				
				x+=width+spacing_width;
			}
			
			spacing+=width/2;
			x=width+spacing+spacing_width/2;
			y+=L+L/2+spacing_height;
		}
	}
}