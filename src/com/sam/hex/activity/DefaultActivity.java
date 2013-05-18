package com.sam.hex.activity;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class DefaultActivity extends SherlockFragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.red));
        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if(0 == titleId) titleId = com.actionbarsherlock.R.id.abs__action_bar_title;
        TextView title = (TextView) findViewById(titleId);
        title.setTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
