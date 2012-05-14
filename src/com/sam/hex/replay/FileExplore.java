package com.sam.hex.replay;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.sam.hex.DialogBox;
import com.sam.hex.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileExplore extends Activity {

	// Stores names of traversed directories
	ArrayList<String> str = new ArrayList<String>();
	
	// Check if the first level of the directory structure is the one showing
	private Boolean firstLvl = true;
	
	private static final String TAG = "F_PATH";
	
	private Item[] fileList;
	public static File path = new File(Environment.getExternalStorageDirectory() + File.separator + "Hex" + File.separator);
	public static String chosenFile;
	
	ListAdapter adapter;
	ListView view;
	Handler handle;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handle = new Handler();
		try {
			loadFileList();
			view = new ListView(this);
			refreshView();
			
			setContentView(view);
		} catch(NullPointerException e){
			e.printStackTrace();
		}
		
	}
	
	private void loadFileList() {
		try {
			path.mkdirs();
		} catch (SecurityException e) {
			Log.e(TAG, "unable to write on the sd card ");
		}
		
		// Checks whether path exists
		if (path.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					// Filters based on whether the file is hidden or not
					return (sel.isFile() || sel.isDirectory())
					&& !sel.isHidden();
				
				}
			};
			
			String[] fList = path.list(filter);
			fileList = new Item[fList.length];
			for (int i = 0; i < fList.length; i++) {
				fileList[i] = new Item(fList[i], R.drawable.file_icon);
				
				// Convert into file path
				File sel = new File(path, fList[i]);
				
				// Set drawables
				if (sel.isDirectory()) {
					fileList[i].icon = R.drawable.directory_icon;
					Log.d("DIRECTORY", fileList[i].file);
				} else {
					Log.d("FILE", fileList[i].file);
				}
			}
			
			if (!firstLvl) {
				Item temp[] = new Item[fileList.length + 1];
				for (int i = 0; i < fileList.length; i++) {
				temp[i + 1] = fileList[i];
				}
				temp[0] = new Item("Up", R.drawable.directory_up);
				fileList = temp;
			}
		} else {
			Log.e(TAG, "path does not exist");
		}
		
		adapter = new ArrayAdapter<Item>(this,
		android.R.layout.select_dialog_item, android.R.id.text1,
		fileList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// creates view
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view
				.findViewById(android.R.id.text1);
				
				// put the image on the text view
				textView.setCompoundDrawablesWithIntrinsicBounds(
				fileList[position].icon, 0, 0, 0);
				
				// add margin between image and text (support various screen
				// densities)
				int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
				textView.setCompoundDrawablePadding(dp5);
				
				return view;
			}
		};
	
	}
	
	private class Item {
		public String file;
		public int icon;
		
		public Item(String file, Integer icon) {
			this.file = file;
			this.icon = icon;
		}
		
		@Override
		public String toString() {
			return file;
		}
	}
	
	private void refreshView(){
		view.setAdapter(adapter);
		view.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id){
					chosenFile = fileList[position].file;
					File sel = new File(path + "/" + chosenFile);
					if (sel.isDirectory()) {
						firstLvl = false;
						
						// Adds chosen directory to list
						str.add(chosenFile);
						fileList = null;
						path = new File(sel + "");
						
						loadFileList();
						
						refreshView();
					}
					
					// Checks if 'up' was clicked
					else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {
						
						// present directory removed from list
						String s = str.remove(str.size() - 1);
						
						// path modified to exclude present directory
						path = new File(path.toString().substring(0,
						path.toString().lastIndexOf(s)));
						fileList = null;
						
						// if there are no more directories in the list, then
						// its the first level
						if (str.isEmpty()) {
							firstLvl = true;
						}
						loadFileList();
						refreshView();
					}
					// File picked
					else {
						new DialogBox(FileExplore.this, chosenFile, new DialogInterface.OnClickListener() {
	    	    	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	    	        switch (which){
	    	    	    	        case DialogInterface.BUTTON_POSITIVE:
	    	    	    	        	Thread loading = new Thread(new ThreadGroup("Load"), new Load(new File(FileExplore.path + File.separator + FileExplore.chosenFile)), "loading", 200000);
	    	    						loading.start();
	    	    						try {
	    	    							loading.join();
	    	    						} catch (InterruptedException e) {
	    	    							e.printStackTrace();
	    	    						}
	    	    			        	finish();
	    	    	    	            break;
	    	    	    	        case DialogInterface.BUTTON_NEGATIVE:
	    	    	    	        	new File(path + "/" + chosenFile).delete();
		    	    	    	    	loadFileList();
		    							handle.post(new Runnable(){
		    								public void run(){
		    									loadFileList();
		    									refreshView();
		    								}
		    							});
	    	    	    	            break;
		    	    	    	    case DialogInterface.BUTTON_NEUTRAL:
		    	    	    	    	final EditText editText = new EditText(FileExplore.this);
		    	    	    	        editText.setInputType(InputType.TYPE_CLASS_TEXT);
		    	    	    	        AlertDialog.Builder builder = new AlertDialog.Builder(FileExplore.this);
		    	    	    	        builder     
		    	    	    	        .setTitle(chosenFile)
		    	    	    	        .setView(editText)
		    	    	    	        .setPositiveButton(getApplicationContext().getString(R.string.okay), new OnClickListener(){
		    	    	    	    		@Override
		    	    	    	    		public void onClick(DialogInterface dialog, int which) {
		    	    	    	    			if(!editText.getText().toString().equals("")){
		    	    	    	    				String fileName = editText.getText().toString();
		    	    	    	        			if(!fileName.toLowerCase().endsWith(".rhex")){
		    	    	    	        			    fileName += ".rhex";
		    	    	    	        			}
		    	    	    	        			new File(path + "/" + chosenFile).renameTo(new File(path + "/" + fileName));
		    	    	    	        			handle.post(new Runnable(){
		    		    								public void run(){
		    		    									loadFileList();
		    		    									refreshView();
		    		    								}
		    		    							});
		    	    	    	    			}
		    	    	    	    		}
		    	    	    	        })
		    	    	    	        .setNegativeButton(getApplicationContext().getString(R.string.cancel), null)
		    	    	    	        .show();
	    	    	    	            break;
	    	    	    	        }
	    	    	    	    }
	    	    	    	}, getApplicationContext().getString(R.string.loadReplay), getApplicationContext().getString(R.string.rename), getApplicationContext().getString(R.string.delete));
					}
				}});
	}
}