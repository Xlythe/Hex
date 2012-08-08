package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sam.hex.GameAction;
import com.sam.hex.GameObject;
import com.sam.hex.Global;
import com.sam.hex.HexGame;
import com.sam.hex.Preferences;
import com.sam.hex.R;
import com.sam.hex.BoardView;
import com.sam.hex.Timer;
import com.sam.hex.net.NetGlobal;
import com.sam.hex.net.igGC.ParsedDataset;
import com.sam.hex.net.igGC.igGameCenter;
import com.sam.hex.replay.Save;

/**
 * @author Will Harmon
 **/
public class NetHexGame extends Activity {
	public static boolean startNewGame = true;
	public static boolean justStart = false;
	private Runnable startnewgame = new Runnable(){
		public void run(){
			HexGame.stopGame(NetGlobal.game);
			startActivity(new Intent(getBaseContext(),WaitingRoomActivity.class));
			finish();
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(NetHexGame.startNewGame){
        	initializeNewGame();//Must be set up immediately
        }
        else{
        	applyBoard();
        }
    }
    
    public void applyBoard(){
    	Global.viewLocation = NetGlobal.GAME_LOCATION;
    	NetGlobal.game.views.board=new BoardView(this);
    	setContentView(R.layout.game_net);
        GamePagerAdapter gameAdapter = new GamePagerAdapter(NetGlobal.game);
    	ViewPager gamePager = (ViewPager) findViewById(R.id.board);
        gamePager.setAdapter(gameAdapter);
    	
    	Button home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
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
            	        	justStart = true;
            	        	NetGlobal.game.player1.supportsNewgame();
            	        	NetGlobal.game.player2.supportsNewgame();
            	            break;
            	        case DialogInterface.BUTTON_NEGATIVE:
            	            //No button clicked
            	        	//Do nothing
            	            break;
            	        }
            	    }
            	};

            	AlertDialog.Builder builder = new AlertDialog.Builder(NetHexGame.this);
            	builder.setMessage(NetHexGame.this.getString(R.string.confirmNewgame)).setPositiveButton(NetHexGame.this.getString(R.string.yes), dialogClickListener).setNegativeButton(NetHexGame.this.getString(R.string.no), dialogClickListener).show();
            }
        });
        
        Button quit = (Button) findViewById(R.id.quit);
        quit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	quit();
            }
        });
        
        NetGlobal.game.views.player1Icon = (ImageButton) this.findViewById(R.id.p1);
        NetGlobal.game.views.player2Icon = (ImageButton) this.findViewById(R.id.p2);
        
        NetGlobal.game.views.timerText = (TextView) this.findViewById(R.id.timer);
        if(NetGlobal.game.timer.type==0 || NetGlobal.game.gameOver){
        	NetGlobal.game.views.timerText.setVisibility(View.GONE);
        } 
        NetGlobal.game.views.winnerText = (TextView) this.findViewById(R.id.winner);
        if(NetGlobal.game.gameOver) NetGlobal.game.views.winnerText.setText(NetGlobal.game.winnerMsg);
        NetGlobal.game.views.handler = new Handler();

        NetGlobal.game.views.replayForward = (ImageButton) this.findViewById(R.id.replayForward);
        NetGlobal.game.views.replayPlayPause = (ImageButton) this.findViewById(R.id.replayPlayPause);
        NetGlobal.game.views.replayBack = (ImageButton) this.findViewById(R.id.replayBack);
        NetGlobal.game.views.replayButtons = (RelativeLayout) this.findViewById(R.id.replayButtons);
	    
	    new Thread(new Runnable(){
        	public void run(){
        		while(!NetGlobal.game.gameOver){
        			NetGlobal.game.views.handler.post(new Runnable(){
        	    		public void run(){
        	    			if(GameAction.getPlayer(NetGlobal.game.currentPlayer, NetGlobal.game) instanceof NetPlayerObject 
        	    					&& !(GameAction.getPlayer(NetGlobal.game.currentPlayer%2+1, NetGlobal.game) instanceof NetPlayerObject))
        	    				for(int i=0;i<NetGlobal.members.size();i++){
        	    					if(NetGlobal.members.get(i).place==NetGlobal.game.currentPlayer){
        	    						if(NetGlobal.members.get(i).lastRefresh>300){
	        	    						NetGlobal.game.views.handler.post(new Runnable(){
	        	    							public void run(){
	        	    								NetGlobal.game.views.timerText.setVisibility(View.GONE);
	        	    							}
	        	    						});
	        	    						final Handler buttonHandler = new Handler();
	        	    						final Button button = (Button) NetHexGame.this.findViewById(R.id.claimVictory);
	        	    						button.setOnClickListener(new OnClickListener() {
												@Override
												public void onClick(View v) {
													new Thread(new Runnable(){
											    		public void run(){
											    			try {
											    	            ParsedDataset parsedDataset = igGameCenter.claimVictory(NetGlobal.server, NetGlobal.uid, NetGlobal.session_id, NetGlobal.sid, NetGlobal.lasteid);
											    	        	if(!parsedDataset.error){
											    	        		buttonHandler.post(new Runnable() {
																		@Override
																		public void run() {
																			button.setVisibility(View.GONE);
																		}
																	});
											    	        		NetGlobal.game.timer.stop();
											    	        		NetGlobal.game.timer = new Timer(NetGlobal.game, 0, 0, Timer.ENTIRE_MATCH);
											    	        		NetGlobal.game.timer.start();
											    	        	}
											    	        	else{
											    	        		System.out.println(parsedDataset.getErrorMessage());
											    	        	}
											    			} catch (MalformedURLException e) {
											    				e.printStackTrace();
											    			} catch (ParserConfigurationException e) {
											    				e.printStackTrace();
											    			} catch (SAXException e) {
											    				e.printStackTrace();
											    			} catch (IOException e) {
											    				e.printStackTrace();
											    			}
											    		}
											    	}).start();
												}
											});
	        	    						button.setVisibility(View.VISIBLE);
        	    						}
        	    						else{
            	    						if(NetGlobal.game.timer.type!=0 || !NetGlobal.game.gameOver){
            	    				        	NetGlobal.game.views.timerText.setVisibility(View.VISIBLE);
            	    				        } 
            	    						Button button = (Button) NetHexGame.this.findViewById(R.id.claimVictory);
            	    						button.setVisibility(View.GONE);
            	    					}
        	    					}
        	    				}
        	    		}});
        			try {
        				Thread.sleep(8000);
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        			}
        		}
        	}
        }).start();
    }
    
    private void initializeNewGame(){
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	startNewGame = false;
    	
    	//Stop the old game
    	HexGame.stopGame(NetGlobal.game);
    	
    	//Create a new game object
    	NetGlobal.game = new GameObject(HexGame.setGrid(prefs, NetGlobal.GAME_LOCATION), true); 
    	
    	//Set players
    	HexGame.setType(prefs, NetGlobal.GAME_LOCATION, NetGlobal.game);
    	HexGame.setPlayer1(NetGlobal.game, startnewgame);
    	HexGame.setPlayer2(NetGlobal.game, startnewgame);
    	HexGame.setNames(prefs, NetGlobal.GAME_LOCATION, NetGlobal.game);
    	HexGame.setColors(prefs, NetGlobal.GAME_LOCATION, NetGlobal.game);
    	if(NetGlobal.timerTime==0){
    	    NetGlobal.game.timer = new Timer(NetGlobal.game, 0, 0, Timer.NO_TIMER);
    	}
    	else{
    	    NetGlobal.game.timer = new Timer(NetGlobal.game, NetGlobal.timerTime, 0, Timer.ENTIRE_MATCH);
    	}
    	
	    //Display board
	    applyBoard();
    	
        //Start the game object
	    NetGlobal.game.start();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	//Check if settings were changed and we need to run a new game
    	if(startNewGame || HexGame.somethingChanged(prefs, NetGlobal.GAME_LOCATION, NetGlobal.game)){
    		initializeNewGame();
    	}
    	else{//Apply minor changes without stopping the current game
    		HexGame.setColors(prefs, NetGlobal.GAME_LOCATION, NetGlobal.game);
    		HexGame.setNames(prefs, NetGlobal.GAME_LOCATION, NetGlobal.game);
    		NetGlobal.game.moveList.replay(0,NetGlobal.game);
    		GameAction.checkedFlagReset(NetGlobal.game);
    		GameAction.checkWinPlayer(1,NetGlobal.game);
    		GameAction.checkWinPlayer(2,NetGlobal.game);
    		GameAction.checkedFlagReset(NetGlobal.game);
	    	
	    	//Apply everything
	    	NetGlobal.game.views.board.invalidate();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu_net, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.settings:
        	startActivity(new Intent(getBaseContext(),Preferences.class));
            return true;
        case R.id.saveReplay:
        	Save save = new Save(NetGlobal.game);
        	save.showSavingDialog();
        	return true;
        case R.id.quit:
        	quit();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void quit(){
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	HexGame.stopGame(NetGlobal.game);
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
    	builder.setMessage(this.getString(R.string.confirmExit)).setPositiveButton(this.getString(R.string.yes), dialogClickListener).setNegativeButton(this.getString(R.string.no), dialogClickListener).show();
    }
    
    private class GamePagerAdapter extends PagerAdapter{
    	private GameObject game;
    	private Boolean inWaitingRoom;
    	public GamePagerAdapter(GameObject game){
    		this.game = game;
    		this.inWaitingRoom = false;
    	}
    	
        @Override
        public int getCount() {
            return 2;
        }

	    /**
	     * Create the page for the given position.  The adapter is responsible
	     * for adding the view to the container given here, although it only
	     * must ensure this is done by the time it returns from
	     * {@link #finishUpdate()}.
	     *
	     * @param container The containing View in which the page will be shown.
	     * @param position The page position to be instantiated.
	     * @return Returns an Object representing the new page.  This does not
	     * need to be a View, but can be some other container of the page.
	     */
        @Override
        public Object instantiateItem(View collection, int position) {
            if(position==0){
            	((ViewPager) collection).addView(game.views.board,0);
                
                return game.views.board;
            }
            if(position==1){
            	this.inWaitingRoom = true;
            	LayoutInflater inflater = (LayoutInflater) NetHexGame.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
            	final View waitingRoom = inflater.inflate(R.layout.waitingroom_body, null);
            	
            	Button submit = (Button) waitingRoom.findViewById(R.id.submit);
                submit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                    	WaitingRoomActivity.sendMessage(waitingRoom, (EditText) waitingRoom.findViewById(R.id.sendMessage));
                    }
                });
                
                EditText text = (EditText) waitingRoom.findViewById(R.id.sendMessage);
                text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        				if(actionId==EditorInfo.IME_ACTION_DONE || event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
        					WaitingRoomActivity.sendMessage(waitingRoom, v);
        					return true;
        				}
        				return false;
        			}
        		});
                final Handler handler = new Handler();
                
                new Thread(new Runnable(){
                	public void run(){
                		while(inWaitingRoom){
                			handler.post(new Runnable(){
                	    		public void run(){
                	    			WaitingRoomActivity.refreshPlayers(waitingRoom, NetHexGame.this);
                	    			WaitingRoomActivity.refreshMessages(waitingRoom);
                	    			game.views.player1Icon.setColorFilter(game.player1.getColor());
                	    			game.views.player2Icon.setColorFilter(game.player2.getColor());
                	    			if(game.currentPlayer==1 && !game.gameOver){
                	    				game.views.player1Icon.setAlpha(255);
                	    				game.views.player2Icon.setAlpha(80);
                	    			}
                	    			else if(game.currentPlayer==2 && !game.gameOver){
                	    				game.views.player1Icon.setAlpha(80);
                	    				game.views.player2Icon.setAlpha(255);
                	    			}
                	    			else{
                	    				game.views.player1Icon.setAlpha(80);
                	    				game.views.player2Icon.setAlpha(80);
                	    			}
                	    		}});
                			try {
	            				Thread.sleep(5000);
	            			} catch (InterruptedException e) {
	            				e.printStackTrace();
	            			}
                		}
                	}
                }).start();
                
            	((ViewPager) collection).addView(waitingRoom,0);
                
                return waitingRoom;
            }
            
            return game.views.board;
        }
        
	    /**
	     * Remove a page for the given position.  The adapter is responsible
	     * for removing the view from its container, although it only must ensure
	     * this is done by the time it returns from {@link #finishUpdate()}.
	     *
	     * @param container The containing View from which the page will be removed.
	     * @param position The page position to be removed.
	     * @param object The same object that was returned by
	     * {@link #instantiateItem(View, int)}.
	     */
        @Override
        public void destroyItem(View collection, int position, Object view) {
        	if(position==1) this.inWaitingRoom = false;
            ((ViewPager) collection).removeView((View) view);
        }

        
        
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==((View)object);
        }

        
	    /**
	     * Called when the a change in the shown pages has been completed.  At this
	     * point you must ensure that all of the pages have actually been added or
	     * removed from the container as appropriate.
	     * @param container The containing View which is displaying this adapter's
	     * page views.
	     */
        @Override
        public void finishUpdate(View arg0) {}
        

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {}

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {}
    }
}