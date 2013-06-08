package com.sam.hex.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
@SuppressLint("NewApi")
public class PreferencesFragment extends PreferenceFragment {
    SharedPreferences settings;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().show();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setTitle(R.string.preferences);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        loadPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();

        setListeners();
    }

    public class NameListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            pref.setSummary(String.format(getString(R.string.player2NameSummary_onChange), newValue.toString()));
            return true;
        }
    }

    public class P1TypeListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            String[] texts = getResources().getStringArray(R.array.player1Array);
            String[] values = getResources().getStringArray(R.array.player1Values);
            String value = getTextValue(texts, values, newValue);
            pref.setSummary(String.format(getString(R.string.player2TypeSummary_onChange), value));
            return true;
        }
    }

    public class P2TypeListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            String[] texts = getResources().getStringArray(R.array.player2Array);
            String[] values = getResources().getStringArray(R.array.player2Values);
            String value = getTextValue(texts, values, newValue);
            pref.setSummary(String.format(getString(R.string.player2TypeSummary_onChange), value));
            return true;
        }
    }

    public class GridListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(newValue.toString().equals("0")) {
                // Custom value needed
                showInputDialog(getString(R.string.customGameSizeSummary));
                return false;
            }
            else {
                preference.setSummary(String.format(getString(R.string.gameSizeSummary_onChange), newValue, newValue));
                return true;
            }
        }
    }

    public class TimerListener implements OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference pref) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void setListeners() {
        // Change the summary to show the player's name
        p1NamePref = findPreference("player1Name");
        if(p1NamePref != null) {
            p1NamePref.setSummary(String.format(getString(R.string.player1NameSummary_onChange),
                    settings.getString("player1Name", getString(R.string.DEFAULT_P1_NAME))));
            p1NamePref.setOnPreferenceChangeListener(new NameListener());
        }
        p2NamePref = findPreference("player2Name");
        if(p2NamePref != null) {
            p2NamePref.setSummary(String.format(getString(R.string.player2NameSummary_onChange),
                    settings.getString("player2Name", getString(R.string.DEFAULT_P2_NAME))));
            p2NamePref.setOnPreferenceChangeListener(new NameListener());
        }

        // Change the summary to show the player's type
        p1TypePref = (ListPreference) findPreference("player1Type");
        if(p1TypePref != null) {
            String[] texts = getResources().getStringArray(R.array.player1Array);
            String[] values = getResources().getStringArray(R.array.player1Values);
            String newValue = settings.getString("player1Type", getString(R.string.DEFAULT_P1_NAME));
            String value = getTextValue(texts, values, newValue);
            p1TypePref.setSummary(String.format(getString(R.string.player1TypeSummary_onChange), value));
            p1TypePref.setOnPreferenceChangeListener(new P1TypeListener());
        }
        p2TypePref = (ListPreference) findPreference("player2Type");
        if(p2TypePref != null) {
            String[] texts = getResources().getStringArray(R.array.player2Array);
            String[] values = getResources().getStringArray(R.array.player2Values);
            String newValue = settings.getString("player2Type", getString(R.string.DEFAULT_P2_NAME));
            String value = getTextValue(texts, values, newValue);
            p2TypePref.setSummary(String.format(getString(R.string.player2TypeSummary_onChange), value));
            p2TypePref.setOnPreferenceChangeListener(new P2TypeListener());
        }

        // Allow for custom grid sizes
        gridPref = findPreference("gameSizePref");
        if(gridPref != null) {
            String defaultBoardSize = getString(R.integer.DEFAULT_BOARD_SIZE);
            String boardSize = Integer.valueOf(settings.getString("gameSizePref", defaultBoardSize)) == 0 ? settings.getString("customGameSizePref",
                    defaultBoardSize) : settings.getString("gameSizePref", defaultBoardSize);
            gridPref.setSummary(String.format(getString(R.string.gameSizeSummary_onChange), boardSize, boardSize));
            gridPref.setOnPreferenceChangeListener(new GridListener());
        }

        // Give that custom popup for timers
        timerPref = findPreference("timerOptionsPref");
        if(timerPref != null) {
            timerPref.setOnPreferenceClickListener(new TimerListener());
        }
    }

    private void loadPreferences() {
        addPreferencesFromResource(R.layout.preferences_general);
        addPreferencesFromResource(R.layout.preferences_player1);
        addPreferencesFromResource(R.layout.preferences_player2);
    }

    /**
     * Popup for custom grid sizes
     * */
    private void showInputDialog(String message) {
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    String boardSize = settings.getString("customGameSizePref", getString(R.integer.DEFAULT_BOARD_SIZE));
                    gridPref.setSummary(String.format(getString(R.string.gameSizeSummary_onChange), boardSize, boardSize));
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
