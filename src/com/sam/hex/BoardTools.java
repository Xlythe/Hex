package com.sam.hex;

public class BoardTools{
	public static void setGame(int m){
		Global.setN(m);
		clearBoard();
		Global.setPolyXY(new Posn[Global.getN()][Global.getN()]);
	}
	
	public static void makeMove(final int x, final int y, final byte team){
		if(Global.getRunning() && Global.getGameboard()[x][y]==0){
			//Play move
			Global.setGameboard(x,y,team);
        	Global.addToMoveList(new Posn(x,y));
		}
	}
	
	public static byte[][] teamGrid(){
		return Global.getGameboard();
	}
	
	public static Posn[][] getPolyXY(){
		return Global.getPolyXY();
	}
	
	public static void setPolyXY(int x, int y, Posn cord){
		Global.setPolyXY(x, y, cord);
	}
	
	public static void clearBoard(){
		Global.setGameboard(new byte[Global.getN()][Global.getN()]);
		for(int i=0;i<Global.getN();i++){
			for(int j=0;j<Global.getN();j++){
				Global.getGameboard()[i][j]=0;
			}
		}
	}
	
	public static void clearMoveList(){
		Global.clearMoveList();
	}
	
	public static void updateCurrentPlayer(){
		Global.setCurrentPlayer((byte) (Global.getCurrentPlayer()%2+1));
	}
	
	public static boolean checkWinPlayer1() {
		//Moves to check
		//(x),(y+1) Forward One
		//(x),(y-1) Back One
		//(x+1),(y-1) Down and Back
		//(x+1),(y) Down and Forward
		//(x-1),(y) Up and Back 
		//(x-1),(y+1) Up and Forward
		byte[][] flags = new byte[Global.getN()][Global.getN()];
		for(int i=0;i<Global.getN();i++){
			for(int j=0;j<Global.getN();j++){
				flags[i][j]=0;
			}
		}
		for(int i=0;i<Global.getN();i++){
			if(recursiveCheckP1(new Posn(i,0),flags,flags)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean recursiveCheckP1(Posn hex, byte[][] flags, byte[][] allFlags){
		byte[][] checked = new byte[Global.getN()][Global.getN()];
		for(int i=0;i<Global.getN();i++){
			checked[i] = flags[i].clone();
		}
		if(Global.getGameboard()[hex.getX()][hex.getY()]!=(byte) 1 || checked[hex.getX()][hex.getY()]==1 || allFlags[hex.getX()][hex.getY()]==1){
			//This isn't a valid piece
			return false;
		}
		else if(hex.getY()==Global.getN()-1){
			//We made it to the other side
			checked[hex.getX()][hex.getY()]=1;
			allFlags[hex.getX()][hex.getY()]=1;
			for(int i=0;i<Global.getN();i++){
				for(int j=0;j<Global.getN();j++){
					if(checked[i][j]==1){
						Global.setGameboard(i, j, (byte) 3);
					}
				}
			}
			return true;
		}
		else{
			//Check surrounding pieces
			checked[hex.getX()][hex.getY()]=1;
			allFlags[hex.getX()][hex.getY()]=1;
			boolean rest = false;
			if(hex.getX()>0){
				rest = rest || 
						recursiveCheckP1(new Posn(hex.getX()-1,hex.getY()),checked,allFlags);
				if(hex.getY()<Global.getN()-1){
					recursiveCheckP1(new Posn(hex.getX()-1,hex.getY()+1),checked,allFlags);
				}
			}
			if(hex.getX()<Global.getN()-1){
				rest = rest || 
						recursiveCheckP1(new Posn(hex.getX()+1,hex.getY()),checked,allFlags);
				if(hex.getY()>0){
					rest = rest || 
							recursiveCheckP1(new Posn(hex.getX()+1,hex.getY()-1),checked,allFlags);
				}
			}
			if(hex.getY()>0){
				rest = rest || 
						recursiveCheckP1(new Posn(hex.getX(),hex.getY()-1),checked,allFlags);
			}
			if(hex.getY()<Global.getN()-1){
				rest = rest || 
						recursiveCheckP1(new Posn(hex.getX(),hex.getY()+1),checked,allFlags);
			}
			return rest;
		}
	}
	
	public static boolean checkWinPlayer2() {
		//Moves to check
		//(x),(y+1) Forward One
		//(x),(y-1) Back One
		//(x+1),(y-1) Down and Back
		//(x+1),(y) Down and Forward
		//(x-1),(y) Up and Back 
		//(x-1),(y+1) Up and Forward
		byte[][] flags = new byte[Global.getN()][Global.getN()];
		for(int i=0;i<Global.getN();i++){
			for(int j=0;j<Global.getN();j++){
				flags[i][j]=0;
			}
		}
		for(int i=0;i<Global.getN();i++){
			if(recursiveCheckP2(new Posn(0,i),flags,flags)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean recursiveCheckP2(Posn hex, byte[][] flags, byte[][] allFlags){
		byte[][] checked = new byte[Global.getN()][Global.getN()];
		for(int i=0;i<Global.getN();i++){
			checked[i] = flags[i].clone();
		}
		if(Global.getGameboard()[hex.getX()][hex.getY()]!=(byte) 2 || checked[hex.getX()][hex.getY()]==1 || allFlags[hex.getX()][hex.getY()]==1){
			//This isn't a valid piece
			return false;
		}
		else if(hex.getX()==Global.getN()-1){
			//We made it to the other side
			checked[hex.getX()][hex.getY()]=1;
			allFlags[hex.getX()][hex.getY()]=1;
			for(int i=0;i<Global.getN();i++){
				for(int j=0;j<Global.getN();j++){
					if(checked[i][j]==1){
						Global.setGameboard(i, j, (byte) 4);
					}
				}
			}
			return true;
		}
		else{
			//Check surrounding pieces
			checked[hex.getX()][hex.getY()]=1;
			allFlags[hex.getX()][hex.getY()]=1;
			boolean rest = false;
			if(hex.getX()>0){
				rest = rest || 
						recursiveCheckP2(new Posn(hex.getX()-1,hex.getY()),checked,allFlags);
				if(hex.getY()<Global.getN()-1){
					recursiveCheckP2(new Posn(hex.getX()-1,hex.getY()+1),checked,allFlags);
				}
			}
			if(hex.getX()<Global.getN()-1){
				rest = rest || 
						recursiveCheckP2(new Posn(hex.getX()+1,hex.getY()),checked,allFlags);
				if(hex.getY()>0){
					rest = rest || 
							recursiveCheckP2(new Posn(hex.getX()+1,hex.getY()-1),checked,allFlags);
				}
			}
			if(hex.getY()>0){
				rest = rest || 
						recursiveCheckP2(new Posn(hex.getX(),hex.getY()-1),checked,allFlags);
			}
			if(hex.getY()<Global.getN()-1){
				rest = rest || 
						recursiveCheckP2(new Posn(hex.getX(),hex.getY()+1),checked,allFlags);
			}
			return rest;
		}
	}
}