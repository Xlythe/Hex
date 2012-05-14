package com.sam.hex;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sam.hex.lan.LANGlobal;
import com.sam.hex.lan.LocalPlayerObject;
import com.sam.hex.net.NetGlobal;
import com.sam.hex.net.NetPlayerObject;

import android.graphics.Point;
import android.view.View;

public class GameAction {
	private GameAction(){}
	
	public static synchronized boolean checkWinPlayer(int team, GameObject game) {
		if(team==1){
			if(game.timer.type!=0 && game.player2.getTime()<0) return true;
			if(game.player2.giveUp()) return true;
			for (int i = 0; i < game.gridSize; i++) {
				if (RegularPolygonGameObject.checkWinTeam((byte) 1, game.gridSize, i, game.gamePiece)) {
					System.out.println("Player one wins");
					checkedFlagReset(game);
					String path=RegularPolygonGameObject.findShortestPath((byte) 1, game.gridSize, i, game.gamePiece);
					RegularPolygonGameObject.colorPath(game.gridSize,i,path,game);
					return true;
				}
			}
			return false;
		}
		else{
			if(game.timer.type!=0 && game.player1.getTime()<0) return true;
			if(game.player1.giveUp()) return true;
			for (int i = 0; i < game.gridSize; i++) {
				if (RegularPolygonGameObject.checkWinTeam((byte) 2, i, game.gridSize, game.gamePiece)) {
					System.out.println("Player two wins");
					checkedFlagReset(game);
					String path=RegularPolygonGameObject.findShortestPath((byte) 2, i, game.gridSize, game.gamePiece);
					RegularPolygonGameObject.colorPath(i,game.gridSize,path,game);
					return true;
				}
			}
			return false;
		}
	}

	public static void checkedFlagReset(GameObject game) {
		for (int x = game.gridSize - 1; x >= 0; x--) {
			for (int y = game.gridSize - 1; y >= 0; y--) {
				game.gamePiece[x][y].checkedflage = false;
			}
		}
	}
	
	public static void setPiece(Point p, GameObject game) {
		getPlayer(game.currentPlayer, game).setMove(new GameAction(),p);
	}
	
	private static void setTeam(byte t,int x,int y, GameObject game) {
		game.moveList.makeMove(x, y, t, System.currentTimeMillis()-game.moveStart, game.moveNumber);
		game.gamePiece[x][y].setTeam(t,game);
		game.moveNumber++;
		game.board.postInvalidate();
	}
	
	public static boolean makeMove(PlayingEntity player, byte team, Point hex, GameObject game){
		if(player!=null && game.gamePiece[hex.x][hex.y].getTeam() == 0){
			setTeam(team,hex.x,hex.y,game);
			return true;
		}
		else if(player!=null && game.moveNumber==2 && game.gamePiece[hex.x][hex.y].getTeam() == 1){//Swap rule
	    	if(game.swap){
				setTeam(team,hex.x,hex.y,game);
				return true;
	    	}
		}
		return false;
	}
	
	public static void undo(int gameLocation, GameObject game){
		if(game.moveNumber>1 && game.player1.supportsUndo() && game.player2.supportsUndo()){
			checkedFlagReset(game);
			
			//Remove the piece from the board and the movelist
			Move lastMove = game.moveList.thisMove;
			game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
			game.moveList = game.moveList.nextMove;
			game.moveList.replay(0,game);
			game.moveNumber--;
			
			if(gameLocation==Global.GAME_LOCATION){
				//Determine who is a human
				boolean p1 = game.player1 instanceof PlayerObject;
				boolean p2 = game.player2 instanceof PlayerObject;
				if(game.gameOver) game.currentPlayer = (game.currentPlayer%2)+1;
				
				if(game.currentPlayer==1 && p1){//It's a human's turn
					game.player2.undoCalled();//Tell the other player we're going back a turn
					
					if(!p2){//If the other person isn't a human, undo again
						if(game.moveNumber>1){
							lastMove = game.moveList.thisMove;
							game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
							game.moveList = game.moveList.nextMove;
							game.moveNumber--;
						}
						else{
							getPlayer(game.currentPlayer, game).endMove();
						}
					}
					else{
						//Otherwise, cede the turn to the other player
						getPlayer(game.currentPlayer, game).endMove();
					}
				}
				else if(game.currentPlayer==1 && !p1){
					if(!game.gameOver){
						game.player1.undoCalled();
					}
				}
				else if(game.currentPlayer==2 && p2){
					game.player1.undoCalled();
					
					//If the other person isn't a (local) human
					if(!p1){
						//Undo again
						if(game.moveNumber>1){
							lastMove = game.moveList.thisMove;
							game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
							game.moveList = game.moveList.nextMove;
							game.moveNumber--;
						}
						else{
							getPlayer(game.currentPlayer, game).endMove();
						}
					}
					else{
						//Otherwise, cede the turn to the other player
						getPlayer(game.currentPlayer, game).endMove();
					}
				}
				else if(game.currentPlayer==2 && !p2){
					if(!game.gameOver) {
						game.player2.undoCalled();
					}
				}
				if(game.gameOver && ((game.currentPlayer==2 && p1) || (game.currentPlayer==1 && p2))) game.currentPlayer = (game.currentPlayer%2)+1;
			}
			else if(gameLocation==LANGlobal.GAME_LOCATION){//Inside a LAN game
				if(game.currentPlayer==1){//First player's turn
					if(game.player1 instanceof LocalPlayerObject){//First player is on the network (not local)
						if(LANGlobal.undoRequested){//First player requested the undo
							//undo twice, don't switch players
							if(game.moveNumber>1){
								lastMove = game.moveList.thisMove;
								game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
								game.moveList = game.moveList.nextMove;
								game.moveNumber--;
							}
							if(game.gameOver) game.currentPlayer = (game.currentPlayer%2)+1;
						}
						else{//Second player requested the undo
							//undo once, switch players
							GameAction.getPlayer(game.currentPlayer, game).endMove();
						}
					}
					else{//First player is local (not on the network)
						if(LANGlobal.undoRequested){//Second player requested the undo
							//undo once, switch players
							getPlayer(game.currentPlayer, game).endMove();
						}
						else{//First player requested the undo
							//undo twice, don't switch players
							if(game.moveNumber>1){
								lastMove = game.moveList.thisMove;
								game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
								game.moveList = game.moveList.nextMove;
								game.moveNumber--;
							}
							if(game.gameOver) game.currentPlayer = (game.currentPlayer%2)+1;
						}
					}
				}
				else{//Second player's turn
					if(game.player2 instanceof LocalPlayerObject){//Second player is local (not on the network)
						if(LANGlobal.undoRequested){//First player requested the undo
							//undo once, switch players
							getPlayer(game.currentPlayer, game).endMove();
						}
						else{//Second player requested the undo
							//undo twice, don't switch players
							if(game.moveNumber>1){
								lastMove = game.moveList.thisMove;
								game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
								game.moveList = game.moveList.nextMove;
								game.moveNumber--;
							}
							if(game.gameOver) game.currentPlayer = (game.currentPlayer%2)+1;
						}
					}
					else{//Second player is on the network (not local)
						if(LANGlobal.undoRequested){//Second player requested the undo
							//undo twice, don't switch players
							if(game.moveNumber>1){
								lastMove = game.moveList.thisMove;
								game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
								game.moveList = game.moveList.nextMove;
								game.moveNumber--;
							}
							if(game.gameOver) game.currentPlayer = (game.currentPlayer%2)+1;
						}
						else{//First player requested the undo
							//undo once, switch players
							GameAction.getPlayer(game.currentPlayer, game).endMove();
						}
					}
				}
				
				LANGlobal.undoRequested = false;
			}
			else if(gameLocation==NetGlobal.GAME_LOCATION){//Inside a net game
				if(game.currentPlayer==1){//First player's turn
					if(game.player1 instanceof NetPlayerObject){//First player is on the network (not local)
						if(NetGlobal.undoRequested){//First player requested the undo
							//undo twice, don't switch players
							if(game.moveNumber>1){
								lastMove = game.moveList.thisMove;
								game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
								game.moveList = game.moveList.nextMove;
								game.moveNumber--;
							}
							if(game.gameOver) game.currentPlayer = (game.currentPlayer%2)+1;
						}
						else{//Second player requested the undo
							//undo once, switch players
							GameAction.getPlayer(game.currentPlayer, game).endMove();
						}
					}
					else{//First player is local (not on the network)
						if(NetGlobal.undoRequested){//Second player requested the undo
							//undo once, switch players
							getPlayer(game.currentPlayer, game).endMove();
						}
						else{//First player requested the undo
							//undo twice, don't switch players
							if(game.moveNumber>1){
								lastMove = game.moveList.thisMove;
								game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
								game.moveList = game.moveList.nextMove;
								game.moveNumber--;
							}
							if(game.gameOver) game.currentPlayer = (game.currentPlayer%2)+1;
						}
					}
				}
				else{//Second player's turn
					if(game.player2 instanceof NetPlayerObject){//Second player is local (not on the network)
						if(NetGlobal.undoRequested){//First player requested the undo
							//undo once, switch players
							getPlayer(game.currentPlayer, game).endMove();
						}
						else{//Second player requested the undo
							//undo twice, don't switch players
							if(game.moveNumber>1){
								lastMove = game.moveList.thisMove;
								game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
								game.moveList = game.moveList.nextMove;
								game.moveNumber--;
							}
							if(game.gameOver) game.currentPlayer = (game.currentPlayer%2)+1;
						}
					}
					else{//Second player is on the network (not local)
						if(NetGlobal.undoRequested){//Second player requested the undo
							//undo twice, don't switch players
							if(game.moveNumber>1){
								lastMove = game.moveList.thisMove;
								game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)0,game);
								game.moveList = game.moveList.nextMove;
								game.moveNumber--;
							}
							if(game.gameOver) game.currentPlayer = (game.currentPlayer%2)+1;
						}
						else{//First player requested the undo
							//undo once, switch players
							GameAction.getPlayer(game.currentPlayer, game).endMove();
						}
					}
				}
				
				NetGlobal.undoRequested = false;
			}
			
			//Reset the game if it's already ended
			if(game.gameOver){
				game.moveList.replay(0,game);
				game.start();
			}
		}
		
		game.board.postInvalidate();
	}
	
	public static class AnnounceWinner{
		public AnnounceWinner(final int team, final GameObject game){
			game.handler.post(new Runnable(){
				public void run(){
					game.winnerMsg = insert(game.board.getContext().getString(R.string.winner), getPlayer(team, game).getName());
					game.winnerText.setText(game.winnerMsg);
					game.winnerText.setVisibility(View.VISIBLE);
					game.winnerText.invalidate();
					game.timerText.setVisibility(View.GONE);
					game.timerText.invalidate();
				}
			});
		}
	}
	
	public static String insert(String text, String name){
		String inserted = text.replaceAll("#",name);
		return inserted;
	}
	
	public static PlayingEntity getPlayer(int i, GameObject game){
		if(i==1){
			return game.player1;
		}
		else if(i==2){
			return game.player2;
		}
		else{
			return null;
		}
	}

	final static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static String pointToString(Point p, GameObject game){
		if(game.moveNumber==2 && game.moveList.thisMove.equals(game.moveList.nextMove.thisMove)) return "SWAP";
		String str = "";
		str += alphabet.charAt(p.y);
		str += (p.x+1);
		return str;
	}
	
	public static Point stringToPoint(String str, GameObject game){
		if(game.moveNumber==1 && str.equals("SWAP")) return new Point(-1,-1);
		if(str.equals("SWAP")) return new Point(game.moveList.thisMove.getX(),game.moveList.thisMove.getY());
		int x = Integer.parseInt(str.substring(1))-1;
		char y = str.charAt(0);
		
		return new Point(x, alphabet.indexOf(y));
	}
	
	public static String md5(String s) {
		MessageDigest digest;
	    try {
	        digest = MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes(),0,s.length());
	        String hash = new BigInteger(1, digest.digest()).toString(16);
	        return hash;
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

}