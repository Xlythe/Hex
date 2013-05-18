package com.sam.hex.startup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sam.hex.HexGame;
import com.sam.hex.Preferences;
import com.sam.hex.R;
import com.sam.hex.activity.HomeActivity;
import com.sam.hex.playgames.LoginActivity;

/**
 * @author Will Harmon
 **/
public class StartUpActivity extends HomeActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Second button
        final Button instructionsButton = (Button) findViewById(R.id.instructionsButton);
        instructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), InstructionsActivity.class));
            }
        });

        // Third button
        final Button optionsButton = (Button) findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), Preferences.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh first button
        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), HexGame.class));
            }
        });
        startButton.setText(R.string.start);

        // Refresh fourth button
        final Button onlineButton = (Button) findViewById(R.id.onlineButton);
        onlineButton.setText(R.string.online);
        onlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
            }
        });
    }
}
