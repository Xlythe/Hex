package com.sam.hex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Locale;

import android.os.Environment;

import com.hex.core.Game;

public class FileUtil {
    public static Game loadGame(String fileName) throws IOException {
        File file = new File(fileName);
        StringBuilder text = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        br.close();
        return Game.load(text.toString());
    }

    public static void saveGame(String fileName, String gameState) throws StreamCorruptedException, FileNotFoundException, IOException {
        if(!fileName.toLowerCase(Locale.getDefault()).endsWith(".rhex")) {
            fileName = fileName + ".rhex";
        }
        fileName = Environment.getExternalStorageDirectory() + File.separator + "Hex" + File.separator + fileName;
        FileUtil.createDirIfNoneExists(File.separator + "Hex" + File.separator);

        File saveFile = new File(fileName);
        if(!saveFile.exists()) {
            saveFile.createNewFile();
        }
        // BufferedWriter for performance, true to set append to file flag
        BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile, true));
        buf.append(gameState);
        buf.close();
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
}
