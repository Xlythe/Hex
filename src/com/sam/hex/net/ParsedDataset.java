package com.sam.hex.net;

import java.util.ArrayList;
import java.util.LinkedList;

import android.graphics.Point;
 
/**
 * @author Will Harmon
 **/
public class ParsedDataset {
	//Logging in
    private int uid = 0;
    private String name = null;
    private String session_id = null;
    
    //Current games
    public ArrayList<GameSession> sessions = new ArrayList<GameSession>();
    public class GameSession{
    	public ArrayList<Member> members = new ArrayList<Member>();
    	public String state;
    	public int sid;
    	public int uid;
    	public String server;
    	public GameSession(String state, int sid, int uid, String server){
        	this.state = state;
        	this.sid = sid;
        	this.uid = uid;
        	this.server = server;
    	}
    	public String toString(){
    		String str = "";
    		for(int i=0;i<members.size();i++){
    			if(members.get(i).place==1){
    				str+="Player 1: "+members.get(i).name;
    			}
    		}
    		str+="\n";
    		for(int i=0;i<members.size();i++){
    			if(members.get(i).place==2){
    				str+="Player 2: "+members.get(i).name;
    			}
    		}
    		if(!str.equals("\n")) return str;
    		else{
    			str="";
    			if(members.size()>0) str+="Spectating: "+members.get(0).name;
    			for(int i=1;i<members.size();i++){
        			str+=", "+members.get(i).name;
        		}
    			return str;
    		}
    	}
    }
    public class Member{
    	public int place;
    	public int uid;
    	public String name;
    	public String state;
    	public int timerLeft;
    	public long lastRefresh;
    	public Member(int place, int uid, String name, String state, int timerLeft, long lastRefresh){
    		this.place = place;
        	this.uid = uid;
        	this.name = name;
        	this.state = state;
        	this.timerLeft = timerLeft;
        	this.lastRefresh = lastRefresh;
    	}
    	public String toString(){
    		return name;
    	}
    }
    public void addSession(String state, int sid, int uid, String server){
    	sessions.add(new GameSession(state, sid, uid, server));
    }
    public void addSessionMember(int position, int uid, String name, String state, int timerLeft, long lastRefresh){
    	sessions.get(sessions.size()-1).members.add(new Member(position, uid, name, state, timerLeft, lastRefresh));
    }
    
    //Playing a game
    public boolean gameActive = false;
    public int lasteid;
    private int sid;
    private String server = null;
    public ArrayList<Member> players = new ArrayList<Member>();
    public void addPlayer(int position, int uid, String name, String state, int timerLeft, long lastRefresh){
    	players.add(new Member(position, uid, name, state, timerLeft, lastRefresh));
    }
    public ArrayList<Message> messages = new ArrayList<Message>();
    public class Message{
    	public String msg;
    	public int uid;
    	public String name;
    	public Message(String msg, int uid, String name){
    		this.msg = msg;
    		this.uid = uid;
    		this.name = name;
    	}
    }
    public void addMessage(String msg, int uid, String name){
    	messages.add(new Message(msg, uid, name));
    }
    public LinkedList<Point> p1moves = new LinkedList<Point>();
    public LinkedList<Point> p2moves = new LinkedList<Point>();
    public boolean undoRequested = false;
    public boolean undoAccepted = false;
    public boolean restart = false;
    public boolean p1GaveUp = false;
    public boolean p2GaveUp = false;
    public boolean optionsChanged = false;
    
    //Errors
    public boolean error = false;
    private String errorMessage = null;
 
    public int getUid() {
    	return uid;
    }
    
    public void setUid(int uid) {
    	this.uid = uid;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSession_id() {
		return session_id;
	}

	public void setSession_id(String session_id) {
		this.session_id = session_id;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
}