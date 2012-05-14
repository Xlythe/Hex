package com.sam.hex;

import android.graphics.Color;

import sl.shapes.RegularPolygon;

public class RegularPolygonGameObject {
	private RegularPolygon Hex;
	private byte teamNumber=0; // 1 is left-right, 2 is top-down
	private int objectColor = Color.WHITE;
	boolean checkedflage = false;
	double x;
	double y;
	double radius;

	public RegularPolygonGameObject() {
		x=0;
		y=0;
		radius=0;
	}
	
	public RegularPolygonGameObject(double x, double y, double r) {
		this.x=x;
		this.y=y;
		radius=r;
	}
	
	public void set(double x, double y, double r){
		this.x=x;
		this.y=y;
		radius=r;
		update(x,y,r,6, Math.PI / 2);
	}
	
	public RegularPolygonGameObject(double x, double y, double r, int vertexCount) {
		Hex = new RegularPolygon(x, y, r, vertexCount);
	}

	public RegularPolygonGameObject(double x, double y, double r, int vertexCount, double startAngle) {
		Hex = new RegularPolygon(x, y, r, vertexCount, startAngle);
	}

	public void update(double x, double y, double r, int vertexCount, double startAngle) {
		Hex = new RegularPolygon(x, y, r, vertexCount, startAngle);
	}
	public void setTeam(byte t, GameObject game) {
		teamNumber = t;
		if (teamNumber == 1)
			setColor(game.player1.getColor());
		else if(teamNumber==2)
			setColor(game.player2.getColor());
		else
			setColor(Color.WHITE);
	}

	public byte getTeam() {
		return teamNumber;
	}

	public boolean checkpiece(byte team, int x, int y, RegularPolygonGameObject[][] gamePeace) { //used for checking victory conditions
		if (team == teamNumber && !checkedflage) {
			checkedflage = !checkedflage;
			if (checkSpot(team, x, y) || checkWinTeam(team, x, y, gamePeace)) {
//				objectColor = Color.GREEN;
				return true;
			}
		}
		return false;
	}

	public static boolean checkWinTeam(byte team, int x, int y, RegularPolygonGameObject[][] gamePeace) { //used for checking victory condition
		if (y < gamePeace.length && x - 1 >= 0
				&& gamePeace[x - 1][y].checkpiece(team, x - 1, y, gamePeace)) {
			return true;
		}
		if (y < gamePeace.length && x + 1 < gamePeace.length
				&& gamePeace[x + 1][y].checkpiece(team, x + 1, y, gamePeace)) {
			return true;
		}
		if (x < gamePeace.length && y - 1 >= 0
				&& gamePeace[x][y - 1].checkpiece(team, x, y - 1, gamePeace)) {
			return true;
		}
		if (x < gamePeace.length && y + 1 < gamePeace.length
				&& gamePeace[x][y + 1].checkpiece(team, x, y + 1, gamePeace)) {
			return true;
		}
		if (y + 1 < gamePeace.length
				&& x - 1 >= 0
				&& gamePeace[x - 1][y + 1].checkpiece(team, x - 1, y + 1,
						gamePeace)) {
			return true;
		}
		if (y - 1 < gamePeace.length
				&& x + 1 < gamePeace.length
				&& y - 1 >= 0
				&& gamePeace[x + 1][y - 1].checkpiece(team, x + 1, y - 1,
						gamePeace)) {
			return true;
		}


		return false;
	}
	public String checkpieceShort(byte team, int x, int y, RegularPolygonGameObject[][] gamePeace) { //used for checking victory condition
		if (team == teamNumber && !checkedflage) {
			checkedflage = true;
			String tempHolder=findShortestPath(team, x, y, gamePeace);
			checkedflage = false;
			if (tempHolder!=null) {
				return tempHolder;
			}
			checkedflage = false;
		}
		
		return null;

	}
	public static String findShortestPath(byte team, int x, int y, RegularPolygonGameObject[][] gamePeace) { //used for checking victory condition
		if(checkSpot(team, x, y)){return "";}
		String[] allPath=new String[6];
		
		if (y < gamePeace.length && x - 1 >= 0) {
			allPath[0]=gamePeace[x - 1][y].checkpieceShort(team, x - 1, y, gamePeace);
		}
		if (y < gamePeace.length && x + 1 < gamePeace.length) {
			allPath[1]=gamePeace[x + 1][y].checkpieceShort(team, x + 1, y, gamePeace);
			
		}
		if (x < gamePeace.length && y - 1 >= 0) {
			allPath[2]=gamePeace[x][y - 1].checkpieceShort(team, x, y - 1, gamePeace);
		}
		if (x < gamePeace.length && y + 1 < gamePeace.length) {
			allPath[3]=gamePeace[x][y + 1].checkpieceShort(team, x, y + 1, gamePeace);
		}
		if (y + 1 < gamePeace.length
				&& x - 1 >= 0) {
			allPath[4]=gamePeace[x - 1][y + 1].checkpieceShort(team, x - 1, y + 1,
					gamePeace);
		}
		
		if (y - 1 < gamePeace.length
				&& x + 1 < gamePeace.length
				&& y - 1 >= 0) {
			allPath[5]=gamePeace[x + 1][y - 1].checkpieceShort(team, x + 1, y - 1,
					gamePeace);
		}
		 int dir= findShortestString(allPath,0,5);
		 if (allPath[dir]==null||allPath[dir]=="null") return null;
		 switch (dir){
		 //ud=y-1 & x+1  dd = y+1 & x-1  uy=y-1 dy=y+1 lx=x-1 rx=x+1
	     case 0: return "lx"+allPath[0];
	     case 1: return "rx"+allPath[1];
	     case 2: return "uy"+allPath[2];
	     case 3: return "dy"+allPath[3];
	     case 4: return "dd"+allPath[4];
	     case 5: return "ud"+allPath[5];
		 }
		return null;
	}
	
	static int findShortestString(String[] paths, int lo, int hi) { //used for checking victory condition
		if ((lo==hi)){return hi;}
		int temp =findShortestString(paths,lo+1,hi);
		return  stringL(paths[lo])<stringL(paths[temp])?lo:temp;
		
	}
	private static int stringL(String temp){ //used for checking victory condition
		if (temp==null) return Integer.MAX_VALUE;
		else return temp.length();
		
	}
	
	private enum posDir{
		lx,rx,uy,dy,dd,ud
		
	}
	
	public static void colorPath(int x,int y, String path, GameObject game){
		while (path!=null&&path.length()!=0){
			switch (posDir.valueOf(path.substring(0,2))){
			 //ud=y-1 & x+1  dd = y+1 & x-1  uy=y-1 dy=y+1 lx=x-1 rx=x+1
			case lx: x-=1; break;
			case rx: x+=1;break;
			case uy: y-=1;break;
			case dy: y+=1;break;
			case dd:  y+=1; x-=1;break;
			case ud:  y-=1; x+=1; break;
			}
			game.gamePiece[x][y].setColor(Color.GREEN);
			path=path.substring(2,path.length());
		} System.out.println("done");
	}
	
	public static boolean checkSpot(byte team, int x, int y) {
		if (team == 1 && x == 0) {
			return true;
		}
		if (team == 2 && y == 0) {
			return true;
		}
		return false;
	}

	public void setColor(int c) {
		objectColor = c;
	}

	public int getColor() {
		return objectColor;
	}

	public boolean contains(double ex, double why) { //TODO: hit detect MAY be off slightly...
		
		
		//return Math.abs(x-ex)<radius/2 && Math.abs(y-why)<radius/2; //very simplified!
		return Hex.contains((int)ex,(int)why);
	}

}
