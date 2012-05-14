package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sam.hex.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
        final Calendar cal = Calendar.getInstance();
        final DatePicker birth = (DatePicker) findViewById(R.id.birthday);
        final EditText about = (EditText) findViewById(R.id.about);
        enter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							int birthDay = birth.getDayOfMonth();
					        int birthMonth = birth.getMonth();
					        int birthYear = birth.getYear();
		            		String registrationUrl = String.format("http://www.iggamecenter.com/api_user_add.php?app_id=%s&app_code=%s&name=%s&password=%s", NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), URLEncoder.encode(username.getText().toString(),"UTF-8"), URLEncoder.encode(password.getText().toString(),"UTF-8"));
		            		if(!email.equals("")) registrationUrl += "&email="+URLEncoder.encode(email.getText().toString(),"UTF-8");
		            		if(birthYear!=cal.get(Calendar.YEAR) && birthMonth!=cal.get(Calendar.MONTH) && birthDay!=cal.get(Calendar.DAY_OF_MONTH)) registrationUrl += "&birthDay="+birthDay+"&birthMonth="+birthMonth+"&birthYear="+birthYear;
		            		if(!about.equals("")) registrationUrl += "&about="+URLEncoder.encode(about.getText().toString(),"UTF-8");
							URL url = new URL(registrationUrl);
							SAXParserFactory spf = SAXParserFactory.newInstance();
			                SAXParser parser = spf.newSAXParser();
			                XMLReader reader = parser.getXMLReader();
			                XMLHandler handler = new XMLHandler();
			                reader.setContentHandler(handler);
			                reader.parse(new InputSource(url.openStream()));
			                
			                ParsedDataset parsedDataset = handler.getParsedData();
			            	if(!parsedDataset.error){
				            	settings.edit().putString("netUsername", username.getText().toString()).commit();
				            	settings.edit().putString("netPassword", password.getText().toString()).commit();
				            	
				            	startActivity(new Intent(getBaseContext(),NetLobbyActivity.class));
				            	finish();
			            	}
			            	else{
			                	new AlertDialog.Builder(RegistrationActivity.this).setMessage(parsedDataset.getErrorMessage()).setNeutralButton(RegistrationActivity.this.getString(R.string.okay), null).show();
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
        	builder.setMessage(getApplicationContext().getString(R.string.cantConnect)).setPositiveButton(getApplicationContext().getString(R.string.yes), dialogClickListener).setNegativeButton(getApplicationContext().getString(R.string.no), dialogClickListener).show();
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