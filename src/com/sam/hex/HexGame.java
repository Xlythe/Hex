package com.sam.hex;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.graphics.Point;
import java.util.ArrayList;

public class HexGame extends Activity {
	GameObject game;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(Global.gamePiece[0][0]==null){
        	initializeNewGame();//Must be set up immediately
        }
        else{
        	//Add the touch listener
        	if(game!=null)
        		game.stop();
        	BoardTools.clearBoard();
        	Global.board=new BoardView(this);
        	game=new GameObject();
            Global.board.setOnTouchListener(new TouchListener());
            setContentView(Global.board);
        }
    }
    class TouchListener implements OnTouchListener{
    	public boolean onTouch(View v, MotionEvent event){
    		int x = (int)event.getX();
			int y = (int)event.getY();
			for (int xc = 0; xc < Global.gamePiece.length; xc++) {
				for (int yc=0; yc<Global.gamePiece[0].length; yc++)
					if (Global.gamePiece[xc][yc].contains(x, y)) {
						if(game!=null)game.setPiece(new Point(xc,yc));
						//Return false. We got our point. (True is used for gestures)
						return false;
					}
			}
			//Return false. We got our point. (True is used for gestures)
			return false;
    	}
    }
    
    public void initializeNewGame(){
    	//Stop the old game
    	if(game!=null)
    		game.stop();
    	
    	//Load preferences
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	Global.gamePrefs=prefs;
    	
    	//Set player names
    	Global.playerOneName = prefs.getString("player1Name", "Player1");
    	Global.playerTwoName = prefs.getString("player2Name", "Player2");
    	
    	//Set player colors
    	Global.playerOneColor = prefs.getInt("player1Color", Global.playerOneDefaultColor);
    	Global.playerTwoColor = prefs.getInt("player2Color", Global.playerTwoDefaultColor);

    	//Create our board
    	Global.gridSize=Integer.decode(prefs.getString("gameSizePref", "7"));
    	Global.difficulty=Integer.decode(prefs.getString("aiPref", "1"));
    	Global.gamePiece=new RegularPolygonGameObject[Global.gridSize][Global.gridSize];
    	BoardTools.clearBoard(); 
    	Global.board=new BoardView(this);
    	
    	//Make sure the board is empty and defaults are set
    	Global.moveList=new ArrayList<Point>();
    	BoardTools.setBoard();
    	
    	//Set game mode
    	Global.gameType=(byte)Integer.parseInt(prefs.getString("gameModePref", "0"));
		
		//Add the touch listener
        Global.board.setOnTouchListener(new TouchListener());
        setContentView(Global.board);
        
        //Create the game object
        game = new GameObject();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//Load preferences
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	//Check if settings were changed and we need to run a new game
    	if(Integer.decode(prefs.getString("gameModePref", "0")) != (int) Global.gameType && Integer.decode(prefs.getString("gameModePref", "0")) == 4){
    		//Go to the local lobby
    		Global.gameType = 4;
        	startActivity(new Intent(getBaseContext(),LocalLobbyActivity.class));
        	finish();
    	}
    	else if(Integer.decode(prefs.getString("aiPref", "1")) != Global.difficulty || Integer.decode(prefs.getString("gameSizePref", "7")) != Global.gridSize || Integer.decode(prefs.getString("gameModePref", "0")) != (int) Global.gameType){
    		//Reset the game
    		initializeNewGame();
    	}
    	else
    	{	
    		//Apply minor changes without stopping the current game
    		Global.playerOneColor = prefs.getInt("player1Color", Global.playerOneDefaultColor);
	    	Global.playerTwoColor = prefs.getInt("player2Color", Global.playerTwoDefaultColor);
	    	Global.playerOneName = prefs.getString("player1Name", "Player1");
	    	Global.playerTwoName = prefs.getString("player2Name", "Player2");
	    	Global.board.onSizeChanged(Global.windowWidth,Global.windowHeight,0,0);
	    	Global.board.invalidate();
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
        	startActivity(new Intent(getBaseContext(),Preferences.class));
            return true;
        case R.id.undo:
        	if(game!=null)
        		game.stop();
        	BoardTools.clearBoard();
        	BoardTools.undo();
        	if(Global.gameType!=0)
        		BoardTools.undo();
        	game= new GameObject();
            return true;
        case R.id.newgame:
        	initializeNewGame();
        	Global.board.invalidate();
            return true;
        case R.id.quit:
        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int which) {
        	        switch (which){
        	        case DialogInterface.BUTTON_POSITIVE:
        	            //Yes button clicked
        	        	android.os.Process.killProcess(android.os.Process.myPid());
        	            break;
        	        case DialogInterface.BUTTON_NEGATIVE:
        	            //No button clicked
        	        	//Do nothing
        	            break;
        	        }
        	    }
        	};

        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Are you sure you want to exit?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
