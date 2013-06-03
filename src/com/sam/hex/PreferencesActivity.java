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
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author Will Harmon
 **/
public class PreferencesActivity extends SherlockPreferenceActivity {
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
    public void onCreate(Bundle savedInstanceState) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    class nameListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            pref.setSummary(String.format(getString(R.string.player2NameSummary_onChange), newValue.toString()));
            return true;
        }
    }

    class p1typeListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            String[] texts = getResources().getStringArray(R.array.player1Array);
            String[] values = getResources().getStringArray(R.array.player1Values);
            String value = getTextValue(texts, values, newValue);
            pref.setSummary(String.format(getString(R.string.player2TypeSummary_onChange), value));
            return true;
        }
    }

    class p2typeListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            String[] texts = getResources().getStringArray(R.array.player2Array);
            String[] values = getResources().getStringArray(R.array.player2Values);
            String value = getTextValue(texts, values, newValue);
            pref.setSummary(String.format(getString(R.string.player2TypeSummary_onChange), value));
            return true;
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
                preference.setSummary(String.format(getString(R.string.gameSizeSummary_onChange), newValue));
                return true;
            }
        }
    }

    class timerListener implements OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference pref) {
            LayoutInflater inflater = (LayoutInflater) PreferencesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialoglayout = inflater.inflate(R.layout.preferences_timer, null);
            final Spinner timerType = (Spinner) dialoglayout.findViewById(R.id.timerType);
            final EditText timer = (EditText) dialoglayout.findViewById(R.id.timer);
            timer.setText(settings.getString("timerPref", getString(R.integer.DEFAULT_TIMER_TIME)));
            timerType.setSelection(Integer.valueOf(settings.getString("timerTypePref", getString(R.integer.DEFAULT_TIMER_TYPE))));
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
            AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
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

    class menuListener implements OnPreferenceClickListener {
        int type;

        menuListener(int type) {
            this.type = type;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            final Intent intent = new Intent(getBaseContext(), PreferencesActivity.class);
            intent.putExtra("type", type);
            startActivity(intent);
            return false;
        }
    }

    private void setListeners() {
        // Change the summary to show the player's name
        p1NamePref = findPreference("player1Name");
        if(p1NamePref != null) {
            p1NamePref.setSummary(String.format(getString(R.string.player1NameSummary_onChange),
                    settings.getString("player1Name", getString(R.string.DEFAULT_P1_NAME))));
            p1NamePref.setOnPreferenceChangeListener(new nameListener());
        }
        p2NamePref = findPreference("player2Name");
        if(p2NamePref != null) {
            p2NamePref.setSummary(String.format(getString(R.string.player2NameSummary_onChange),
                    settings.getString("player2Name", getString(R.string.DEFAULT_P2_NAME))));
            p2NamePref.setOnPreferenceChangeListener(new nameListener());
        }

        // Change the summary to show the player's type
        p1TypePref = (ListPreference) findPreference("player1Type");
        if(p1TypePref != null) {
            String[] texts = getResources().getStringArray(R.array.player1Array);
            String[] values = getResources().getStringArray(R.array.player1Values);
            String newValue = settings.getString("player1Type", getString(R.string.DEFAULT_P1_NAME));
            String value = getTextValue(texts, values, newValue);
            p1TypePref.setSummary(String.format(getString(R.string.player1TypeSummary_onChange), value));
            p1TypePref.setOnPreferenceChangeListener(new p1typeListener());
        }
        p2TypePref = (ListPreference) findPreference("player2Type");
        if(p2TypePref != null) {
            String[] texts = getResources().getStringArray(R.array.player2Array);
            String[] values = getResources().getStringArray(R.array.player2Values);
            String newValue = settings.getString("player2Type", getString(R.string.DEFAULT_P2_NAME));
            String value = getTextValue(texts, values, newValue);
            p2TypePref.setSummary(String.format(getString(R.string.player2TypeSummary_onChange), value));
            p2TypePref.setOnPreferenceChangeListener(new p2typeListener());
        }

        // Allow for custom grid sizes
        gridPref = findPreference("gameSizePref");
        if(gridPref != null) {
            String defaultBoardSize = getString(R.integer.DEFAULT_BOARD_SIZE);
            if(Integer.valueOf(settings.getString("gameSizePref", defaultBoardSize)) == 0) gridPref.setSummary(String.format(
                    getString(R.string.gameSizeSummary_onChange), Integer.valueOf(settings.getString("customGameSizePref", defaultBoardSize))));
            else gridPref.setSummary(String.format(getString(R.string.gameSizeSummary_onChange),
                    Integer.valueOf(settings.getString("gameSizePref", defaultBoardSize))));
            gridPref.setOnPreferenceChangeListener(new gridListener());
        }

        // Give that custom popup for timers
        timerPref = findPreference("timerOptionsPref");
        if(timerPref != null) {
            timerPref.setOnPreferenceClickListener(new timerListener());
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

    private void loadPreferences() {
        setContentView(R.layout.preferences);
        getSupportActionBar().setTitle(R.string.preferences);
        if(!in_submenu) {
            addPreferencesFromResource(R.layout.preferences_abstract);
        }
        else if(in_general) {
            addPreferencesFromResource(R.layout.preferences_general);

            generalScreen = (PreferenceScreen) findPreference("generalScreen");
        }
        else if(in_p1) {
            addPreferencesFromResource(R.layout.preferences_player1);
        }
        else if(in_p2) {
            addPreferencesFromResource(R.layout.preferences_player2);
        }
        screen = (PreferenceScreen) findPreference("preferences");
    }

    /**
     * Popup for custom grid sizes
     * */
    private void showInputDialog(String message) {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
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
                    settings.edit().putString("customGameSizePref", String.valueOf(input)).commit();
                    settings.edit().putString("gameSizePref", String.valueOf(0)).commit();
                    gridPref.setSummary(String.format(getString(R.string.gameSizeSummary_onChange),
                            Integer.valueOf(settings.getString("customGameSizePref", getString(R.integer.DEFAULT_BOARD_SIZE)))));
                    generalScreen.removeAll();
                    loadPreferences();
                    setListeners();
                }
            }
        }).setNegativeButton(getString(R.string.cancel), null).show();
    }

    private String getTextValue(String[] texts, String[] values, Object value) {
        for(int i = 0; i < values.length; i++) {
            String s = values[i];
            if(s.equals(value)) {
                return texts[i];
            }
        }
        return null;
    }
}
