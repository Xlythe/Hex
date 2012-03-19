package com.sam.hex;

import android.graphics.Bitmap;
import android.graphics.Point;

public class BoardTools {
	static double spaceH; // Horizontal
	static double spaceV; // Vertical

	public static double radiusCalculator(double w, double h, double n) {

		spaceV = (((n - 1) * 3 / 2) + 2);

		spaceH = n + (n - 1) / 2; // always bigger.
		spaceH = (w / (spaceH * Math.sqrt(3)));
		spaceV = (h / spaceV);
		if (spaceV < spaceH) {
			return spaceV;
		}
		return spaceH;

	}
	public static void undo(){
		if(!Global.moveList.isEmpty()){
			Point lastMove = Global.moveList.get(Global.moveList.size()-1);
			
			if(Global.currentPlayer==(byte)1) Global.player2.undo(lastMove);
			else Global.player1.undo(lastMove);
			
			Global.moveList.remove(Global.moveList.size()-1);
			//Reset the game if it's already ended
			if(!Global.gameRunning){
				GameAction.checkedFlagReset();
				for(int x=0;x<Global.gridSize;x++){
    				for(int y=0;y<Global.gridSize;y++){
    					if(Global.gamePiece[x][y].getTeam()==(byte)1){
    						Global.gamePiece[x][y].setColor(Global.player1Color);
    					}
    					else if(Global.gamePiece[x][y].getTeam()==(byte)2){
    						Global.gamePiece[x][y].setColor(Global.player2Color);
    					}
    				}
    			}
				Global.gameRunning=true;
				Global.game = new GameObject();
				Global.currentPlayer=(byte) ((Global.currentPlayer%2)+1);
				if(Global.currentPlayer==(byte)1) Global.player2.undo(lastMove);
				else Global.player1.undo(lastMove);
			}
			
			Global.currentPlayer=(byte) ((Global.currentPlayer%2)+1);
		}
		
		Global.board.postInvalidate();
	}
	public static Bitmap getBackground(int w, int h) {
		Bitmap background =Bitmap.createBitmap(Global.windowWidth, Global.windowHeight,Bitmap.Config.ARGB_8888); //the background is drawn to this bitmap. 
		RegularPolygonGameObject[][] gamePeace = Global.gamePiece;
		double radius = BoardTools.radiusCalculator(Global.windowWidth, Global.windowHeight,Global.gridSize);
		double hrad = radius * Math.sqrt(3) / 2; // Horizontal radius
		int yOffset = (int) ((Global.windowHeight - ((3 * radius / 2)
				* (gamePeace[0].length - 1) + 2 * radius)) / 2);
		int xOffset = (int) ((Global.windowWidth - (hrad
				* gamePeace.length * 2 + hrad * (gamePeace[0].length - 1))) / 2);
		int aX = xOffset;
		int aY = yOffset + (int) radius / 2;
		int bX = xOffset
				+ (int) ((gamePeace.length - 1) * hrad + gamePeace.length
						* hrad * 2);
		int bY = yOffset
				+ (int) (((gamePeace.length - 1) * radius * 3 / 2) + radius * 3 / 2);
		double slope1 = (double) (bY - aY) / (bX - aX);

		int cX = xOffset + (int) ((gamePeace.length - 1) * hrad + hrad / 2);
		int cY = yOffset
				+ (int) (((gamePeace.length - 1) * radius * 3 / 2) + radius * 7 / 4);
		int dX = xOffset + (int) (gamePeace.length * hrad * 2 - hrad / 2);
		int dY = yOffset + (int) radius / 4;
		double slope2 = (double) (dY - cY) / (dX - cX);

		for (double x = 0; x < w; x++) {
			for (double y = 0; y < h; y++) {
				if (y > slope1 * x + (aY - aX * slope1) != y > slope2 * x
						+ (cY - cX * slope2)) { // if above line 1 == above line
					// 2
					// if((y+x)/(((double)h+(double)w))<.5==((double)h/(double)w>y/x)){
					background.setPixel((int) x, (int) y, Global.player1Color);
				} else {
					background.setPixel((int) x, (int) y, Global.player2Color);
				}
				// (((h*w)-h)>y/x)
			}
		}
		return background;

	}

	public static byte[][] teamGrid() { //not yet in use but will be used to send netcode
		byte[][] loyalty = new byte[Global.gridSize][Global.gridSize];
		RegularPolygonGameObject[][] gamePeace = Global.gamePiece;
		for (int x = 0; x < gamePeace.length; x++)
			for (int y = 0; y < gamePeace[0].length; y++)
				loyalty[x][y] = gamePeace[x][y].getTeam();
		return loyalty;
	}
	public static void clearBoard(){
		for(RegularPolygonGameObject[] things:Global.gamePiece)
			for(RegularPolygonGameObject guy:things)
				if(guy!=null)guy.setTeam((byte)0);
	}
	@SuppressWarnings("unused")
	public static void setBoard(){
		for(RegularPolygonGameObject[] things:Global.gamePiece)
			for(RegularPolygonGameObject guy:things)
				guy= new RegularPolygonGameObject();
	}
}