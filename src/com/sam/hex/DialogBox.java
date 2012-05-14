package com.sam.hex;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;

/**
 * @author Will Harmon
 **/
public class DialogBox implements Runnable{
	private static Handler handler;
	AlertDialog.Builder builder;
	public Thread thread;
	public DialogBox(Context context, String message, DialogInterface.OnClickListener clickListener, String positiveButtonText, String neutralButtonText, String negativeButtonText){
		builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_NoActionBar));
		builder.setMessage(message);
		builder.setPositiveButton(positiveButtonText, clickListener);
		builder.setNeutralButton(neutralButtonText, clickListener);
		builder.setNegativeButton(negativeButtonText, clickListener);
		
		thread = new Thread(this, "dialog"); //Create a new thread.
		thread.start();
	}
	public DialogBox(Context context, String message, DialogInterface.OnClickListener clickListener, String positiveButtonText, String neutralButtonText, String negativeButtonText, boolean cancelable){
		builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_NoActionBar));
		builder.setMessage(message);
		builder.setPositiveButton(positiveButtonText, clickListener);
		builder.setNeutralButton(neutralButtonText, clickListener);
		builder.setNegativeButton(negativeButtonText, clickListener);
		builder.setCancelable(cancelable);
		
		thread = new Thread(this, "dialog"); //Create a new thread.
		thread.start();
	}
	public DialogBox(Context context, String message, DialogInterface.OnClickListener clickListener, String positiveButtonText, String negativeButtonText){
		builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_NoActionBar));
		builder.setMessage(message);
		builder.setPositiveButton(positiveButtonText, clickListener);
		builder.setNegativeButton(negativeButtonText, clickListener);
		
		thread = new Thread(this, "dialog"); //Create a new thread.
		thread.start();
	}
	public DialogBox(Context context, String message, DialogInterface.OnClickListener clickListener, String positiveButtonText, String negativeButtonText, boolean cancelable){
		builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_NoActionBar));
		builder.setMessage(message);
		builder.setPositiveButton(positiveButtonText, clickListener);
		builder.setNegativeButton(negativeButtonText, clickListener);
		builder.setCancelable(cancelable);
		
		thread = new Thread(this, "dialog"); //Create a new thread.
		thread.start();
	}
	public DialogBox(Context context, String message, DialogInterface.OnClickListener clickListener, String neutralButtonText){
		builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_NoActionBar));
		builder.setMessage(message);
		builder.setNeutralButton(neutralButtonText, clickListener);
		
		thread = new Thread(this, "dialog"); //Create a new thread.
		thread.start();
	}
	
	@Override
	public void run() {
		if(handler!=null) handler.getLooper().quit();
		Looper.prepare();
		handler = new Handler();
		builder.show();
		Looper.loop();
	}
}