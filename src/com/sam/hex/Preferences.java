package com.sam.hex;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * @author Will Harmon
 **/
public class Preferences extends PreferenceActivity {
    private static final int GENERAL = 0;
    private static final int PLAYER1 = 1;
    private static final int PLAYER2 = 2;

    SharedPreferences settings;
    PreferenceScreen screen;
    PreferenceScreen generalScreen;
    Preference p1NamePref;
    Preference p2NamePref;
    ListPreference p1TypePref;
    ListPreference p2TypePref;
    Preference resetPref;
    Preference gridPref;
    Preference timerPref;
    Preference passwordPref;
    Preference options;
    Preference p1;
    Preference p2;

    private boolean in_submenu = false;
    private boolean in_general = false;
    private boolean in_p1 = false;
    private boolean in_p2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        if(getIntent().getExtras() != null) {
            in_submenu = true;
            Bundle extras = getIntent().getExtras();
            int type = extras.getInt("type");

            if(type == GENERAL) {
                in_general = true;
            }
            else if(type == PLAYER1) {
                in_p1 = true;
            }
            else if(type == PLAYER2) {
                in_p2 = true;
            }
        }

        loadPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();

        setListeners();
    }

    class locListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            settings.edit().putString(pref.getKey(), (String) newValue).commit();
            screen.removeAll();
            loadPreferences();

            setListeners();
            return true;
        }
    }

    class nameListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            pref.setSummary(GameAction.insert(getString(R.string.player2NameSummary_onChange), newValue.toString()));
            return true;
        }
    }

    class typeListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            pref.setSummary(GameAction.insert(getString(R.string.player2TypeSummary_onChange), ((ListPreference) pref).getEntry().toString()));
            return true;
        }
    }

    class resetListener implements OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference pref) {
            // Clear everything
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().commit();
            // Reload settings
            if(screen != null) screen.removeAll();
            if(generalScreen != null) generalScreen.removeAll();
            loadPreferences();
            setListeners();
            return false;
        }
    }

    class gridListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(newValue.toString().equals("0")) {
                // Custom value needed
                showInputDialog(getString(R.string.customGameSizeSummary));
                return false;
            }
            else {
                preference.setSummary(GameAction.insert(getString(R.string.gameSizeSummary_onChange), newValue.toString()));
                return true;
            }
        }
    }

    class timerListener implements OnPreferenceClickListener {
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
                    if(arg2 > 0) {
                        timer.setVisibility(View.VISIBLE);
                    }
                    else {
                        timer.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    timer.setVisibility(View.GONE);
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
            builder.setView(dialoglayout).setPositiveButton(getString(R.string.okay), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    settings.edit().putString("timerTypePref", getResources().getStringArray(R.array.timerTypeValues)[timerType.getSelectedItemPosition()])
                            .commit();
                    settings.edit().putString("timerPref", timer.getText().toString()).commit();
                }
            }).setNegativeButton(getString(R.string.cancel), null).show();
            return true;
        }
    }

    class passwordListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            settings.edit().putString("netPassword", GameAction.md5((String) newValue)).commit();
            return false;
        }
    }

    class menuListener implements OnPreferenceClickListener {
        int type;

        menuListener(int type) {
            this.type = type;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            final Intent intent = new Intent(getBaseContext(), Preferences.class);
            intent.putExtra("type", type);
            startActivity(intent);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private void setListeners() {
        // Hide player2 unless the game location is on a single phone
        Preference gameLoc = findPreference("gameLocation");
        if(gameLoc != null) {
            gameLoc.setOnPreferenceChangeListener(new locListener());
        }

        // Change the summary to show the player's name
        p1NamePref = findPreference("player1Name");
        if(p1NamePref != null) {
            p1NamePref.setSummary(GameAction.insert(getString(R.string.player1NameSummary_onChange), settings.getString("player1Name", "Player1")));
            p1NamePref.setOnPreferenceChangeListener(new nameListener());
        }
        p2NamePref = findPreference("player2Name");
        if(p2NamePref != null) {
            p2NamePref.setSummary(GameAction.insert(getString(R.string.player2NameSummary_onChange), settings.getString("player2Name", "Player2")));
            p2NamePref.setOnPreferenceChangeListener(new nameListener());
        }

        // Change the summary to show the player's type
        p1TypePref = (ListPreference) findPreference("player1Type");
        if(p1TypePref != null) {
            p1TypePref.setSummary(GameAction.insert(getString(R.string.player1TypeSummary_onChange), p1TypePref.getEntry().toString()));
            p1TypePref.setOnPreferenceChangeListener(new typeListener());
        }
        p2TypePref = (ListPreference) findPreference("player2Type");
        if(p2TypePref != null) {
            p1TypePref.setSummary(GameAction.insert(getString(R.string.player2TypeSummary_onChange), p2TypePref.getEntry().toString()));
            p2TypePref.setOnPreferenceChangeListener(new typeListener());
        }

        // Set up the code to return everything to default
        resetPref = findPreference("resetPref");
        if(resetPref != null) {
            resetPref.setOnPreferenceClickListener(new resetListener());
        }

        // Allow for custom grid sizes
        gridPref = findPreference("gameSizePref");
        if(gridPref != null) {
            if(settings.getString("gameSizePref", "7").equals("0")) gridPref.setSummary(GameAction.insert(getString(R.string.gameSizeSummary_onChange),
                    settings.getString("customGameSizePref", "7")));
            else gridPref.setSummary(GameAction.insert(getString(R.string.gameSizeSummary_onChange), settings.getString("gameSizePref", "7")));
            gridPref.setOnPreferenceChangeListener(new gridListener());
        }

        // Give that custom popup for timers
        timerPref = findPreference("timerOptionsPref");
        if(timerPref != null) {
            timerPref.setOnPreferenceClickListener(new timerListener());
        }

        // Encrypt the password
        passwordPref = findPreference("visibleNetPassword");
        if(passwordPref != null) {
            passwordPref.setOnPreferenceChangeListener(new passwordListener());
        }

        // Set up the abstract menu
        options = findPreference("general");
        if(options != null) {
            options.setOnPreferenceClickListener(new menuListener(GENERAL));
        }
        p1 = findPreference("p1");
        if(p1 != null) {
            p1.setOnPreferenceClickListener(new menuListener(PLAYER1));
        }
        p2 = findPreference("p2");
        if(p2 != null) {
            p2.setOnPreferenceClickListener(new menuListener(PLAYER2));
        }
    }

    @SuppressWarnings("deprecation")
    private void loadPreferences() {
        setContentView(R.layout.preferences);
        int gameLoc;
        if(!in_submenu) {
            addPreferencesFromResource(R.layout.preferences_location);
            ListPreference val = (ListPreference) findPreference("gameLocation");
            gameLoc = Integer.parseInt(val.getValue());
        }
        else {
            gameLoc = 0;
        }
        if(gameLoc == 0) {
            if(!in_submenu) {
                addPreferencesFromResource(R.layout.preferences_abstract);
            }
            else if(in_general) {
                addPreferencesFromResource(R.layout.preferences_general);

                // Hide hidden preferences
                generalScreen = (PreferenceScreen) findPreference("generalScreen");
                generalScreen.removePreference(findPreference("customGameSizePref"));
                generalScreen.removePreference(findPreference("timerTypePref"));
                generalScreen.removePreference(findPreference("timerPref"));
            }
            else if(in_p1) {
                addPreferencesFromResource(R.layout.preferences_player1);
            }
            else if(in_p2) {
                addPreferencesFromResource(R.layout.preferences_player2);
            }
        }
        else if(gameLoc == 1) {
            addPreferencesFromResource(R.layout.preferences_general);

            // Hide hidden preferences
            PreferenceCategory general = (PreferenceCategory) findPreference("generalCategory");
            general.removePreference(findPreference("customGameSizePref"));
            general.removePreference(findPreference("timerTypePref"));
            general.removePreference(findPreference("timerPref"));
        }
        else if(gameLoc == 2) {
            addPreferencesFromResource(R.layout.preferences_netplayer);

            // Hide hidden preferences
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
    }

    /**
     * Popup for custom grid sizes
     * */
    private void showInputDialog(String message) {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
        builder.setTitle(message).setView(editText).setPositiveButton(getString(R.string.okay), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!editText.getText().toString().equals("")) {
                    int input = Integer.decode(editText.getText().toString());
                    if(input > 30) {
                        input = 30;
                    }
                    else if(input < 4) {
                        input = 4;
                    }
                    settings.edit().putString("customGameSizePref", input + "").commit();
                    settings.edit().putString("gameSizePref", "0").commit();
                    gridPref.setSummary(GameAction.insert(getString(R.string.gameSizeSummary_onChange), settings.getString("customGameSizePref", "7")));
                    generalScreen.removeAll();
                    loadPreferences();
                    setListeners();
                }
            }
        }).setNegativeButton(getString(R.string.cancel), null).show();
    }
}
