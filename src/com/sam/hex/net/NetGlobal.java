package com.sam.hex.net;

import java.util.ArrayList;
import com.sam.hex.GameObject;
import com.sam.hex.net.igGC.ParsedDataset;

/**
 * @author Will Harmon
 **/
public class NetGlobal{
	//Hex game id
	public static final int gid = 12;
	
	//Session information
	public static int uid;
	public static String session_id;
	
	//Match information
	public static int place;
	public static int gridSize;
	public static int sid;
	public static String server;
	public static int lasteid;
	public static int timerTime;
	public static int additionalTimerTime;
	public static boolean ratedGame;
	public static ArrayList<ParsedDataset.GameSession> sessions;
	public static ArrayList<ParsedDataset.Member> members = new ArrayList<ParsedDataset.Member>();
	public static boolean undoRequested = false;
	
	//Game object
	public static GameObject game;
	public static final int GAME_LOCATION = 2;
	
	//Unique identifier for each phone
	public static String android_id;
}