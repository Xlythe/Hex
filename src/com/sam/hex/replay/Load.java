package com.sam.hex.replay;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import com.sam.hex.GameObject;
import com.sam.hex.HexGame;
import com.sam.hex.MoveList;
import com.sam.hex.PlayerObject;
import com.sam.hex.Timer;

/**
 * @author Will Harmon
 **/
public class Load implements Runnable {
    private File file;
    private GameObject game;

    public Load(File file, GameObject game) {
        this.file = file;
        this.game = game;
    }

    @Override
    public void run() {
        try {
            HexGame.stopGame(game);
            if(file != null) {
                // Construct the ObjectInputStream object
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));

                int gridSize = (Integer) inputStream.readObject();
                boolean swap = (Boolean) inputStream.readObject();

                game = new GameObject(gridSize, swap);

                game.player1 = new PlayerObject(1, game);
                game.player2 = new PlayerObject(2, game);

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
                game.timer = new Timer(game, timerlength, 0, timertype);

                inputStream.close();

                game.currentPlayer = ((game.moveNumber + 1) % 2) + 1;
                HexGame.replay = true;
                if(game.views.board != null) game.views.board.replayRunning = false;
                HexGame.startNewGame = false;

                // Does not support saving PlayingEntities yet
                game.player1Type = 0;
                game.player2Type = 0;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
