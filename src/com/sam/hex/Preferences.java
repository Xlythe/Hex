package com.sam.hex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	boolean CheckboxPreference;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
        
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        OnPreferenceChangeListener nameListener = new OnPreferenceChangeListener() {        
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
            	settings.edit().putString(pref.getKey(), (String) newValue).commit();
                pref.setSummary("Name: "+newValue.toString());
                return false;
            }
        };
        
        //Change the summary to show the player's name
        Preference p1Pref = findPreference("player1Name");
        p1Pref.setSummary("Name: "+settings.getString("player1Name", "Player1"));
        p1Pref.setOnPreferenceChangeListener(nameListener);
        Preference p2Pref = findPreference("player2Name");
        p2Pref.setSummary("Name: "+settings.getString("player2Name", "Player2"));
        p2Pref.setOnPreferenceChangeListener(nameListener);
        
        //Set up the code to return everything to default
        Preference resetPref = findPreference("resetPref");
        resetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {        
            @Override
            public boolean onPreferenceClick(Preference pref) {
            	//Clear everything
        		PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().commit();
        		//Reload settings
            	startActivity(new Intent(getBaseContext(),Preferences.class));
            	finish();
                return false;
            }
        });
    }
}