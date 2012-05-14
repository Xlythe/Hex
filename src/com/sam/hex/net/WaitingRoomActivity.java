package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sam.hex.Global;
import com.sam.hex.Preferences;
import com.sam.hex.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Will Harmon
 **/
public class WaitingRoomActivity extends Activity {
	public static LinkedList<String> messages = new LinkedList<String>();
	private RefreshGamePlayerlist refreshPlayers;
	private Runnable startGame = new Runnable(){
		public void run(){
			NetHexGame.startNewGame = true;
			startActivity(new Intent(getBaseContext(),NetHexGame.class));
        	finish();
		}
	};
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waitingroom);

        Button home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        
        Button options = (Button) findViewById(R.id.options);
        options.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	editBoard();
            }
        });
        
        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	sendMessage(WaitingRoomActivity.this.findViewById(R.id.body), (EditText) findViewById(R.id.sendMessage));
            }
        });
        
        EditText text = (EditText) findViewById(R.id.sendMessage);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId==EditorInfo.IME_ACTION_DONE || event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
					sendMessage(WaitingRoomActivity.this.findViewById(R.id.body), v);
					return true;
				}
				return false;
			}
		});
    	
    	ListView lobby = (ListView) findViewById(R.id.players);
    	Button start = new Button(this);
    	start.setText(this.getString(R.string.ready));
    	start.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new Thread(new Runnable(){
		    		public void run(){
		    			try {
		    				String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=START&lasteid=%s", URLEncoder.encode(NetGlobal.server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), NetGlobal.uid, URLEncoder.encode(NetGlobal.session_id,"UTF-8"), NetGlobal.sid, NetGlobal.lasteid);
		    				URL url = new URL(lobbyUrl);
		    				SAXParserFactory spf = SAXParserFactory.newInstance();
		    	            SAXParser parser = spf.newSAXParser();
		    	            XMLReader reader = parser.getXMLReader();
		    	            XMLHandler xmlHandler = new XMLHandler();
		    	            reader.setContentHandler(xmlHandler);
		    	            reader.parse(new InputSource(url.openStream()));
		    	            
		    	            ParsedDataset parsedDataset = xmlHandler.getParsedData();
		    	        	if(!parsedDataset.error){
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
    	lobby.addFooterView(start);
    	
    	refreshPlayers = new RefreshGamePlayerlist(new Handler(), new Runnable(){
    		public void run(){
				refreshPlayers(WaitingRoomActivity.this.findViewById(R.id.body), WaitingRoomActivity.this);
				refreshMessages(WaitingRoomActivity.this.findViewById(R.id.body));
    		}}, startGame, this);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	refreshPlayers.start();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	
    	if(refreshPlayers!=null) refreshPlayers.stop();
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
    
    public static void sendMessage(View body, TextView v){
    	final String message = v.getText().toString();
    	if(!message.equals("")){
        	v.setText("");
        	refreshMessages(body);
		}
    	
    	new Thread(new Runnable(){
    		public void run(){
    			try {
    				String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=MSG&message=%s&lasteid=%s", URLEncoder.encode(NetGlobal.server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), NetGlobal.uid, URLEncoder.encode(NetGlobal.session_id,"UTF-8"), NetGlobal.sid, URLEncoder.encode(message,"UTF-8"), NetGlobal.lasteid);
    				URL url = new URL(lobbyUrl);
    				SAXParserFactory spf = SAXParserFactory.newInstance();
    	            SAXParser parser = spf.newSAXParser();
    	            XMLReader reader = parser.getXMLReader();
    	            XMLHandler xmlHandler = new XMLHandler();
    	            reader.setContentHandler(xmlHandler);
    	            reader.parse(new InputSource(url.openStream()));
    	            
    	            ParsedDataset parsedDataset = xmlHandler.getParsedData();
    	        	if(!parsedDataset.error){
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
    
    public static void refreshMessages(View body){
    	TextView messageBoard = (TextView) body.findViewById(R.id.messages);
    	String msg = "";
    	for(int i=0;i<messages.size();i++){
    		msg+=messages.get(i)+"\n";
    	}
    	messageBoard.setText(msg);
    	final ScrollView sv = (ScrollView) body.findViewById(R.id.messageScroller);
    	sv.post(new Runnable() {            
    	    @Override
    	    public void run() {
    	        sv.fullScroll(View.FOCUS_DOWN);              
    	    }
    	});

    }
    
    public static void refreshPlayers(View body, Context context){
    	ListView lobby = (ListView) body.findViewById(R.id.players);
    	GamePlayerlistAdapter adapter = new GamePlayerlistAdapter(context,R.layout.waitingroom_list_item, NetGlobal.members);
        lobby.setAdapter(adapter);
        
        lobby.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
	        	
			}
        });
    }
    
    private void editBoard(){
    	LayoutInflater inflater = (LayoutInflater) WaitingRoomActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
    	View dialoglayout = inflater.inflate(R.layout.netlobby_createboard, null);
    	
    	//Board size
    	final Spinner gameSize = (Spinner)dialoglayout.findViewById(R.id.gameSize);
        ArrayAdapter<CharSequence> gameSizeAdapter = ArrayAdapter.createFromResource(this, R.array.netGameSizeArray, android.R.layout.simple_spinner_item);
        gameSizeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        gameSize.setAdapter(gameSizeAdapter);
        int pos = 0;
        while(NetGlobal.gridSize!=getResources().getIntArray(R.array.netGameSizeValues)[pos]){
        	pos++;
        }
        gameSize.setSelection(pos);
        
        //Position
        final Spinner position = (Spinner)dialoglayout.findViewById(R.id.position);
        ArrayAdapter<CharSequence> positionAdapter = ArrayAdapter.createFromResource(this, R.array.netPositionArray, android.R.layout.simple_spinner_item);
        positionAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        position.setAdapter(positionAdapter);
        position.setSelection(NetGlobal.place);

        //Timer time
        final Spinner timerTime = (Spinner)dialoglayout.findViewById(R.id.timerTime);
        ArrayAdapter<CharSequence> timerTimeAdapter = ArrayAdapter.createFromResource(this, R.array.netTimerArray, android.R.layout.simple_spinner_item);
        timerTimeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        timerTime.setAdapter(timerTimeAdapter);
        pos = 0;
        while(NetGlobal.timerTime!=getResources().getIntArray(R.array.netTimerValues)[pos]){
        	pos++;
        }
        timerTime.setSelection(pos);

        //Additional timer time
        final Spinner additionalTimerTime = (Spinner)dialoglayout.findViewById(R.id.additionalTimerTime);
        ArrayAdapter<CharSequence> additionalTimerTimeAdapter = ArrayAdapter.createFromResource(this, R.array.netAdditionalTimeArray, android.R.layout.simple_spinner_item);
        additionalTimerTimeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        additionalTimerTime.setAdapter(additionalTimerTimeAdapter);
        pos = 0;
        while(NetGlobal.additionalTimerTime!=getResources().getIntArray(R.array.netAdditionalTimeValues)[pos]){
        	pos++;
        }
        additionalTimerTime.setSelection(pos);
        
        //Rated game
        final CheckBox ratedGame = (CheckBox)dialoglayout.findViewById(R.id.ratedGame);
        ratedGame.setChecked(NetGlobal.ratedGame);
        
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
    	        			int previousGridSize = NetGlobal.gridSize;
	    	        		NetGlobal.gridSize = getResources().getIntArray(R.array.netGameSizeValues)[gameSize.getSelectedItemPosition()];
	    	        		NetGlobal.place = getResources().getIntArray(R.array.netPositionValues)[position.getSelectedItemPosition()];
	    	        		int previousTime = NetGlobal.timerTime;
	    	        		NetGlobal.timerTime = getResources().getIntArray(R.array.netTimerValues)[timerTime.getSelectedItemPosition()];
	    	        		int previousAdditionalTime = NetGlobal.additionalTimerTime;
	    	        		NetGlobal.additionalTimerTime = getResources().getIntArray(R.array.netAdditionalTimeValues)[additionalTimerTime.getSelectedItemPosition()];
	    	        		boolean previousRatedGame = NetGlobal.ratedGame;
	    	        		NetGlobal.ratedGame = ratedGame.isChecked();
	    	        		int scored = 0;
	    	        		if(NetGlobal.ratedGame) scored++;
	    	        		try {
	    	        			String boardUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=SETUP&boardSize=%s&timerTotal=%s&timerInc=%s&scored=%s&lasteid=%s", URLEncoder.encode(NetGlobal.server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), NetGlobal.uid, URLEncoder.encode(NetGlobal.session_id,"UTF-8"), NetGlobal.sid, NetGlobal.gridSize, NetGlobal.timerTime*60, NetGlobal.additionalTimerTime, scored, NetGlobal.lasteid);
		    	        		String placeUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=PLACE&place=%s&lasteid=%s", URLEncoder.encode(NetGlobal.server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), NetGlobal.uid, URLEncoder.encode(NetGlobal.session_id,"UTF-8"), NetGlobal.sid, NetGlobal.place, NetGlobal.lasteid);
	    	        			if(previousGridSize!=NetGlobal.gridSize || previousTime!=NetGlobal.timerTime || previousAdditionalTime!=NetGlobal.additionalTimerTime || previousRatedGame!=NetGlobal.ratedGame) new URL(boardUrl).openStream();
	    	        			new URL(placeUrl).openStream();
	    	        		} catch (MalformedURLException e) {
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

    public static class GamePlayerlistAdapter extends ArrayAdapter<ParsedDataset.Member> {
            private ArrayList<ParsedDataset.Member> items;
            private Context context;
            
            public GamePlayerlistAdapter(Context context, int textViewResourceId, ArrayList<ParsedDataset.Member> items) {
                    super(context, textViewResourceId, items);
                    this.context = context;
                    this.items = items;
            }
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                    View v = convertView;
                    if (v == null) {
                        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        v = vi.inflate(R.layout.waitingroom_list_item, null);
                    }
                    ParsedDataset.Member o = items.get(position);
                    if (o != null) {
                            TextView name = (TextView) v.findViewById(R.id.text1);
                            ImageButton image = (ImageButton) v.findViewById(R.id.image1);
                            if (name != null) {
                                  name.setText(o.name);
                                  if(o.state.equals("OFFERSTART")) name.setTextColor(Color.GREEN);
                            }
                            if(image != null){
                                  if(o.place==0) image.setColorFilter(Color.TRANSPARENT);
                                  else if(o.place==1) image.setColorFilter(Global.player1DefaultColor);
                                  else if(o.place==2) image.setColorFilter(Global.player2DefaultColor);
                            }
                    }
                    return v;
            }
    }
}