package com.sam.hex.replay;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import com.sam.hex.Game;
import com.sam.hex.Game.GameListener;
import com.sam.hex.Game.GameOptions;
import com.sam.hex.MoveList;
import com.sam.hex.PlayerObject;
import com.sam.hex.Timer;

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
                // Construct the ObjectInputStream object
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));

                int gridSize = (Integer) inputStream.readObject();
                boolean swap = (Boolean) inputStream.readObject();
                GameOptions go = new GameOptions();
                go.gridSize = gridSize;
                go.swap = swap;
                Game game = new Game(go, gl);

                game.player1 = new PlayerObject(1);
                game.player2 = new PlayerObject(2);

                game.player1Type = (Integer) inputStream.readObject();
                game.player2Type = (Integer) inputStream.readObject();
                game.player1.setColor((Integer) inputStream.readObject());
                game.player2.setColor((Integer) inputStream.readObject());
                game.player1.setName((String) inputStream.readObject());
                game.player2.setName((String) inputStream.readObject());
                game.moveList = (MoveList) inputStream.readObject();
                game.moveNumber = (Integer) inputStream.readObject();
                int timertype = (Integer) inputStream.readObject();
                long timerlength = (Long) inputStream.readObject();
                game.gameOptions.timer = new Timer(timerlength, 0, timertype);

                inputStream.close();

                game.currentPlayer = ((game.moveNumber + 1) % 2) + 1;
                game.replayRunning = false;

                // Does not support saving PlayingEntities yet
                game.player1Type = 0;
                game.player2Type = 0;

                return game;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}