package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sam.hex.GameAction;
import com.sam.hex.GameObject;
import com.sam.hex.PlayingEntity;

import android.graphics.Point;
import android.os.Handler;

/**
 * @author Will Harmon
 **/
public class NetPlayerObject implements PlayingEntity {
	private String name;
	private int color;
	private long timeLeft;
	private int team;
	private MoveListener listener;
	private LinkedList<Point> hex = new LinkedList<Point>();
	private GameObject game;
	private String server;
	private int uid;
	private String session_id;
	private int sid;
	
	public NetPlayerObject(int team, GameObject game, Handler handler, Runnable newgame) {
		this.team = team;
		this.game = game;
		this.server = NetGlobal.server;
		this.uid = NetGlobal.uid;
		this.session_id = NetGlobal.session_id;
		this.sid = NetGlobal.sid;
		this.listener = new MoveListener(game, team, handler, newgame, this, server, uid, session_id, sid);
	}

	@Override
	public void getPlayerTurn() {
		if(game.moveNumber>1 && !(GameAction.getPlayer((team%2+1),game) instanceof NetPlayerObject)){
			new Thread(new Runnable(){
	    		public void run(){
	    			try {
	    				String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=MOVE&move=%s&lasteid=%s", URLEncoder.encode(server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, URLEncoder.encode(GameAction.pointToString(new Point(game.moveList.getmove().getX(),game.moveList.getmove().getY()),game),"UTF-8"), NetGlobal.lasteid);
	    				URL url = new URL(lobbyUrl);
	    				SAXParserFactory spf = SAXParserFactory.newInstance();
	    	            SAXParser parser = spf.newSAXParser();
	    	            XMLReader reader = parser.getXMLReader();
	    	            XMLHandler xmlHandler = new XMLHandler();
	    	            reader.setContentHandler(xmlHandler);
	    	            reader.parse(new InputSource(url.openStream()));
	    	            
	    	            ParsedDataset parsedDataset = xmlHandler.getParsedData();
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
			else if (GameAction.makeMove(this, (byte) team, hex.get(0), game)) {
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
    		public void run(){
    			try {
    				String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=UNDO&type=ASK&move_ind=%s&lasteid=%s", URLEncoder.encode(server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, (game.moveNumber-1), NetGlobal.lasteid);
    				URL url = new URL(lobbyUrl);
    				SAXParserFactory spf = SAXParserFactory.newInstance();
    	            SAXParser parser = spf.newSAXParser();
    	            XMLReader reader = parser.getXMLReader();
    	            XMLHandler xmlHandler = new XMLHandler();
    	            reader.setContentHandler(xmlHandler);
    	            reader.parse(new InputSource(url.openStream()));
    	            
    	            ParsedDataset parsedDataset = xmlHandler.getParsedData();
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
		return false;
	}

	@Override
	public boolean supportsNewgame() {
		if(!game.gameOver){
			new Thread(new Runnable(){
	    		public void run(){
	    			try {
	    				String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=END&type=GIVEUP&lasteid=%s", URLEncoder.encode(server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, NetGlobal.lasteid);
	    				URL url = new URL(lobbyUrl);
	    				SAXParserFactory spf = SAXParserFactory.newInstance();
	    	            SAXParser parser = spf.newSAXParser();
	    	            XMLReader reader = parser.getXMLReader();
	    	            XMLHandler xmlHandler = new XMLHandler();
	    	            reader.setContentHandler(xmlHandler);
	    	            reader.parse(new InputSource(url.openStream()));
	    	            
	    	            ParsedDataset parsedDataset = xmlHandler.getParsedData();
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
		new Thread(new Runnable(){
    		public void run(){
    			try {
    				String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=RESTART&lasteid=%s", URLEncoder.encode(server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, NetGlobal.lasteid);
    				URL url = new URL(lobbyUrl);
    				SAXParserFactory spf = SAXParserFactory.newInstance();
    	            SAXParser parser = spf.newSAXParser();
    	            XMLReader reader = parser.getXMLReader();
    	            XMLHandler xmlHandler = new XMLHandler();
    	            reader.setContentHandler(xmlHandler);
    	            reader.parse(new InputSource(url.openStream()));
    	            
    	            ParsedDataset parsedDataset = xmlHandler.getParsedData();
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
		return false;
	}

	@Override
	public void quit() {
		if(!game.gameOver){
			new Thread(new Runnable(){
	    		public void run(){
	    			try {
	    				String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=END&type=GIVEUP&lasteid=%s", URLEncoder.encode(server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, NetGlobal.lasteid);
	    				URL url = new URL(lobbyUrl);
	    				SAXParserFactory spf = SAXParserFactory.newInstance();
	    	            SAXParser parser = spf.newSAXParser();
	    	            XMLReader reader = parser.getXMLReader();
	    	            XMLHandler xmlHandler = new XMLHandler();
	    	            reader.setContentHandler(xmlHandler);
	    	            reader.parse(new InputSource(url.openStream()));
	    	            
	    	            ParsedDataset parsedDataset = xmlHandler.getParsedData();
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
		listener.stop();
	}

	@Override
	public void win() {
	}

	@Override
	public void lose() {
		if(game.moveNumber>1 && !(GameAction.getPlayer((team%2+1),game) instanceof NetPlayerObject)){
			new Thread(new Runnable(){
	    		public void run(){
	    			try {
	    				String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=MOVE&move=%s&lasteid=%s", URLEncoder.encode(server, "UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, URLEncoder.encode(GameAction.pointToString(new Point(game.moveList.getmove().getX(),game.moveList.getmove().getY()),game),"UTF-8"), NetGlobal.lasteid);
	    				URL url = new URL(lobbyUrl);
	    				SAXParserFactory spf = SAXParserFactory.newInstance();
	    	            SAXParser parser = spf.newSAXParser();
	    	            XMLReader reader = parser.getXMLReader();
	    	            XMLHandler xmlHandler = new XMLHandler();
	    	            reader.setContentHandler(xmlHandler);
	    	            reader.parse(new InputSource(url.openStream()));
	    	            
	    	            ParsedDataset parsedDataset = xmlHandler.getParsedData();
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
	}

	@Override
	public boolean supportsSave() {
		return false;
	}

	@Override
	public void endMove() {
		hex.add(new Point(-1,-1));
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
	public void setMove(Object o, Point hex) {
		if(o instanceof MoveListener) this.hex.add(hex);
	}

	@Override
	public boolean giveUp() {
		return listener.giveup;
	}
}