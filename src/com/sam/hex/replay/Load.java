package com.sam.hex.replay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import com.hex.core.Game;

/**
 * @author Will Harmon
 **/
public class Load {
    private final File file;

    public Load(File file) {
        this.file = file;
    }

    public Game run() {
        try {
            if(file != null) {
                String game = "";
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
                if(inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    game = stringBuilder.toString();
                }
                return Game.load(game);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
