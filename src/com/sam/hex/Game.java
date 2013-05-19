package com.sam.hex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.os.Handler;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Game implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;
    private boolean gameRunning = true;
    public final RegularPolygonGameObject[][] gamePiece;
    public int moveNumber;
    public MoveList moveList;
    public int currentPlayer;
    public boolean gameOver = false;
    public PlayingEntity player1;
    public PlayingEntity player2;
    public int player1Type;
    public int player2Type;
    public long moveStart;
    public boolean replayRunning = false;
    public transient GameListener gameListener;
    public GameOptions gameOptions;

    public Game(GameOptions gameOptions, GameListener gameListener) {
        this.gameOptions = gameOptions;
        this.gameListener = gameListener;

        gamePiece = new RegularPolygonGameObject[gameOptions.gridSize][gameOptions.gridSize];
        for(int i = 0; i < gameOptions.gridSize; i++) {
            for(int j = 0; j < gameOptions.gridSize; j++) {
                gamePiece[i][j] = new RegularPolygonGameObject();
            }
        }

        moveNumber = 1;
        moveList = new MoveList();
        currentPlayer = 1;
        gameRunning = true;
        gameOver = false;
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.writeObject(gameOptions.gridSize);
        outputStream.writeObject(gameOptions.swap);
        outputStream.writeObject(player1Type);
        outputStream.writeObject(player2Type);
        outputStream.writeObject(player1.getColor());
        outputStream.writeObject(player2.getColor());
        outputStream.writeObject(player1.getName());
        outputStream.writeObject(player2.getName());
        outputStream.writeObject(moveList);
        outputStream.writeObject(moveNumber);
        outputStream.writeObject(0);// Timer type
        outputStream.writeObject((gameOptions.timer.totalTime / 60) / 1000);
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        int gridSize = (Integer) inputStream.readObject();
        boolean swap = (Boolean) inputStream.readObject();
        GameOptions go = new GameOptions();
        go.gridSize = gridSize;
        go.swap = swap;

        player1 = new PlayerObject(1);
        player2 = new PlayerObject(2);

        player1Type = (Integer) inputStream.readObject();
        player2Type = (Integer) inputStream.readObject();
        player1.setColor((Integer) inputStream.readObject());
        player2.setColor((Integer) inputStream.readObject());
        player1.setName((String) inputStream.readObject());
        player2.setName((String) inputStream.readObject());
        moveList = (MoveList) inputStream.readObject();
        moveNumber = (Integer) inputStream.readObject();
        int timertype = (Integer) inputStream.readObject();
        long timerlength = (Long) inputStream.readObject();
        gameOptions.timer = new Timer(timerlength, 0, timertype);

        inputStream.close();

        currentPlayer = ((moveNumber + 1) % 2) + 1;
        replayRunning = false;

        // Does not support saving PlayingEntities yet
        player1Type = 0;
        player2Type = 0;
    }

    public void start() {
        gameListener.onStart();
        gameOver = false;
        gameRunning = true;
        player1.setTime(gameOptions.timer.totalTime);
        player2.setTime(gameOptions.timer.totalTime);
        gameOptions.timer.start(this);
        new Thread(this, "runningGame").start();
    }

    public void stop() {
        gameListener.onStop();
        gameRunning = false;
        gameOptions.timer.stop();
        player1.quit();
        player2.quit();
        gameOver = true;
    }

    @Override
    public void run() {
        PlayingEntity player;

        // Loop the game
        gameListener.onTurn(player1);
        while(gameRunning) {
            if(!checkForWinner()) {
                moveStart = System.currentTimeMillis();
                player = GameAction.getPlayer(currentPlayer, this);

                // Let the player make its move
                player.getPlayerTurn(this);

                // Update the timer
                if(gameOptions.timer.type == 1) {
                    gameOptions.timer.startTime = System.currentTimeMillis();
                    player.setTime(gameOptions.timer.totalTime);
                }
                player.setTime(player.getTime() + gameOptions.timer.additionalTime);

                gameListener.onTurn(player);
            }

            currentPlayer = (currentPlayer % 2) + 1;
        }
    }

    private boolean checkForWinner() {
        GameAction.checkedFlagReset(this);
        if(GameAction.checkWinPlayer(1, this)) {
            gameRunning = false;
            gameOver = true;
            player1.win();
            player2.lose();
            gameListener.onWin(player1);
        }
        else if(GameAction.checkWinPlayer(2, this)) {
            gameRunning = false;
            gameOver = true;
            player1.lose();
            player2.win();
            gameListener.onWin(player2);
        }

        return gameOver;
    }

    public void clearBoard() {
        for(int i = 0; i < gameOptions.gridSize; i++) {
            for(int j = 0; j < gameOptions.gridSize; j++) {
                gamePiece[i][j] = new RegularPolygonGameObject();
            }
        }
        gameListener.onClear();
    }

    public class Views {
        public BoardView board;
        public TextView timerText;
        public TextView winnerText;
        public ImageButton replayForward;
        public ImageButton replayPlayPause;
        public ImageButton replayBack;
        public RelativeLayout replayButtons;
        public ImageButton player1Icon;
        public ImageButton player2Icon;
        public Handler handler;
    }

    public static class GameOptions implements Serializable {
        private static final long serialVersionUID = 1L;
        public Timer timer;
        public int gridSize;
        public boolean swap;
    }

    public static interface GameListener extends Serializable {
        public void onWin(PlayingEntity player);

        public void onClear();

        public void onStart();

        public void onStop();

        public void onTurn(PlayingEntity player);

        public void onReplay();

        public void onTeamSet();

        public void onUndo();

        public void startTimer();

        public void displayTime(int minutes, int seconds);
    }
}