package com.sam.hex.activity;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class HomeActivity extends SherlockActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.red));
        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if(0 == titleId) titleId = com.actionbarsherlock.R.id.abs__action_bar_title;
        TextView title = (TextView) findViewById(titleId);
        if(title != null) title.setTextColor(Color.WHITE);
    }
}
