package com.sam.hex.replay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.text.InputType;
import android.widget.EditText;

import com.hex.core.Game;
import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class Save implements Runnable {
    public static String fileName;
    private final Game game;

    public Save(Game game) {
        this.game = game;
    }

    private void saveGame(String fileName, Context context) {
        run();
        showSavedDialog(context.getString(R.string.saved), context);
    }

    @Override
    public void run() {
        createDirIfNoneExists(File.separator + "Hex" + File.separator);
        String file = Environment.getExternalStorageDirectory() + File.separator + "Hex" + File.separator + fileName;
        if(file != null) {
            if(!file.toLowerCase(Locale.getDefault()).endsWith(".rhex")) {
                file = file + ".rhex";
            }
            try {
                OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(file));
                outputStream.write(game.save());
                outputStream.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean createDirIfNoneExists(String path) {
        boolean ret = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
        if(!file.exists()) {
            if(!file.mkdirs()) {
                ret = false;
            }
        }
        return ret;
    }

    public void showSavingDialog(final Context context) {
        final EditText editText = new EditText(context);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault());
        editText.setText(dateFormat.format(date));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter a filename").setView(editText).setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileName = editText.getText().toString();
                saveGame(fileName, context);
            }
        }).setNegativeButton(context.getString(R.string.cancel), null).show();
    }

    private void showSavedDialog(String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(message).setNeutralButton(android.R.string.ok, null).show();
    }
}
