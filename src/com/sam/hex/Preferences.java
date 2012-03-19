package com.sam.hex;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.widget.EditText;

public class Preferences extends PreferenceActivity {
	SharedPreferences settings;
	Preference p1Pref;
	Preference p2Pref;
	Preference resetPref;
	EditTextPreference customGridPref;
	PreferenceScreen screen;
	Preference gridPref;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
        
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        //Hide custom grid size preference
        customGridPref = (EditTextPreference) findPreference("customGameSizePref");
        screen = (PreferenceScreen) findPreference("preferences");
        screen.removePreference(customGridPref);
    }
    
    class nameListener implements OnPreferenceChangeListener{        
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
        	settings.edit().putString(pref.getKey(), (String) newValue).commit();
            pref.setSummary("Name: "+newValue.toString());
            return false;
        }
    }
    
    class resetListener implements OnPreferenceClickListener{        
        @Override
        public boolean onPreferenceClick(Preference pref) {
        	//Clear everything
    		PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().commit();
    		//Reload settings
        	startActivity(new Intent(getBaseContext(),Preferences.class));
        	finish();
            return false;
        }
    }
    
    class gridListener implements OnPreferenceChangeListener{
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if(newValue.toString().equals("0")){
				//Custom value needed
				showInputDialog("Enter a grid size (Ex. 9)");
			}
			else{
				preference.setSummary("Pick a size for the gameboard (Current: "+newValue.toString()+"x"+newValue.toString()+")");
			}
			return true;
		}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//Change the summary to show the player's name
        p1Pref = findPreference("player1Name");
        p1Pref.setSummary("Name: "+settings.getString("player1Name", "Player1"));
        p1Pref.setOnPreferenceChangeListener(new nameListener());
        p2Pref = findPreference("player2Name");
        p2Pref.setSummary("Name: "+settings.getString("player2Name", "Player2"));
        p2Pref.setOnPreferenceChangeListener(new nameListener());
        
        //Set up the code to return everything to default
        resetPref = findPreference("resetPref");
        resetPref.setOnPreferenceClickListener(new resetListener());
        
        //Allow for custom grid sizes
        gridPref = findPreference("gameSizePref");
        if(settings.getString("gameSizePref", "7").equals("0")) gridPref.setSummary("Pick a size for the gameboard (Current: "+settings.getString("customGameSizePref", "7")+"x"+settings.getString("customGameSizePref", "7")+")");
        else gridPref.setSummary("Pick a size for the gameboard (Current: "+settings.getString("gameSizePref", "7")+"x"+settings.getString("gameSizePref", "7")+")");
        gridPref.setOnPreferenceChangeListener(new gridListener());
    }
    
    public void showInputDialog(String message){
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder     
        .setTitle(message)
        .setView(editText)
        .setPositiveButton("OK", new OnClickListener(){
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			settings.edit().putString("customGameSizePref", editText.getText().toString()).commit();
    			gridPref.setSummary("Pick a size for the gameboard (Current: "+settings.getString("customGameSizePref", "7")+"x"+settings.getString("customGameSizePref", "7")+")");
    		}
        })
        .setNegativeButton("Cancel", null)
        .show();
    } 
}