package com.sam.hex.replay;

import java.io.File;
import java.io.FileInputStream;
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
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
                String game = (String) inputStream.readObject();
                inputStream.close();
                return Game.load(game);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
