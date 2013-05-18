package com.sam.hex;

import java.util.LinkedList;

import android.graphics.Point;

public class PlayerObject implements PlayingEntity {
    private static final int SET = 0;
    private static final int GET = 1;
    private String name;
    private int color;
    private long timeLeft;
    public final int team;
    public final Game game;
    private final LinkedList<Point> hex = new LinkedList<Point>();

    public PlayerObject(int team, Game game) {
        this.team = team;
        this.game = game;
    }

    @Override
    public void getPlayerTurn() {
        if(hex(GET, null).size() > 0 && hex(GET, null).get(0).equals(new Point(-1, -1))) {
            hex(GET, null).clear();
        }
        while(true) {
            while(hex(GET, null).size() == 0) {
                try {
                    Thread.sleep(80);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(hex(GET, null).get(0).equals(new Point(-1, -1))) {
                hex(GET, null).remove(0);
                break;
            }
            if(GameAction.makeMove(this, team, hex(GET, null).get(0), game)) {
                hex(GET, null).remove(0);
                break;
            }
            hex(GET, null).remove(0);
        }
    }

    @Override
    public void undoCalled() {}

    @Override
    public void newgameCalled() {
        endMove();
    }

    @Override
    public boolean supportsUndo() {
        return true;
    }

    @Override
    public boolean supportsNewgame() {
        return true;
    }

    @Override
    public void quit() {
        endMove();
    }

    @Override
    public void win() {}

    @Override
    public void lose() {}

    @Override
    public boolean supportsSave() {
        return false;
    }

    @Override
    public void endMove() {
        hex(SET, new Point(-1, -1));
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setTime(long time) {
        this.timeLeft = time;
    }

    @Override
    public long getTime() {
        return timeLeft;
    }

    @Override
    public void setMove(final Object o, final Point point) {
        if(o instanceof GameAction && game.currentPlayer == team) hex(SET, point);
    }

    @Override
    public boolean giveUp() {
        return false;
    }

    private synchronized LinkedList<Point> hex(int type, Point point) {
        if(type == SET) {
            hex.clear();
            hex.add(point);
        }
        return hex;
    }
}