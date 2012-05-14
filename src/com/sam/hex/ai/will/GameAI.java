package com.sam.hex.ai.will;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.sam.hex.BoardTools;
import com.sam.hex.GameAction;
import com.sam.hex.GameObject;
import com.sam.hex.PlayerObject;
import com.sam.hex.PlayingEntity;

import android.graphics.Point;

/**
 * @author Will Harmon
 **/
public class GameAI implements PlayingEntity { 
	private String name;
	private int color;
	private long timeLeft;
	private int team;//1 is left-right, 2 is top-down
	private byte[][] gameBoard;
	private int[] n={0,0},m = {0,0};//n is the leftmost AI move, m is the rightmost AI move
	private List<List<List<Integer>>> pairs = new ArrayList<List<List<Integer>>>();//List of pair-pieces
	private List<AIHistoryObject> history = new LinkedList<AIHistoryObject>();//List of the AI's state. Used when Undo is called.
	private int rand_a = 0;
	private int rand_b = 0;
	private boolean skipMove = false;
	private GameObject game;
	
	public GameAI(int team, GameObject game){
		this.team=team;
		this.game=game;
		while(rand_a==0 && rand_b==0){
			rand_a = new Random().nextInt(3)-1;
			rand_b = new Random().nextInt(3)-1;
		}
	}
	
	public class AIHistoryObject{
		List<List<List<Integer>>> pairs = new ArrayList<List<List<Integer>>>();;
		int[] n = {0,0};
		int[] m = {0,0};
		
		public AIHistoryObject(List<List<List<Integer>>> pairs, int[] n, int[] m) {
			for(int i=0;i<pairs.size();i++){
				this.pairs.add(pairs.get(i));
			}
			this.n[0] = n[0];
			this.n[1] = n[1];
			this.m[0] = m[0];
			this.m[1] = m[1];
		}
		
		public String toString(){
			return pairs.toString()+" : "+n.toString()+" : "+m.toString();
		}
	}
	
	@Override
	public void getPlayerTurn() {
		skipMove = false;
		this.gameBoard=BoardTools.teamGrid(game);
		AIHistoryObject state = new AIHistoryObject(pairs, n, m);
		history.add(state);
		makeMove();
	}
	
	@Override
	public void undoCalled(){
		if(history.size()>0){
			AIHistoryObject previousState = history.get(history.size()-1);
			pairs = previousState.pairs;
			n = previousState.n;
			m = previousState.m;
			history.remove(history.size()-1);
		}
		skipMove = true;
	}
	
	private boolean right(){
		return m[0]+2 <= gameBoard.length-1 && m[1]+1 <= gameBoard.length-1 && m[1]-1 >= 0;
	}
	private boolean left(){
		return n[0]-2 >= 0 && n[1]-1 >= 0 && n[1]+1 <= gameBoard.length-1;
	}

	private void makeMove(){
		/**
		 * Will's AI
		 * */
		int x = 0;
		int y = 0;
		if(team==2){
			x++;
		}
		else if(team==1){
			y++;
		}
		
		//Sleep to stop instantaneous playing
		try {
			for(int i=0;i<10;i++){
    			Thread.sleep(50);
    			if(game.gameOver) break;
    		}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try{
			//Play in the middle if possible
			int mid = 1;
			mid*=(gameBoard.length-1)/2;
			if(gameBoard[mid][mid]==0){
				n[0] = mid;//horizontal
				n[1] = mid;//vertical
				m[0] = mid;
				m[1] = mid;
				sendMove(mid,mid);
				
				return;
			}
			else if(gameBoard[mid][mid]!=team && gameBoard[mid+rand_a][mid+rand_b]==0){
				n[x] = mid+rand_a;//horizontal
				n[y] = mid+rand_b;//vertical
				m[x] = mid+rand_a;
				m[y] = mid+rand_b;
				sendMove(mid+rand_a,mid+rand_b);
	
				return;
			}
			
			//Add the edges as pairs after we've reached both sides of the map
			if(n[0]-1 == 0){
				List<List<Integer>> pair = new LinkedList<List<Integer>>();
				List<Integer> cord1 = new LinkedList<Integer>();
				List<Integer> cord2 = new LinkedList<Integer>();
				cord1.add(n[0]-1);
				cord1.add(n[1]);
				pair.add(cord1);
				cord2.add(n[0]-1);
				cord2.add(n[1]+1);
				pair.add(cord2);
				pairs.add(pair);
				
				n[0] = n[0]-1;
			}
			if(m[0]+1 == gameBoard.length-1){
				List<List<Integer>> pair = new LinkedList<List<Integer>>();
				List<Integer> cord1 = new LinkedList<Integer>();
				List<Integer> cord2 = new LinkedList<Integer>();
				cord1.add(m[0]+1);
				cord1.add(m[1]);
				pair.add(cord1);
				cord2.add(m[0]+1);
				cord2.add(m[1]-1);
				pair.add(cord2);
				pairs.add(pair);
				
				m[0] = m[0]+1;
			}
			
			//Check if one of our pairs is being attacked, and fill in the alternate if so
			for(int i=0; i<pairs.size(); i++){
				if(gameBoard[pairs.get(i).get(0).get(x)][pairs.get(i).get(0).get(y)]==0 || gameBoard[pairs.get(i).get(1).get(x)][pairs.get(i).get(1).get(y)]==0){
					if(gameBoard[pairs.get(i).get(0).get(x)][pairs.get(i).get(0).get(y)]!=0){
						sendMove(pairs.get(i).get(1).get(x),pairs.get(i).get(1).get(y));
						pairs.remove(i);
						return;
					}
					else if(gameBoard[pairs.get(i).get(1).get(x)][pairs.get(i).get(1).get(y)]!=0){
						sendMove(pairs.get(i).get(0).get(x),pairs.get(i).get(0).get(y));
						pairs.remove(i);
						return;
					}
				}
				else{
					pairs.remove(i);
				}
			}
			
			//Check if they were sneaky and played in front of us
			if(right() && gameBoard[m[x]+0*x+1*y][m[y]+1*x+0*y]!=0){
				if(gameBoard[m[x]-1*x+1*y][m[y]-1*y+1*x]==0){
					m[0] = m[0]+1;
					m[1] = m[1]-1;
					
					sendMove(m[x],m[y]);
					return;
				}
				else if(gameBoard[m[x]+1*x+0*y][m[y]+1*y+0*x]==0){
					m[0] = m[0];
					m[1] = m[1]+1;
					
					sendMove(m[x],m[y]);
					return;
				}
			}
			if(right() && (gameBoard[m[x]-1*x+1*y][m[y]-1*y+1*x]!=0 || gameBoard[m[x]+1*x+0*y][m[y]+1*y+0*x]!=0) && gameBoard[m[x]+0*x+1*y][m[y]+0*y+1*x]==0){
				m[0] = m[0]+1;
				m[1] = m[1];
				
				sendMove(m[x],m[y]);
				return;
			}
			//Check if they were sneakier and played behind us
			if(left() && gameBoard[n[x]+0*x-1*y][n[y]+0*y-1*x]!=0){
				if(gameBoard[n[x]+1*x-1*y][n[y]+1*y-1*x]==0){
					n[0] = n[0]-1;
					n[1] = n[1]+1;
					
					sendMove(n[x],n[y]);
					return;
				}
				else if(gameBoard[n[x]-1*x+0*y][n[y]-1*y+0*x]==0){
					n[0] = n[0];
					n[1] = n[1]-1;
					
					sendMove(n[x],n[y]);
					return;
				}
			}
			if(left() && (gameBoard[n[x]+1*x-1*y][n[y]+1*y-1*x]!=0 || gameBoard[n[x]-1*x+0*y][n[y]-1*y+0*x]!=0) && gameBoard[n[x]+0*x-1*y][n[y]+0*y-1*x]==0){
				n[0] = n[0]-1;
				n[1] = n[1];
				
				sendMove(n[x],n[y]);
				return;
			}
			
			
			//Check if we should extend to the left
			if(left()){
				if(gameBoard[n[x]-1*x-1*y][n[y]-1*y-1*x]!=0 && gameBoard[n[x]+1*x-2*y][n[y]+1*y-2*x]==0){
					List<List<Integer>> pair = new LinkedList<List<Integer>>();
					List<Integer> cord1 = new LinkedList<Integer>();
					List<Integer> cord2 = new LinkedList<Integer>();
					cord1.add(n[0]-1);
					cord1.add(n[1]);
					pair.add(cord1);
					cord2.add(n[0]-1);
					cord2.add(n[1]+1);
					pair.add(cord2);
					pairs.add(pair);
					
					n[0] = n[0]-2;
					n[1] = n[1]+1;
					
					sendMove(n[x],n[y]);
					return;
				}
				else if(gameBoard[n[x]+1*x-2*y][n[y]+1*y-2*x]!=0 && gameBoard[n[x]-1*x-1*y][n[y]-1*y-1*x]==0){
					List<List<Integer>> pair = new LinkedList<List<Integer>>();
					List<Integer> cord1 = new LinkedList<Integer>();
					List<Integer> cord2 = new LinkedList<Integer>();
					cord1.add(n[0]);
					cord1.add(n[1]-1);
					pair.add(cord1);
					cord2.add(n[0]-1);
					cord2.add(n[1]);
					pair.add(cord2);
					pairs.add(pair);
	
					n[0] = n[0]-1;
					n[1] = n[1]-1;
					
					sendMove(n[x],n[y]);
					return;
				}
			}
			
			//Check if we should extend to the right
			if(right()){
				if(gameBoard[m[x]-1*x+2*y][m[y]-1*y+2*x]!=0 && gameBoard[m[x]+1*x+1*y][m[y]+1*y+1*x]==0){
					List<List<Integer>> pair = new LinkedList<List<Integer>>();
					List<Integer> cord1 = new LinkedList<Integer>();
					List<Integer> cord2 = new LinkedList<Integer>();
					cord1.add(m[0]+1);
					cord1.add(m[1]);
					pair.add(cord1);
					cord2.add(m[0]);
					cord2.add(m[1]+1);
					pair.add(cord2);
					pairs.add(pair);
	
					m[0] = m[0]+1;
					m[1] = m[1]+1;
					
					sendMove(m[x],m[y]);
					return;
				}
				else if(gameBoard[m[x]+1*x+1*y][m[y]+1*y+1*x]!=0 && gameBoard[m[x]-1*x+2*y][m[y]-1*y+2*x]==0){
					List<List<Integer>> pair = new LinkedList<List<Integer>>();
					List<Integer> cord1 = new LinkedList<Integer>();
					List<Integer> cord2 = new LinkedList<Integer>();
					cord1.add(m[0]+1);
					cord1.add(m[1]);
					pair.add(cord1);
					cord2.add(m[0]+1);
					cord2.add(m[1]-1);
					pair.add(cord2);
					pairs.add(pair);
	
					m[0] = m[0]+2;
					m[1] = m[1]-1;
					
					sendMove(m[x],m[y]);
					return;
				}
			}
			int rand = 2;
			rand*=Math.random();
			
			//Extend left if we haven't gone right
			if(left() && rand==0 && gameBoard[n[x]+1*x-2*y][n[y]+1*y-2*x]==0){
				List<List<Integer>> pair = new LinkedList<List<Integer>>();
				List<Integer> cord1 = new LinkedList<Integer>();
				List<Integer> cord2 = new LinkedList<Integer>();
				cord1.add(n[0]-1);
				cord1.add(n[1]);
				pair.add(cord1);
				cord2.add(n[0]-1);
				cord2.add(n[1]+1);
				pair.add(cord2);
				pairs.add(pair);
	
				n[0] = n[0]-2;
				n[1] = n[1]+1;
				
				sendMove(n[x],n[y]);
				return;
			}
			else if(left() && rand==1 && gameBoard[n[x]-1*x-1*y][n[y]-1*y-1*x]==0){
				List<List<Integer>> pair = new LinkedList<List<Integer>>();
				List<Integer> cord1 = new LinkedList<Integer>();
				List<Integer> cord2 = new LinkedList<Integer>();
				cord1.add(n[0]);
				cord1.add(n[1]-1);
				pair.add(cord1);
				cord2.add(n[0]-1);
				cord2.add(n[1]);
				pair.add(cord2);
				pairs.add(pair);
	
				n[0] = n[0]-1;
				n[1] = n[1]-1;
				
				sendMove(n[x],n[y]);
				return;
			}
			//Extend right if we haven't gone left
			if(right() && rand==0 && gameBoard[m[x]-1*x+2*y][m[y]-1*y+2*x]==0){
				List<List<Integer>> pair = new LinkedList<List<Integer>>();
				List<Integer> cord1 = new LinkedList<Integer>();
				List<Integer> cord2 = new LinkedList<Integer>();
				cord1.add(m[0]+1);
				cord1.add(m[1]);
				pair.add(cord1);
				cord2.add(m[0]+1);
				cord2.add(m[1]-1);
				pair.add(cord2);
				pairs.add(pair);
	
				m[0] = m[0]+2;
				m[1] = m[1]-1;
				
				sendMove(m[x],m[y]);
				return;
			}
			else if(right() && rand==1 && gameBoard[m[x]+1*x+1*y][m[y]+1*y+1*x]==0){
				List<List<Integer>> pair = new LinkedList<List<Integer>>();
				List<Integer> cord1 = new LinkedList<Integer>();
				List<Integer> cord2 = new LinkedList<Integer>();
				cord1.add(m[0]+1);
				cord1.add(m[1]);
				pair.add(cord1);
				cord2.add(m[0]);
				cord2.add(m[1]+1);
				pair.add(cord2);
				pairs.add(pair);
	
				m[0] = m[0]+1;
				m[1] = m[1]+1;
				
				sendMove(m[x],m[y]);
				return;
			}
			
			//Fill in the pairs after we've reached both sides of the map
			if( !left() && !right() && pairs.size() > 0){
				//Play a random pair
				sendMove(pairs.get(0).get(1).get(x),pairs.get(0).get(1).get(y));
				pairs.remove(0);
				
				return;
			}
		}
		catch(Exception e){}
		
		//Pick randomly
		int moves=0;
		for(int a=0; a<gameBoard.length; a++){
			for(int b=0; b<gameBoard[a].length; b++){
				if(gameBoard[a][b]==0) moves++;
			}
		}
		moves*=Math.random();
		moves++;
		for(int a=0; a<gameBoard.length; a++){
			for(int b=0; b<gameBoard[a].length; b++){
				if(gameBoard[a][b]==0) {
					moves--;
				}
				if(moves==0) {
					sendMove(a,b);
					moves=-10;
				}	
			}
		}
		
		return;
	}
	
	private void sendMove(int x, int y){
		if(!skipMove) GameAction.makeMove(this, (byte) team, new Point(x,y), game);
		else skipMove = false;
	}

	@Override
	public void newgameCalled() {
		skipMove = true;
	}

	@Override
	public boolean supportsUndo() {
		if(team==1){
			return game.player2 instanceof PlayerObject;
		}
		else{
			return game.player1 instanceof PlayerObject;
		}
	}

	@Override
	public boolean supportsNewgame() {
		return true;
	}

	@Override
	public void quit() {
		skipMove = true;
	}

	@Override
	public void win() {
	}

	@Override
	public void lose() {
	}

	@Override
	public boolean supportsSave() {
//		if(team==(byte)1){
//			return Global.player2 instanceof PlayerObject;
//		}
//		else{
//			return Global.player1 instanceof PlayerObject;
//		}
		return false;
	}

	@Override
	public void endMove() {
		skipMove = true;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public void setTime(long time) {
		this.timeLeft = time;
	}

	@Override
	public long getTime() {
		return timeLeft;
	}

	@Override
	public void setMove(Object o, Point hex) {
	}

	@Override
	public boolean giveUp() {
		return false;
	}
	
	/*  Bah, ignore this for now.
	private void nickMove(){
		int[][] moves=new int[gameBoard.length][gameBoard.length];
		 
		 for(int x=0; x<gameBoard.length; x++){
			for(int y=0; y<gameBoard[x].length; y++){
				if(gameBoard[x][y]!=0){
					moves[x][y]=-10000;
				}
				else{
					//Actually calculate value
				}
				if(gameBoard[x][y]!=0 && gameBoard[x][y]!=team){
					
				}
				if(gameBoard[x][y]==team){
					//Look through all pieces and find distance from my piece
					for(int i=0; i<gameBoard.length; i++)
						for(int j=0; j<gameBoard[i].length; j++){
							if(gameBoard[i][j]==0){
								//TODO: calculate distance
								if(team==1){
									
								}
								if(team==2){
									
								}
							}
							
						}
				}
			}
		}
		 
		 int max=moves[0][0];
		 ArrayList<RegularPolygonGameObject> possible=new ArrayList<RegularPolygonGameObject>();
		 possible.add(Global.gamePiece[0][0]);
		 for(int x=0; x<gameBoard.length; x++){
				for(int y=0; y<gameBoard[x].length; y++){
					if (moves[x][y]==max){
						possible.add(Global.gamePiece[x][y]);
					}
					else if(moves[x][y]>max){
						 max=moves[x][y];
						 possible=new ArrayList<RegularPolygonGameObject>();
						 possible.add(Global.gamePiece[x][y]);
					}
				}
			}
		 int move=(int)(possible.size()*Math.random());
		 possible.get(move).setTeam(team);
	} */ 
}