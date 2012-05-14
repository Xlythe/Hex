package com.sam.hex;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author Will Harmon
 **/
public class Preferences extends PreferenceActivity {
	SharedPreferences settings;
	PreferenceScreen screen;
	Preference p1Pref;
	Preference p2Pref;
	Preference lanPlPref;
	Preference resetPref;
	Preference gridPref;
	Preference timerPref;
	Preference passwordPref;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        loadPreferences();
    }
    
    class locListener implements OnPreferenceChangeListener{        
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
        	settings.edit().putString(pref.getKey(), (String) newValue).commit();
        	screen.removeAll();
        	loadPreferences();
            
            setListeners();
            return true;
        }
    }
    
    class nameListener implements OnPreferenceChangeListener{        
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            pref.setSummary(GameAction.insert(getApplicationContext().getString(R.string.player2NameSummary_onChange),newValue.toString()));
            return true;
        }
    }
    
    class resetListener implements OnPreferenceClickListener{        
        @Override
        public boolean onPreferenceClick(Preference pref) {
        	//Clear everything
    		PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().commit();
    		//Reload settings
    		screen.removeAll();
        	loadPreferences();
        	setListeners();
            return false;
        }
    }
    
    class gridListener implements OnPreferenceChangeListener{
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if(newValue.toString().equals("0")){
				//Custom value needed
				showInputDialog(getApplicationContext().getString(R.string.customGameSizeSummary));
				return false;
			}
			else{
				preference.setSummary(GameAction.insert(getApplicationContext().getString(R.string.gameSizeSummary_onChange), newValue.toString()));
				return true;
			}
		}
    }
    
    class timerListener implements OnPreferenceClickListener{        
        @Override
        public boolean onPreferenceClick(Preference pref) {
        	LayoutInflater inflater = (LayoutInflater) Preferences.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        	View dialoglayout = inflater.inflate(R.layout.preferences_timer, null);
        	final Spinner timerType = (Spinner) dialoglayout.findViewById(R.id.timerType);
        	final EditText timer = (EditText) dialoglayout.findViewById(R.id.timer);
        	timer.setText(settings.getString("timerPref", "0"));
        	timerType.setSelection(Integer.parseInt(settings.getString("timerTypePref", "0")));
        	timerType.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int arg2, long arg3) {
					if(arg2>0){
						timer.setVisibility(View.VISIBLE);
					}
					else{
						timer.setVisibility(View.GONE);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					timer.setVisibility(View.GONE);
				}
			});
        	AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
            builder
            .setView(dialoglayout)
            .setPositiveButton(getApplicationContext().getString(R.string.okay), new OnClickListener(){
        		@Override
        		public void onClick(DialogInterface dialog, int which) {
        			settings.edit().putString("timerTypePref", getResources().getStringArray(R.array.timerTypeValues)[timerType.getSelectedItemPosition()]).commit();
        			settings.edit().putString("timerPref", timer.getText().toString()).commit();
        		}
            })
            .setNegativeButton(getApplicationContext().getString(R.string.cancel), null)
            .show();
            return true;
        }
    }
    
    class passwordListener implements OnPreferenceChangeListener{        
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
        	settings.edit().putString("netPassword", GameAction.md5((String) newValue)).commit();
            return false;
        }
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	setListeners();
    }
    
    private void setListeners(){
    	//Hide player2 unless the game location is on a single phone
    	Preference gameLoc = findPreference("gameLocation"); 
    	gameLoc.setOnPreferenceChangeListener(new locListener());
    	
    	//Change the summary to show the player's name
        p1Pref = findPreference("player1Name");
        if(p1Pref!=null){
        	p1Pref.setSummary(GameAction.insert(getApplicationContext().getString(R.string.player1NameSummary_onChange), settings.getString("player1Name", "Player1")));
        	p1Pref.setOnPreferenceChangeListener(new nameListener());
        }
        p2Pref = findPreference("player2Name");
        if(p2Pref!=null){
	        p2Pref.setSummary(GameAction.insert(getApplicationContext().getString(R.string.player2NameSummary_onChange), settings.getString("player2Name", "Player2")));
	        p2Pref.setOnPreferenceChangeListener(new nameListener());
        }
        lanPlPref = findPreference("lanPlayerName");
        if(lanPlPref!=null){
	        lanPlPref.setSummary(GameAction.insert(getApplicationContext().getString(R.string.lanPlayerNameSummary_onChange), settings.getString("lanPlayerName", "Player")));
	        lanPlPref.setOnPreferenceChangeListener(new nameListener());
        }
        
        //Set up the code to return everything to default
        resetPref = findPreference("resetPref");
        resetPref.setOnPreferenceClickListener(new resetListener());
        
        //Allow for custom grid sizes
        gridPref = findPreference("gameSizePref");
        if(gridPref!=null){
        	if(settings.getString("gameSizePref", "7").equals("0")) gridPref.setSummary(GameAction.insert(getApplicationContext().getString(R.string.gameSizeSummary_onChange), settings.getString("customGameSizePref", "7")));
        	else gridPref.setSummary(GameAction.insert(getApplicationContext().getString(R.string.gameSizeSummary_onChange), settings.getString("gameSizePref", "7")));
        	gridPref.setOnPreferenceChangeListener(new gridListener());
        }
        
        //Give that custom popup for timers
        timerPref = findPreference("timerOptionsPref");
        if(timerPref!=null){
	        timerPref.setOnPreferenceClickListener(new timerListener());
        }
        
        //Encrypt the password
        passwordPref = findPreference("visibleNetPassword");
        if(passwordPref!=null){
	        passwordPref.setOnPreferenceChangeListener(new passwordListener());
        }
    }
    
    private void loadPreferences(){
    	setContentView(R.layout.preferences);
    	TextView title = (TextView) findViewById(R.id.actionbarTitle);
    	addPreferencesFromResource(R.layout.preferences_location);
        ListPreference val = (ListPreference) findPreference("gameLocation");
        if(val.getValue().equals("0")){
            title.setText(this.getText(R.string.preferences));
        	addPreferencesFromResource(R.layout.preferences_general);
        	addPreferencesFromResource(R.layout.preferences_player1);
    		addPreferencesFromResource(R.layout.preferences_player2);
    		
    		//Hide hidden preferences
        	PreferenceCategory general = (PreferenceCategory) findPreference("generalCategory");
            general.removePreference(findPreference("customGameSizePref"));
            general.removePreference(findPreference("timerTypePref"));
            general.removePreference(findPreference("timerPref"));
    	}
        else if(val.getValue().equals("1")){
        	addPreferencesFromResource(R.layout.preferences_general);
        	addPreferencesFromResource(R.layout.preferences_lanplayer);

        	//Hide hidden preferences
        	PreferenceCategory general = (PreferenceCategory) findPreference("generalCategory");
            general.removePreference(findPreference("customGameSizePref"));
            general.removePreference(findPreference("timerTypePref"));
            general.removePreference(findPreference("timerPref"));
        }
        else if(val.getValue().equals("2")){
            title.setText(this.getText(R.string.preferences_net));
        	addPreferencesFromResource(R.layout.preferences_netplayer);
        	
        	//Hide hidden preferences
        	PreferenceCategory general = (PreferenceCategory) findPreference("netPlayerCategory");
        	general.removePreference(findPreference("netPassword"));
        	general.removePreference(findPreference("netPosition"));
            general.removePreference(findPreference("netGridSize"));
            general.removePreference(findPreference("netTimerTime"));
            general.removePreference(findPreference("netAdditionalTimerTime"));
            general.removePreference(findPreference("netRatedGame"));
        }
    	addPreferencesFromResource(R.layout.preferences_reset);
    	
        screen = (PreferenceScreen) findPreference("preferences");
        
        Button home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        
        ListView list = getListView();
        View ads = View.inflate(this, R.layout.preferences_ads, null);
        list.addFooterView(ads);
    }
    
    /**
     * Popup for custom grid sizes
     * */
    private void showInputDialog(String message){
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
        builder     
        .setTitle(message)
        .setView(editText)
        .setPositiveButton(getApplicationContext().getString(R.string.okay), new OnClickListener(){
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			if(!editText.getText().toString().equals("")){
	    			int input = Integer.decode(editText.getText().toString());
	    			if(input>30){
	    				input = 30;
	    			}
	    			else if(input<4){
	    				input = 4;
	    			}
	    			settings.edit().putString("customGameSizePref", input+"").commit();
	    			settings.edit().putString("gameSizePref", "0").commit();
	    			gridPref.setSummary(GameAction.insert(getApplicationContext().getString(R.string.gameSizeSummary_onChange), settings.getString("customGameSizePref", "7")));
	        		screen.removeAll();
	            	loadPreferences();
	            	setListeners();
    			}
    		}
        })
        .setNegativeButton(getApplicationContext().getString(R.string.cancel), null)
        .show();
    } 
}