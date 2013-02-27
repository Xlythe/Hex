package com.sam.hex.ai.template;

import com.sam.hex.GameObject;
import com.sam.hex.PlayerObject;
import com.sam.hex.PlayingEntity;

import android.graphics.Point;

public class AI implements PlayingEntity {
    private String name;
    private int color;
    private long timeLeft;
    public final int team;
    public final GameObject game;
    private boolean skipMove = false;
    
    public AI(int team, GameObject game) {
        this.team=team;
        this.game=game;
    }
    
    @Override
    public void getPlayerTurn() {
        setSkipMove(false);
    }
    
    @Override
    public void undoCalled(){
        setSkipMove(true);
    }
    
    @Override
    public void newgameCalled() {
        endMove();
    }

    @Override
    public boolean supportsUndo() {
        if(team==1){
            return game.player2 instanceof PlayerObject;
        }
        else{
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
    public void win() {
    }

    @Override
    public void lose() {
    }

    @Override
    public boolean supportsSave() {
//        if(team==1){
//            return game.player2 instanceof PlayerObject;
//        }
//        else{
//            return game.player1 instanceof PlayerObject;
//        }
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

    @Override
    public void setMove(Object o, Point hex) {
    }

    public boolean getSkipMove() {
        return skipMove;
    }

    private void setSkipMove(boolean skipMove) {
        this.skipMove = skipMove;
    }
}