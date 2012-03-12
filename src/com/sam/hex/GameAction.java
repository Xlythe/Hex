package com.sam.hex;

import android.graphics.Point;

public class GameAction {
	private static int[][] gameboard;
	private static int n;
	private static Point[][] polyXY;
	private static RegularPolygonGameObject hex;

	public static boolean checkWinPlayer1() {
		for (int i = 0; i < Global.gridSize; i++) {
			if (RegularPolygonGameObject.checkWinTeam((byte) 1,
					Global.gridSize, i, Global.gamePiece)) {
				System.out.println("Player one wins");
				String path=RegularPolygonGameObject.findShortestPath((byte) 1,
						Global.gridSize, i, Global.gamePiece);
				RegularPolygonGameObject.colorPath(Global.gridSize,i,path);
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkWinPlayer2() {
		for (int i = 0; i < Global.gridSize; i++) {
			if (RegularPolygonGameObject.checkWinTeam((byte) 2, i,
					Global.gridSize, Global.gamePiece)) {
				System.out.println("Player Two wins");
				RegularPolygonGameObject.findShortestPath((byte) 2, i,
				Global.gridSize, Global.gamePiece);
				return true;
			}
		}
		return false;
	}

	public static void checkedFlagReset() {
		for (int x = Global.gridSize - 1; x >= 0; x--) {
			for (int y = Global.gridSize - 1; y >= 0; y--) {
				Global.gamePiece[x][y].checkedflage = false;
			}
		}
	}
/*	public static void updateBoard() { //should be handled in boardview
		if (HexGameWindow.cPolygons.getWidth() != Global.windowWidth
				|| HexGameWindow.cPolygons.getHeight() != Global.windowHeight) {
			fullUpdateBoard();
		}

		HexGameWindow.cPolygons.revalidate();
		HexGameWindow.cPolygons.repaint();

	}

	public static void fullUpdateBoard() { //should be handled in boardview
		Global.windowWidth = HexGameWindow.cPolygons.getWidth();
		Global.windowHeight = HexGameWindow.cPolygons.getHeight();
		double radius;
		RegularPolygonGameObject[][] gamePeace = Global.gamePiece;
		radius = BoardTools.radiusCalculator(Global.windowWidth,
				Global.windowHeight, Global.gridSize);
		// radius = BoardTools.radiusCalculator(400,400, 7);
		double hrad = radius * Math.sqrt(3) / 2; // Horizontal radius
		int yOffset = (int) ((HexGameWindow.cPolygons.getHeight() - ((3 * radius / 2)
				* (gamePeace[0].length - 1) + 2 * radius)) / 2);
		int xOffset = (int) ((HexGameWindow.cPolygons.getWidth() - (hrad
				* gamePeace.length * 2 + hrad * (gamePeace[0].length - 1))) / 2);

		for (int xc = 0; xc < Global.gamePiece.length; xc++) {
			for (int yc = 0; yc < gamePeace[0].length; yc++)
				gamePeace[xc][yc].update((hrad + yc * hrad + 2 * hrad * xc)
						+ xOffset, (1.5 * radius * yc + radius) + yOffset,
						radius, 6, Math.PI / 2);
		}

		BoardTools.setBackground(Global.windowWidth, Global.windowHeight);
		HexGameWindow.cPolygons.revalidate();
		HexGameWindow.cPolygons.repaint();

	}*/

	public static void setPiece(RegularPolygonGameObject h) {
		hex = h;
	}

	public static void getPlayerTurn(byte team) {
		while (true) {
			while (hex == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (hex.getTeam() == 0) {
				hex.setTeam(team);
				hex = null;
				break;
			}
			hex = null;
		}
	}
	
	public void setGame(int m){
		n=m;
		gameboard = new int[n][n];
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				gameboard[i][j]=0;
			}
		}
		
		polyXY = new Point[n][n];
	}
	
	public boolean makeMove(int X, int Y, int team){
    	for(int i=getN()-1;i>-1;i--){
    		for(int j=getN()-1;j>-1;j--){
    			if(X>getPolyXY()[i][j].x && Y>getPolyXY()[i][j].y){
    				if(gameboard[i][j]==0){
    					gameboard[i][j] = team;
    					return true;
    				}
    				else{
    					return false;
    				}
    			}
    		}
    	}
    	return false;
    }
	
	public int getN(){
		return n;
	}
	
	public int[][] getGameboard(){
		return gameboard;
	}
	
	public Point[][] getPolyXY(){
		return polyXY;
	}
	
	public void setPolyXY(int x, int y, Point cord){
		polyXY[x][y] = cord;
	}
}