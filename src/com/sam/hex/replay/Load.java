package com.sam.hex.replay;

import java.io.File;

import com.hex.core.Game;
import com.hex.core.Game.GameListener;

/**
 * @author Will Harmon
 **/
public class Load {
    private final File file;

    public Load(File file) {
        this.file = file;
    }

    public Game run(GameListener gl) {
        try {
            if(file != null) {
                return Game.load(file);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
