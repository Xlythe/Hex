package com.sam.hex;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.sam.hex.fragment.PreferencesFragment;

/**
 * @author Will Harmon
 **/
@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class PreferencesActivity extends PreferenceActivity {
    SharedPreferences settings;
    Preference gridPref;
    Preference timerPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.activity_title_preferences);
        if (Build.VERSION.SDK_INT < 11) {
            settings = PreferenceManager.getDefaultSharedPreferences(this);
            loadPreferences();
        } else {
            if (savedInstanceState == null) {
                PreferencesFragment preferences = new PreferencesFragment();
                getFragmentManager().beginTransaction().add(R.id.content, preferences).commit();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onResume() {
        super.onResume();
        setListeners();
    }

    private class DifficultyListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, @NonNull Object newValue) {
            preference.setSummary(getResources().getStringArray(R.array.comDifficultyArray)[Integer.valueOf(newValue.toString())]);
            return true;
        }
    }

    private class GridListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, @NonNull Object newValue) {
            if (newValue.toString().equals("0")) {
                // Custom value needed
                showInputDialog(getString(R.string.preferences_summary_custom_game_size));
                return false;
            } else {
                preference.setSummary(String.format(getString(R.string.preferences_summary_game_size), newValue, newValue));
                return true;
            }
        }
    }

    private class TimerListener implements OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference pref) {
            LayoutInflater inflater = (LayoutInflater) PreferencesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialoglayout = inflater.inflate(R.layout.preferences_timer, null);
            final Spinner timerType = (Spinner) dialoglayout.findViewById(R.id.timerType);
            final EditText timer = (EditText) dialoglayout.findViewById(R.id.timer);
            timer.setText(settings.getString(Settings.TIMER, Integer.toString(getResources().getInteger(R.integer.DEFAULT_TIMER_TIME))));
            timerType.setSelection(Integer.valueOf(settings.getString(Settings.TIMER_TYPE, Integer.toString(getResources().getInteger(R.integer.DEFAULT_TIMER_TYPE)))));
            timerType.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int arg2, long arg3) {
                    if (arg2 > 0) {
                        timer.setVisibility(View.VISIBLE);
                    } else {
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
                    String timerTime = timer.getText().toString();
                    if (timerTime.isEmpty()) timerTime = "0";
                    settings.edit()
                            .putString(Settings.TIMER_TYPE, getResources().getStringArray(R.array.timerTypeValues)[timerType.getSelectedItemPosition()])
                            .putString(Settings.TIMER, timerTime)
                            .apply();
                }
            }).setNegativeButton(getString(R.string.cancel), null).show();
            return true;
        }
    }

    private void setListeners() {
        // Allow for custom grid sizes
        gridPref = findPreference(Settings.GAME_SIZE);
        if (gridPref != null) {
            String boardSize = String.valueOf(Settings.getGridSize(this));
            gridPref.setSummary(String.format(getString(R.string.preferences_summary_game_size), boardSize, boardSize));
            gridPref.setOnPreferenceChangeListener(new GridListener());
        }

        // Give that custom popup for timers
        timerPref = findPreference(Settings.TIMER_OPTIONS);
        if (timerPref != null) {
            timerPref.setOnPreferenceClickListener(new TimerListener());
        }

        Preference comDifficultyPref = findPreference(Settings.DIFFICULTY);
        if (comDifficultyPref != null) {
            comDifficultyPref.setOnPreferenceChangeListener(new DifficultyListener());
            comDifficultyPref.setSummary(getResources().getStringArray(R.array.comDifficultyArray)[Settings.getComputerDifficulty(this)]);
        }
    }

    private void loadPreferences() {
        addPreferencesFromResource(R.layout.preferences_general);
    }

    /**
     * Popup for custom grid sizes
     */
    private void showInputDialog(String message) {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
        builder.setTitle(message).setView(editText).setPositiveButton(getString(R.string.okay), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!editText.getText().toString().equals("")) {
                    int input = Integer.decode(editText.getText().toString());
                    if (input > Settings.MAX_BOARD_SIZE) {
                        input = Settings.MAX_BOARD_SIZE;
                    } else if (input < Settings.MIN_BOARD_SIZE) {
                        input = Settings.MIN_BOARD_SIZE;
                    }
                    settings.edit()
                            .putString(Settings.CUSTOM_GAME_SIZE, String.valueOf(input))
                            .putString(Settings.GAME_SIZE, String.valueOf(0))
                            .apply();
                    String boardSize = settings.getString(Settings.CUSTOM_GAME_SIZE, Integer.toString(getResources().getInteger(R.integer.DEFAULT_BOARD_SIZE)));
                    gridPref.setSummary(String.format(getString(R.string.preferences_summary_game_size), boardSize, boardSize));
                }
            }
        }).setNegativeButton(getString(R.string.cancel), null).show();
    }
}
