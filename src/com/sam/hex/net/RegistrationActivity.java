package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.sam.hex.R;
import com.sam.hex.net.igGC.ParsedDataset;
import com.sam.hex.net.igGC.igGameCenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Will Harmon
 **/
public class RegistrationActivity extends Activity {
	SharedPreferences settings;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        
        Button home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        Button enter = (Button) findViewById(R.id.loginEnter);
        final EditText username = (EditText) findViewById(R.id.username);
        final EditText password = (EditText) findViewById(R.id.password);
        final EditText email = (EditText) findViewById(R.id.email);
        enter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	new Thread(new Runnable(){
					@Override
					public void run() {
						try {
			                ParsedDataset parsedDataset = igGameCenter.register(username.getText().toString(), password.getText().toString(), email.getText().toString());
			            	if(!parsedDataset.error){
				            	settings.edit().putString("netUsername", username.getText().toString()).commit();
				            	settings.edit().putString("netPassword", password.getText().toString()).commit();
				            	
				            	startActivity(new Intent(getBaseContext(),NetLobbyActivity.class));
				            	finish();
			            	}
			            	else{
			            		Looper.prepare();
			                	new AlertDialog.Builder(RegistrationActivity.this).setMessage(parsedDataset.getErrorMessage()).setNeutralButton(RegistrationActivity.this.getString(R.string.okay), null).show();
			                	Looper.loop();
			            	}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (ParserConfigurationException e) {
							e.printStackTrace();
						} catch (SAXException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
            	}).start();
            }
        });
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	if(!isOnline()){
    		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int which) {
        	        switch (which){
        	        case DialogInterface.BUTTON_POSITIVE:
        	            //Yes button clicked
        	        	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        	            break;
        	        case DialogInterface.BUTTON_NEGATIVE:
        	            //No button clicked
        	        	android.os.Process.killProcess(android.os.Process.myPid());
        	            break;
        	        }
        	    }
        	};

        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage(getApplicationContext().getString(R.string.cantConnect)).setPositiveButton(getApplicationContext().getString(R.string.yes), dialogClickListener).setNegativeButton(getApplicationContext().getString(R.string.no), dialogClickListener).setCancelable(false).show();
    	}
    }
    
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        boolean connected = false;
        try{
        	connected = cm.getActiveNetworkInfo().isConnected();
        }catch(NullPointerException e){
        	e.printStackTrace();
        }
        return connected;
    }
}