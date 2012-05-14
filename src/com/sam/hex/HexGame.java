package com.sam.hex;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Point;

import com.sam.hex.ai.bee.BeeGameAI;
import com.sam.hex.ai.will.GameAI;
import com.sam.hex.lan.LANGlobal;
import com.sam.hex.lan.LocalPlayerObject;
import com.sam.hex.net.NetGlobal;
import com.sam.hex.net.NetPlayerObject;
import com.sam.hex.replay.FileExplore;
import com.sam.hex.replay.Load;
import com.sam.hex.replay.Replay;
import com.sam.hex.replay.Save;

public class HexGame extends Activity {
	public static boolean startNewGame = true;
	public static boolean replay = false;
	public static boolean replayRunning = false;
	private static Intent intent;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(HexGame.startNewGame){
        	initializeNewGame();//Must be set up immediately
        }
        applyBoard();
        
        if(intent!=getIntent()){
	        intent = getIntent();
	        if (intent.getData() != null) {
	        	Thread loading = new Thread(new ThreadGroup("Load"), new Load(new File(intent.getData().getPath())), "loading", 200000);
	        	loading.start();
				try {
					loading.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
        }
    }
    
    private void applyBoard(){
    	Global.viewLocation = Global.GAME_LOCATION;
    	setContentView(R.layout.game);
    	Global.game.board=(BoardView) findViewById(R.id.board);
    	Global.game.board.setOnTouchListener(new TouchListener(Global.game));
    	
    	Button home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        
        Button undo = (Button) findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	undo();
            }
        });
        
        Button newgame = (Button) findViewById(R.id.newgame);
        newgame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            	    public void onClick(DialogInterface dialog, int which) {
            	        switch (which){
            	        case DialogInterface.BUTTON_POSITIVE:
            	            //Yes button clicked
            	        	newGame();
            	            break;
            	        case DialogInterface.BUTTON_NEGATIVE:
            	            //No button clicked
            	        	//Do nothing
            	            break;
            	        }
            	    }
            	};

            	AlertDialog.Builder builder = new AlertDialog.Builder(HexGame.this);
            	builder.setMessage(HexGame.this.getString(R.string.confirmNewgame)).setPositiveButton(HexGame.this.getString(R.string.yes), dialogClickListener).setNegativeButton(HexGame.this.getString(R.string.no), dialogClickListener).show();
            }
        });
        
        Button quit = (Button) findViewById(R.id.quit);
        quit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	quit();
            }
        });
        
        Global.game.player1Icon = (ImageButton) findViewById(R.id.p1);
        Global.game.player2Icon = (ImageButton) findViewById(R.id.p2);
        
        Global.game.timerText = (TextView) findViewById(R.id.timer);
        if(Global.game.timer.type==0 || Global.game.gameOver){
        	Global.game.timerText.setVisibility(View.GONE);
        } 
        Global.game.winnerText = (TextView) findViewById(R.id.winner);
        if(Global.game.gameOver) Global.game.winnerText.setText(Global.game.winnerMsg);
        Global.game.handler = new Handler();

        Global.game.replayForward = (ImageButton) findViewById(R.id.replayForward);
        Global.game.replayPlayPause = (ImageButton) findViewById(R.id.replayPlayPause);
        Global.game.replayBack = (ImageButton) findViewById(R.id.replayBack);
        Global.game.replayButtons = (RelativeLayout) findViewById(R.id.replayButtons);
    }
    
    public static class TouchListener implements OnTouchListener{
    	GameObject game;
    	public TouchListener(GameObject game){
    		this.game = game;
    	}
    	public boolean onTouch(View v, MotionEvent event){
    		int eventaction = event.getAction();
    		if(eventaction==MotionEvent.ACTION_UP){
    			int x = (int)event.getX();
				int y = (int)event.getY();
				for(int xc = 0; xc < game.gamePiece.length; xc++){
					for(int yc=0; yc<game.gamePiece[0].length; yc++)
						if(game.gamePiece[xc][yc].contains(x, y)){
							if(game!=null)GameAction.setPiece(new Point(xc,yc),game);
							return false;
						}
				}
    		}
    		
			return true;
    	}
    }
    
    private void initializeNewGame(){
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	startNewGame = false;
    	replayRunning = false;
    	
    	//Stop the old game
    	stopGame(Global.game);
    	
    	//Create a new game object
    	Global.game = new GameObject(setGrid(prefs, Global.GAME_LOCATION), prefs.getBoolean("swapPref", true));
    	
    	//Set players
    	setType(prefs, Global.GAME_LOCATION, Global.game);
    	setPlayer1(Global.game, new Runnable(){
			public void run(){
				initializeNewGame();
			}
		});
    	setPlayer2(Global.game, new Runnable(){
			public void run(){
				initializeNewGame();
			}
		});
    	setNames(prefs, Global.GAME_LOCATION, Global.game);
    	setColors(prefs, Global.GAME_LOCATION, Global.game);
    	int timerType = Integer.parseInt(prefs.getString("timerTypePref", "0"));
	    Global.game.timer = new Timer(Global.game, Integer.parseInt(prefs.getString("timerPref", "0")), 0, timerType);
	    
	    applyBoard();
	    Global.game.timer.start();
	    Global.game.start();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	//Check if settings were changed and we need to run a new game
    	 if(replayRunning){
     		//Do nothing
     	}
    	 else if(replay){
    		replay = false;
    		replay(800);
    	}
    	else if(HexGame.startNewGame || somethingChanged(prefs, Global.GAME_LOCATION, Global.game)){
    		initializeNewGame();
    		applyBoard();
    	}
    	else{//Apply minor changes without stopping the current game
    		setColors(prefs, Global.GAME_LOCATION, Global.game);
    		setNames(prefs, Global.GAME_LOCATION, Global.game);
    		Global.game.moveList.replay(0, Global.game);
    		GameAction.checkedFlagReset(Global.game);
    		GameAction.checkWinPlayer(1,Global.game);
    		GameAction.checkWinPlayer(2,Global.game);
    		GameAction.checkedFlagReset(Global.game);
	    	
	    	//Apply everything
	    	Global.game.board.invalidate();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.settings:
        	replayRunning = false;
        	startActivity(new Intent(getBaseContext(),Preferences.class));
            return true;
        case R.id.undo:
        	undo();
            return true;
        case R.id.newgame:
        	newGame();
            return true;
        case R.id.replay:
        	replay(900);
            return true;
        case R.id.loadReplay:
        	startActivity(new Intent(getBaseContext(),FileExplore.class));
            return true;
        case R.id.saveReplay:
        	Save save = new Save(Global.game);
        	save.showSavingDialog();
        	return true;
        case R.id.quit:
        	quit();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	
    	//If the board's empty, just trigger "startNewGame"
    	if(Global.game==null || (Global.game.moveNumber==1 && Global.game.timer.type==0)) HexGame.startNewGame=true;
    }
    
    /**
     * Terminates the game
     * */
    public static void stopGame(GameObject game){
    	if(game!=null){
    		game.stop();
    	}
    }
    
    /**
     * Refreshes both player's names
     * Does not invalidate the board
     * */
    public static void setNames(SharedPreferences prefs, int gameLocation, GameObject game){
    	if(gameLocation==Global.GAME_LOCATION){
    		//Playing on the same phone
    		game.player1.setName(prefs.getString("player1Name", "Player1"));
    		game.player2.setName(prefs.getString("player2Name", "Player2"));
    	}
    	if(gameLocation==LANGlobal.GAME_LOCATION){
    		//Playing over LAN
    		if(LANGlobal.localPlayer.firstMove){
    			game.player1.setName(LANGlobal.localPlayer.playerName);
        		game.player2.setName(prefs.getString("lanPlayerName", "Player"));
    		}
    		else{
    			game.player1.setName(prefs.getString("lanPlayerName", "Player"));
        		game.player2.setName(LANGlobal.localPlayer.playerName);
    		}
    	}
    	else if(gameLocation==NetGlobal.GAME_LOCATION){
    		//Playing over the net
    		for(int i=0;i<NetGlobal.members.size();i++){
    			if(NetGlobal.members.get(i).place==1){
    				game.player1.setName(NetGlobal.members.get(i).name);
    			}
    			else if(NetGlobal.members.get(i).place==2){
    				game.player2.setName(NetGlobal.members.get(i).name);
    			}
    		}
    	}
    }
    
    /**
     * Refreshes both player's colors
     * Does not invalidate the board
     * */
    public static void setColors(SharedPreferences prefs, int gameLocation, GameObject game){
    	if(gameLocation==1){
    		//Playing over LAN
    		if(LANGlobal.localPlayer.firstMove){
    			game.player1.setColor(LANGlobal.localPlayer.playerColor);
        		game.player2.setColor(prefs.getInt("lanPlayerColor", Global.player1DefaultColor));
    		}
    		else{
    			game.player1.setColor(prefs.getInt("lanPlayerColor", Global.player1DefaultColor));
        		game.player2.setColor(LANGlobal.localPlayer.playerColor);
    		}
    	}
    	else if(gameLocation==2){
    		//Playing on the net
    		game.player1.setColor(Global.player1DefaultColor);
    		game.player2.setColor(Global.player2DefaultColor);
    	}
    	else{
    		//Playing on the same phone
    		game.player1.setColor(prefs.getInt("player1Color", Global.player1DefaultColor));
    		game.player2.setColor(prefs.getInt("player2Color", Global.player2DefaultColor));
    	}
    }
    
    public static int setGrid(SharedPreferences prefs, int gameLocation){
    	int gridSize = 0;
    	if(gameLocation==Global.GAME_LOCATION){
    		//Playing on the same phone
    		gridSize=Integer.decode(prefs.getString("gameSizePref", "7"));
    		if(gridSize==0) gridSize=Integer.decode(prefs.getString("customGameSizePref", "7"));
    	}
    	else if(gameLocation==LANGlobal.GAME_LOCATION){
    		//Playing over LAN
    		if(LANGlobal.localPlayer.firstMove){
    			gridSize=LANGlobal.localPlayer.gridSize;
    		}
    		else{
    			gridSize=Integer.decode(prefs.getString("gameSizePref", "7"));
    		}
    	}
    	else if(gameLocation==NetGlobal.GAME_LOCATION){
    		//Playing over the net
    		gridSize = NetGlobal.gridSize;
    	}
    	
    	//We don't want 0x0 games
    	if(gridSize<=0) gridSize=1;
    	
    	return gridSize;
    }
    
    public static void setType(SharedPreferences prefs, int gameLocation, GameObject game){
    	if(gameLocation==Global.GAME_LOCATION){
    		game.player1Type=(byte)Integer.parseInt(prefs.getString("player1Type", "0"));
        	game.player2Type=(byte)Integer.parseInt(prefs.getString("player2Type", "0"));
    	}
    	else if(gameLocation==LANGlobal.GAME_LOCATION){
    		//Playing over LAN
    		if(LANGlobal.localPlayer.firstMove){
    			game.player1Type=(byte)2;
    			game.player2Type=(byte)Integer.parseInt(prefs.getString("lanPlayerType", "0"));
    		}
    		else{
    			game.player1Type=(byte)Integer.parseInt(prefs.getString("lanPlayerType", "0"));
    			game.player2Type=(byte)2;
    		}
    	}
    	else if(gameLocation==NetGlobal.GAME_LOCATION){
    		//Playing over the net
    		for(int i=0;i<NetGlobal.members.size();i++){
    			if(NetGlobal.members.get(i).place==1){
    				if(prefs.getString("netUsername", "").toLowerCase().equals(NetGlobal.members.get(i).name.toLowerCase())){
    					game.player1Type=(byte)0;
    				}
    				else{
    					game.player1Type=(byte)3;
    				}
    			}
    			else if(NetGlobal.members.get(i).place==2){
    				if(prefs.getString("netUsername", "").toLowerCase().equals(NetGlobal.members.get(i).name.toLowerCase())){
    					game.player2Type=(byte)0;
    				}
    				else{
    					game.player2Type=(byte)3;
    				}
    			}
    		}
    	}
    }
    
    public static void setPlayer1(GameObject game, Runnable newgame){
    	if(game.player1Type==0) game.player1=new PlayerObject(1,game);
		else if(game.player1Type==1) game.player1=new GameAI(1,game);
		else if(game.player1Type==2) game.player1=new LocalPlayerObject(1,game);
		else if(game.player1Type==3) game.player1=new NetPlayerObject(1, game, new Handler(), newgame);
		else if(game.player1Type==4) game.player1=new BeeGameAI(1,game);
    }
    
    public static void setPlayer2(GameObject game, Runnable newgame){
		if(game.player2Type==0) game.player2=new PlayerObject(2,game);
		else if(game.player2Type==1) game.player2=new GameAI(2,game);
		else if(game.player2Type==2) game.player2=new LocalPlayerObject(2,game);
		else if(game.player2Type==3) game.player2=new NetPlayerObject(2, game, new Handler(), newgame);
		else if(game.player2Type==4) game.player2=new BeeGameAI(2,game);
    }
    
    private void undo(){
    	GameAction.undo(Global.GAME_LOCATION,Global.game);
    }
    
    private void newGame(){
    	if(Global.game.player1.supportsNewgame() && Global.game.player2.supportsNewgame()){
			replayRunning = false;
			initializeNewGame();
			applyBoard();
    	}
    }
    
    /**
     * Returns true if a major setting was changed
     * */
    public static boolean somethingChanged(SharedPreferences prefs, int gameLocation, GameObject game){
    	if(game==null) return true;
    	if(game.gridSize==1) return true;
    	if(gameLocation==Global.GAME_LOCATION){
    		return (Integer.decode(prefs.getString("gameSizePref", "7")) != game.gridSize && Integer.decode(prefs.getString("gameSizePref", "7")) != 0) 
    				|| (Integer.decode(prefs.getString("customGameSizePref", "7")) != game.gridSize && Integer.decode(prefs.getString("gameSizePref", "7")) == 0)
    				|| Integer.decode(prefs.getString("player1Type", "0")) != (int) game.player1Type 
    	    		|| Integer.decode(prefs.getString("player2Type", "0")) != (int) game.player2Type 
    	    	    || Integer.decode(prefs.getString("timerTypePref", "0")) != game.timer.type
    	    	    || Integer.decode(prefs.getString("timerPref", "0"))*60*1000 != game.timer.totalTime;
    	}
    	if(gameLocation==LANGlobal.GAME_LOCATION){
    		return !(Integer.decode(prefs.getString("lanPlayerType", "0")) == (int) game.player1Type || Integer.decode(prefs.getString("lanPlayerType", "0")) == (int) game.player2Type);
    	}
    	else if(gameLocation==NetGlobal.GAME_LOCATION){
    		return (game!=null && game.gameOver);
    	}
    	else{
    		return true;
    	}
    }
    
    private Thread replayThread;
    private void replay(int time){
    	//Create our board
    	applyBoard();
    	Global.game.clearBoard();
        
    	if(Global.game.moveNumber>1) Global.game.currentPlayer=(Global.game.currentPlayer%2)+1;
	    
    	replayRunning = true;
		replayThread = new Thread(new Replay(time, new Handler(), new Runnable(){
			public void run(){
				Global.game.timerText.setVisibility(View.GONE);
				Global.game.winnerText.setVisibility(View.GONE);
//				Global.replayButtons.setVisibility(View.VISIBLE);
			}
		}, new Runnable(){
			public void run(){
				if(Global.game.timer.type!=0) Global.game.timerText.setVisibility(View.VISIBLE);
//				Global.replayButtons.setVisibility(View.GONE);
			}
		},Global.game, Global.GAME_LOCATION), "replay");
		replayThread.start();
    }
    
    private void quit(){
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	stopGame(Global.game);
    	        	startNewGame = true;
    	        	finish();
    	            break;
    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked
    	        	//Do nothing
    	            break;
    	        }
    	    }
    	};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(getString(R.string.confirmExit)).setPositiveButton(getString(R.string.yes), dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }
}