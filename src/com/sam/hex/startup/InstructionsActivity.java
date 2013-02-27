package com.sam.hex.startup;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class InstructionsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);

        TextView view = (TextView) findViewById(R.id.rules);
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
