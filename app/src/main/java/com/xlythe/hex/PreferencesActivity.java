package com.xlythe.hex;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.xlythe.hex.fragment.PreferencesFragment;

/** AndroidX host for the settings screen. */
public final class PreferencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        TextView title = findViewById(R.id.title);
        title.setText(R.string.activity_title_preferences);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, new PreferencesFragment())
                    .commit();
        }
    }
}
