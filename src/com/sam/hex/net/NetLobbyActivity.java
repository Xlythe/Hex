package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sam.hex.DialogBox;
import com.sam.hex.Preferences;
import com.sam.hex.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;

/**
 * @author Will Harmon
 **/
public class NetLobbyActivity extends Activity {
	private boolean loginSucceeded = false;
	RefreshPlayerlist refreshPlayers;
	final Handler handler = new Handler();
	final Runnable updateResults = new Runnable() {
        public void run() {
        	updateResultsInUi();
        }
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netlobby);
        
        Button home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        
        Button createBoard = (Button) findViewById(R.id.createBoard);
        createBoard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(loginSucceeded){
            		createBoard();
            	}
            }
        });
        
        NetGlobal.android_id = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	loginSucceeded = false;
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("netUsername", "").equals("") || prefs.getString("netPassword", "").equals("")){
        	startActivity(new Intent(getBaseContext(),LoginActivity.class));
        	finish();
        }
        else{
        	if(!isOnline()){
        		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            	    public void onClick(DialogInterface dialog, int which) {
            	        switch (which){
            	        case DialogInterface.BUTTON_POSITIVE:
            	            //Yes button clicked
            	        	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            	            break;
            	        case DialogInterface.BUTTON_NEGATIVE:
            	            //No button clicked
            	        	finish();
            	            break;
            	        }
            	    }
            	};

            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setMessage(getApplicationContext().getString(R.string.cantConnect)).setPositiveButton(getApplicationContext().getString(R.string.yes), dialogClickListener).setNegativeButton(getApplicationContext().getString(R.string.no), dialogClickListener).show();
        	}
        	new Thread(new Runnable(){
    			@Override
    			public void run() {
    				try {
                		String loginUrl = String.format("http://www.iggamecenter.com/api_login.php?app_id=%s&app_code=%s&login=%s&password=%s&networkuid=%s&md5=1", NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), URLEncoder.encode(prefs.getString("netUsername", ""),"UTF-8"), URLEncoder.encode(prefs.getString("netPassword", ""),"UTF-8"), URLEncoder.encode(NetGlobal.android_id,"UTF-8"));
    					URL url = new URL(loginUrl);
    					SAXParserFactory spf = SAXParserFactory.newInstance();
    	                SAXParser parser = spf.newSAXParser();
    	                XMLReader reader = parser.getXMLReader();
    	                XMLHandler xmlHandler = new XMLHandler();
    	                reader.setContentHandler(xmlHandler);
    	                reader.parse(new InputSource(url.openStream()));
    	                
    	                ParsedDataset parsedDataset = xmlHandler.getParsedData();
    	                if(!parsedDataset.error){
    	                	NetGlobal.uid = parsedDataset.getUid();
    	                	NetGlobal.session_id = parsedDataset.getSession_id();
    	        	    	refreshPlayers = new RefreshPlayerlist(handler, updateResults);
    	                	loginSucceeded = true;
    	                }
    	                else{
    	                	System.out.println(parsedDataset.getErrorMessage());
    	                	new DialogBox(NetLobbyActivity.this, NetLobbyActivity.this.getString(R.string.loginFailed), new DialogInterface.OnClickListener() {
    	                	    public void onClick(DialogInterface dialog, int which) {
    	                	        switch (which){
    	                	        case DialogInterface.BUTTON_POSITIVE:
    	                	            //Register button clicked
    	                	        	startActivity(new Intent(getBaseContext(),RegistrationActivity.class));
    	                	        	finish();
    	                	            break;
    	                	        case DialogInterface.BUTTON_NEGATIVE:
    	                	            //Cancel button clicked
    	                	        	android.os.Process.killProcess(android.os.Process.myPid());
    	                	            break;
    	                	        case DialogInterface.BUTTON_NEUTRAL:
    	                	        	//Login button clicked
    	                	        	startActivity(new Intent(getBaseContext(),LoginActivity.class));
    	                	        	finish();
    	                	        	break;
    	                	        }
    	                	    }
    	                	}, NetLobbyActivity.this.getString(R.string.register), NetLobbyActivity.this.getString(R.string.login), NetLobbyActivity.this.getString(R.string.cancel),false);
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
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	
    	if(loginSucceeded){
    		if(refreshPlayers!=null) refreshPlayers.stop();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.locallobby_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.settings:
        	startActivity(new Intent(getBaseContext(),Preferences.class));
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
        	builder.setMessage(this.getString(R.string.confirmExit)).setPositiveButton(this.getString(R.string.yes), dialogClickListener).setNegativeButton(this.getString(R.string.no), dialogClickListener).show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void updateResultsInUi(){
    	final ListView lobby = (ListView) findViewById(R.id.players);
        ArrayAdapter<ParsedDataset.GameSession> adapter = new ArrayAdapter<ParsedDataset.GameSession>(this,R.layout.simple_list_item_1, R.id.text1, NetGlobal.sessions);
        lobby.setAdapter(adapter);
        
        lobby.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
	        	new Thread(new Runnable(){
	        		@Override
	        		public void run() {
    	        		try {
    	        			NetGlobal.server = NetGlobal.sessions.get(position).server;
    	        			NetGlobal.sid = NetGlobal.sessions.get(position).sid;
	    	        		String registrationUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s", URLEncoder.encode(NetGlobal.server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), NetGlobal.uid, URLEncoder.encode(NetGlobal.session_id,"UTF-8"), NetGlobal.sid);
	    	        		URL url = new URL(registrationUrl);
	    	        		SAXParserFactory spf = SAXParserFactory.newInstance();
	    	        		SAXParser parser = spf.newSAXParser();
	    	        		XMLReader reader = parser.getXMLReader();
	    	        		XMLHandler xmlHandler = new XMLHandler();
	    	        		reader.setContentHandler(xmlHandler);
	    	        		reader.parse(new InputSource(url.openStream()));
	
	    	        		ParsedDataset parsedDataset = xmlHandler.getParsedData();
	    	        		if(!parsedDataset.error){
	    	        			NetGlobal.members = NetGlobal.sessions.get(position).members;
	    	        			WaitingRoomActivity.messages = new LinkedList<String>();
		    	        		startActivity(new Intent(getBaseContext(),WaitingRoomActivity.class));
		    	        		finish();
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
    	        		} catch (IndexOutOfBoundsException e){
    	        		e.printStackTrace();
    	        		}
	        		}}).start();
			}
        });
    }
    
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        boolean connected = false;
        try{
        	connected = cm.getActiveNetworkInfo().isConnected();
        }catch(NullPointerException e){
        	e.printStackTrace();
        }
        return connected;
    }
    
    private void createBoard(){
    	final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(NetLobbyActivity.this);
    	LayoutInflater inflater = (LayoutInflater) NetLobbyActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
    	View dialoglayout = inflater.inflate(R.layout.netlobby_createboard, null);
    	
    	//Board size
    	final Spinner gameSize = (Spinner)dialoglayout.findViewById(R.id.gameSize);
        ArrayAdapter<CharSequence> gameSizeAdapter = ArrayAdapter.createFromResource(this, R.array.netGameSizeArray, android.R.layout.simple_spinner_item);
        gameSizeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        gameSize.setAdapter(gameSizeAdapter);
        gameSize.setSelection(Integer.parseInt(settings.getString("netGridSize", "0")));
        
        //Position
        final Spinner position = (Spinner)dialoglayout.findViewById(R.id.position);
        ArrayAdapter<CharSequence> positionAdapter = ArrayAdapter.createFromResource(this, R.array.netPositionArray, android.R.layout.simple_spinner_item);
        positionAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        position.setAdapter(positionAdapter);
        position.setSelection(Integer.parseInt(settings.getString("netPosition", "0")));
        
        //Timer time
        final Spinner timerTime = (Spinner)dialoglayout.findViewById(R.id.timerTime);
        ArrayAdapter<CharSequence> timerTimeAdapter = ArrayAdapter.createFromResource(this, R.array.netTimerArray, android.R.layout.simple_spinner_item);
        timerTimeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        timerTime.setAdapter(timerTimeAdapter);
        timerTime.setSelection(Integer.parseInt(settings.getString("netTimerTime", "0")));
        
        //Additional timer time
        final Spinner additionalTimerTime = (Spinner)dialoglayout.findViewById(R.id.additionalTimerTime);
        ArrayAdapter<CharSequence> additionalTimerTimeAdapter = ArrayAdapter.createFromResource(this, R.array.netAdditionalTimeArray, android.R.layout.simple_spinner_item);
        additionalTimerTimeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        additionalTimerTime.setAdapter(additionalTimerTimeAdapter);
        additionalTimerTime.setSelection(Integer.parseInt(settings.getString("netAdditionalTimerTime", "0")));
        
        //Rated game
        final CheckBox ratedGame = (CheckBox)dialoglayout.findViewById(R.id.ratedGame);
        ratedGame.setChecked(settings.getBoolean("netRatedGame", true));
        
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setView(dialoglayout);
		builder.setMessage(this.getText(R.string.createBoard));
		
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            //Yes button clicked
    	        	new Thread(new Runnable(){
    	        		@Override
    	        		public void run() {
	    	        		NetGlobal.gridSize = getResources().getIntArray(R.array.netGameSizeValues)[gameSize.getSelectedItemPosition()];
	    	        		settings.edit().putString("netGridSize", gameSize.getSelectedItemPosition()+"").commit();
	    	        		NetGlobal.place = getResources().getIntArray(R.array.netPositionValues)[position.getSelectedItemPosition()];
			            	settings.edit().putString("netPosition", position.getSelectedItemPosition()+"").commit();
	    	        		NetGlobal.timerTime = getResources().getIntArray(R.array.netTimerValues)[timerTime.getSelectedItemPosition()];
			            	settings.edit().putString("netTimerTime", timerTime.getSelectedItemPosition()+"").commit();
	    	        		NetGlobal.additionalTimerTime = getResources().getIntArray(R.array.netAdditionalTimeValues)[additionalTimerTime.getSelectedItemPosition()];
			            	settings.edit().putString("netAdditionalTimerTime", additionalTimerTime.getSelectedItemPosition()+"").commit();
			            	NetGlobal.ratedGame = ratedGame.isChecked();
			            	settings.edit().putBoolean("netRatedGame", NetGlobal.ratedGame);
	    	        		try {
		    	        		String registrationUrl = String.format("http://www.iggamecenter.com/api_board_create.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&gid=%s&place=%s", NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), NetGlobal.uid, URLEncoder.encode(NetGlobal.session_id,"UTF-8"), NetGlobal.gid, NetGlobal.place);
		    	        		URL url = new URL(registrationUrl);
		    	        		SAXParserFactory spf = SAXParserFactory.newInstance();
		    	        		SAXParser parser = spf.newSAXParser();
		    	        		XMLReader reader = parser.getXMLReader();
		    	        		XMLHandler xmlHandler = new XMLHandler();
		    	        		reader.setContentHandler(xmlHandler);
		    	        		reader.parse(new InputSource(url.openStream()));
		
		    	        		ParsedDataset parsedDataset = xmlHandler.getParsedData();
		    	        		if(!parsedDataset.error){
			    	        		NetGlobal.sid = parsedDataset.getSid();
			    	        		NetGlobal.server = parsedDataset.getServer();
			    	        		
			    	        		//Apply board configuration
			    	        		int scored = 0;
			    	        		if(NetGlobal.ratedGame) scored++;
			    	        		String boardUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=SETUP&boardSize=%s&timerTotal=%s&timerInc=%s&scored=%s", URLEncoder.encode(NetGlobal.server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), NetGlobal.uid, URLEncoder.encode(NetGlobal.session_id,"UTF-8"), NetGlobal.sid, NetGlobal.gridSize, NetGlobal.timerTime*60, NetGlobal.additionalTimerTime, scored);
			    	        		new URL(boardUrl).openStream();
			    	        		
		    	        			WaitingRoomActivity.messages = new LinkedList<String>();
			    	        		startActivity(new Intent(getBaseContext(),WaitingRoomActivity.class));
			    	        		finish();
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
    	        		}}).start();
    	            break;
    	        case DialogInterface.BUTTON_NEGATIVE:
    	            //No button clicked
    	        	//Do nothing
    	            break;
    	        }
    	    }
    	};
		builder.setPositiveButton(this.getText(R.string.okay), dialogClickListener);
		builder.setNegativeButton(this.getText(R.string.cancel), dialogClickListener);
    	builder.show();
    }
}