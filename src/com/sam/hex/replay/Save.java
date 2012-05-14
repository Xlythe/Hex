package com.sam.hex.replay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.text.InputType;
import android.widget.EditText;

import com.sam.hex.GameObject;
import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class Save{
	public static String fileName;
	private GameObject game;
	
	public Save(GameObject game){
		this.game = game;
	}
	
	private void saveGame(String fileName){
    	Thread saving = new Thread(new ThreadGroup("Save"), new save(), "saving", 200000);
		saving.start();
		try {
			saving.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		showSavedDialog(game.board.getContext().getString(R.string.saved));
	}
    
    class save implements Runnable{

		@Override
		public void run() {
			createDirIfNoneExists(File.separator + "Hex" + File.separator);
			String file = Environment.getExternalStorageDirectory() + File.separator + "Hex" + File.separator + fileName;
			if(file!=null){
				if(!file.toLowerCase().endsWith(".rhex")){
				    file = file + ".rhex";
				}
				try {
					ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));

					outputStream.writeObject(game.gridSize);
					outputStream.writeObject(game.swap);
					outputStream.writeObject(game.player1Type);
					outputStream.writeObject(game.player2Type);
					outputStream.writeObject(game.player1.getColor());
					outputStream.writeObject(game.player2.getColor());
					outputStream.writeObject(game.player1.getName());
					outputStream.writeObject(game.player2.getName());
					outputStream.writeObject(game.moveList);
					outputStream.writeObject(game.moveNumber);
					outputStream.writeObject(0);//Timer type
					outputStream.writeObject((game.timer.totalTime/60)/1000);
					
					outputStream.flush();
                    outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    	
    }
	
	public static boolean createDirIfNoneExists(String path) {
	    boolean ret = true;

	    File file = new File(Environment.getExternalStorageDirectory(), path);
	    if (!file.exists()) {
	        if (!file.mkdirs()) {
	            ret = false;
	        }
	    }
	    return ret;
	}
	
	public void showSavingDialog(){
        final EditText editText = new EditText(game.board.getContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        editText.setText(dateFormat.format(date) + "");
        AlertDialog.Builder builder = new AlertDialog.Builder(game.board.getContext());
        builder     
        .setTitle("Enter a filename")
        .setView(editText)
        .setPositiveButton("OK", new OnClickListener(){
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			fileName = editText.getText().toString();
    			saveGame(fileName);
    		}
        })
        .setNegativeButton(game.board.getContext().getString(R.string.cancel), null)
        .show();
    }
	
	private void showSavedDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(game.board.getContext());
        builder     
        .setTitle(message)
        .setNeutralButton(game.board.getContext().getString(R.string.okay), null)
        .show();
    }
}