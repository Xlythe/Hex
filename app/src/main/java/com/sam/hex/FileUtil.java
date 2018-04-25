package com.sam.hex;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import static com.sam.hex.Settings.TAG;

/**
 * @author Will Harmon
 **/
public class FileUtil {
    private static final String PATH = Environment.getExternalStorageDirectory() + File.separator + "Hex" + File.separator;

    public static String loadGameAsString(@NonNull String fileName) throws IOException {
        File file = new File(fileName);
        StringBuilder text = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }

        br.close();
        return text.toString();
    }

    public static void autoSaveGame(@NonNull String fileName, String gameState) throws IOException {
        if (!fileName.toLowerCase(Locale.getDefault()).endsWith(".rhex")) {
            fileName = fileName + ".rhex";
        }
        fileName = PATH + fileName;
        FileUtil.createDirIfNoneExists(PATH);

        File saveFile = new File(fileName);
        Log.d(TAG, "Attempting to create file " + fileName);
        if (!saveFile.exists()) {
            if (!saveFile.createNewFile()) {
                Log.w(TAG, "Failed to create file " + fileName);
            }
        }

        BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile, true));
        buf.append(gameState);
        buf.close();
        Log.d(TAG, "Successfully created file " + fileName);
    }

    private static void createDirIfNoneExists(@NonNull String path) {
        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.w(TAG, "Failed to create directory for " + path);
            }
        }
    }
}
