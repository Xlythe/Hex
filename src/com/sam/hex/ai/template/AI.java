package com.sam.hex.ai.template;

import com.sam.hex.Game;
import com.sam.hex.PlayerObject;
import com.sam.hex.PlayingEntity;

public abstract class AI implements PlayingEntity {
    private static final long serialVersionUID = 1L;
    private String name;
    private int color;
    private long timeLeft;
    public final int team;
    private boolean skipMove = false;

    public AI(int team) {
        this.team = team;
    }

    @Override
    public void getPlayerTurn(Game game) {
        setSkipMove(false);
    }

    @Override
    public void undoCalled() {
        setSkipMove(true);
    }

    @Override
    public void newgameCalled() {
        endMove();
    }

    @Override
    public boolean supportsUndo(Game game) {
        if(team == 1) {
            return game.player2 instanceof PlayerObject;
        }
        else {
            return game.player1 instanceof PlayerObject;
        }
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
    public boolean supportsSave() {
        return false;
    }

    @Override
    public void endMove() {
        setSkipMove(true);
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
    public boolean giveUp() {
        return false;
    }

    public boolean getSkipMove() {
        return skipMove;
    }

    private void setSkipMove(boolean skipMove) {
        this.skipMove = skipMove;
    }
}