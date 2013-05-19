package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.sam.hex.GameAction;
import com.sam.hex.GameObject;
import com.sam.hex.PlayerObject;
import com.sam.hex.net.igGC.ParsedDataset;
import com.sam.hex.net.igGC.igGameCenter;

import android.graphics.Point;
import android.os.Handler;

/**
 * @author Will Harmon
 **/
public class NetPlayerObject extends PlayerObject {
    private MoveListener listener;
    private LinkedList<Point> hex = new LinkedList<Point>();
    private final igGameCenter igGC;
    
    public NetPlayerObject(int team, GameObject game, Handler handler, Runnable newgame) {
        super(team, game);
        this.igGC = new igGameCenter(NetGlobal.server, NetGlobal.uid, NetGlobal.session_id, NetGlobal.sid);
        this.listener = new MoveListener(game, team, handler, newgame, this, igGC);
    }

    @Override
    public void getPlayerTurn() {
        if(game.moveNumber>1 && !(GameAction.getPlayer((team%2+1),game) instanceof NetPlayerObject)){
            new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        ParsedDataset parsedDataset = igGC.move(GameAction.pointToString(new Point(game.moveList.getmove().getX(),game.moveList.getmove().getY()),game), NetGlobal.lasteid);
                        if(!parsedDataset.error){
                        }
                        else{
                            System.out.println(parsedDataset.getErrorMessage());
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        
        while (true) {
            while (hex.size()==0) {
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (hex.get(0).equals(new Point(-1,-1))){
                hex.remove(0);
                break;
            }
            else if (GameAction.makeMove(this, team, hex.get(0), game)) {
                hex.remove(0);
                break;
            }
            hex.remove(0);
        }
    }

    @Override
    public void undoCalled(){
    }

    @Override
    public void newgameCalled() {
        hex.add(new Point(-1,-1));
    }

    @Override
    public boolean supportsUndo() {
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    ParsedDataset parsedDataset = igGC.requestUndo((game.moveNumber-1), NetGlobal.lasteid);
                    if(parsedDataset.error){
                        System.out.println(parsedDataset.getErrorMessage());
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return false;
    }

    @Override
    public boolean supportsNewgame() {
        if(!game.gameOver){
            new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        ParsedDataset parsedDataset = igGC.quit(NetGlobal.lasteid);
                        if(parsedDataset.error){
                            System.out.println(parsedDataset.getErrorMessage());
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    ParsedDataset parsedDataset = igGC.rematch(NetGlobal.lasteid);
                    if(parsedDataset.error){
                        System.out.println(parsedDataset.getErrorMessage());
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return false;
    }

    @Override
    public void quit() {
        if(!game.gameOver){
            new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        ParsedDataset parsedDataset = igGC.quit(NetGlobal.lasteid);
                        if(parsedDataset.error){
                            System.out.println(parsedDataset.getErrorMessage());
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        listener.stop();
    }

    @Override
    public void win() {
    }

    @Override
    public void lose() {
        if(game.moveNumber>1 && !(GameAction.getPlayer((team%2+1),game) instanceof NetPlayerObject)){
            new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        ParsedDataset parsedDataset = igGC.move(GameAction.pointToString(new Point(game.moveList.getmove().getX(),game.moveList.getmove().getY()),game), NetGlobal.lasteid);
                        if(parsedDataset.error){
                            System.out.println(parsedDataset.getErrorMessage());
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public boolean supportsSave() {
        return false;
    }

    @Override
    public void endMove() {
        hex.clear();
        hex.add(new Point(-1,-1));
    }

    @Override
    public void setMove(Object o, Point hex) {
        if(o instanceof MoveListener) this.hex.add(hex);
    }

    @Override
    public boolean giveUp() {
        return listener.giveup;
    }
}