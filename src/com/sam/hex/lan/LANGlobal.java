package com.sam.hex.lan;

import java.util.ArrayList;

import com.sam.hex.GameObject;

/**
 * @author Will Harmon
 **/
public class LANGlobal {
	public static ArrayList<LocalNetworkObject> localObjects = new ArrayList<LocalNetworkObject>();
	public static LocalNetworkObject localPlayer = new LocalNetworkObject();
	public static String LANipAddress;
	public final static String MULTICASTADDRESS = "234.235.236.237"; 
	
	public final static int MULTICASTPORT = 4080;
	public final static int PLAYERPORT = 4081;
	public final static int CHALLENGERPORT = 4082;
	
	/**
	 * A flag for undo. Checked if undo originated on the other phone
	 * */
	public static boolean undoRequested = false;
	
	public static String playerName;
	public static int playerColor;
	public static int gridSize;
	
	public static int undoNumber = 0;
	
	public static final int GAME_LOCATION = 1;
	public static GameObject game;
}