package com.sam.hex.net.igGC;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
 
/**
 * @author Will Harmon
 **/
public class igGameCenter {
	//Our ids to connect to igGC
	private static final int id = 17;
	private static final String passcode = "wihamo8984";
	
	private String server;
	private int uid;
	private String session_id;
	private int sid;
	
	public igGameCenter(String server, int uid, String session_id, int sid){
		this.server = server;
		this.uid = uid;
		this.session_id = session_id;
		this.sid = sid;
	}
	
	public ParsedDataset refresh(long lasteid) throws ParserConfigurationException, SAXException, IOException{
		String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&lasteid=%s&cmd=REFRESH", URLEncoder.encode(server,"UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, lasteid);
		URL url = new URL(lobbyUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public static ParsedDataset refresh(String server, int uid, String session_id, int sid, long lasteid) throws ParserConfigurationException, SAXException, IOException{
		igGameCenter igGC = new igGameCenter(server, uid, session_id, sid);
		return igGC.refresh(lasteid);
	}
	
	public void forbidUndo(long lasteid) throws MalformedURLException, IOException{
		String undoUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=UNDO&type=FORBID&lasteid=%s", URLEncoder.encode(server,"UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, lasteid);
		new URL(undoUrl).openStream();
	}
	
	public ParsedDataset move(String move, long lasteid) throws IOException, SAXException, ParserConfigurationException{
		String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=MOVE&move=%s&lasteid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, URLEncoder.encode(move,"UTF-8"), lasteid);
		URL url = new URL(lobbyUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public ParsedDataset requestUndo(int moveNumber, long lasteid) throws IOException, SAXException, ParserConfigurationException{
		String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=UNDO&type=ASK&move_ind=%s&lasteid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, moveNumber, lasteid);
		URL url = new URL(lobbyUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public ParsedDataset quit(long lasteid) throws IOException, SAXException, ParserConfigurationException{
		String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=END&type=GIVEUP&lasteid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, lasteid);
		URL url = new URL(lobbyUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public ParsedDataset rematch(long lasteid) throws ParserConfigurationException, SAXException, IOException{
		String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=RESTART&lasteid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, lasteid);
		URL url = new URL(lobbyUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public static ParsedDataset login(String username, String password, String androidId) throws ParserConfigurationException, SAXException, IOException{
		String loginUrl = String.format("http://www.iggamecenter.com/api_login.php?app_id=%s&app_code=%s&login=%s&password=%s&networkuid=%s&md5=1", id, URLEncoder.encode(passcode,"UTF-8"), URLEncoder.encode(username,"UTF-8"), URLEncoder.encode(password,"UTF-8"), URLEncoder.encode(androidId,"UTF-8"));
		URL url = new URL(loginUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public static ParsedDataset joinGame(String server, int uid, String session_id, int sid) throws IOException, SAXException, ParserConfigurationException{
		String registrationUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid);
		URL url = new URL(registrationUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser parser = spf.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		XMLHandler xmlHandler = new XMLHandler();
		reader.setContentHandler(xmlHandler);
		reader.parse(new InputSource(url.openStream()));

		return xmlHandler.getParsedData();
	}
	
	public static ParsedDataset claimVictory(String server, int uid, String session_id, int sid, long lasteid) throws ParserConfigurationException, SAXException, IOException{
		String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=end&type=CLAIMQUIT&lasteid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, lasteid);
		URL url = new URL(lobbyUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public static ParsedDataset register(String username, String password, String email) throws IOException, SAXException, ParserConfigurationException{
		String registrationUrl = String.format("http://www.iggamecenter.com/api_user_add.php?app_id=%s&app_code=%s&name=%s&password=%s", igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), URLEncoder.encode(username,"UTF-8"), URLEncoder.encode(password,"UTF-8"));
		if(!email.isEmpty()) registrationUrl += "&email="+URLEncoder.encode(email,"UTF-8");
		URL url = new URL(registrationUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler handler = new XMLHandler();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(url.openStream()));
        
        return handler.getParsedData();
	}
	
	public static ParsedDataset refreshLobby(int uid, String session_id, int gid) throws IOException, SAXException, ParserConfigurationException{
		String lobbyUrl = String.format("http://www.iggamecenter.com/api_board_list.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&gid=%s", igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), gid);
		URL url = new URL(lobbyUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public static ParsedDataset ready(String server, int uid, String session_id, int sid, long lasteid) throws IOException, SAXException, ParserConfigurationException{
		String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=START&lasteid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, lasteid);
		URL url = new URL(lobbyUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public static ParsedDataset createBoard(int uid, String session_id, int gid, int place) throws ParserConfigurationException, SAXException, IOException{
		String registrationUrl = String.format("http://www.iggamecenter.com/api_board_create.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&gid=%s&place=%s", igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), gid, place);
		URL url = new URL(registrationUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser parser = spf.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		XMLHandler xmlHandler = new XMLHandler();
		reader.setContentHandler(xmlHandler);
		reader.parse(new InputSource(url.openStream()));

		return xmlHandler.getParsedData();
	}
	
	public static void editBoard(String server, int uid, String session_id, int sid, int gridSize, long timerTime, long additionalTimerTime, int scored, long lasteid) throws MalformedURLException, IOException{
		String boardUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=SETUP&boardSize=%s&timerTotal=%s&timerInc=%s&scored=%s&lasteid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, gridSize, timerTime*60, additionalTimerTime, scored, lasteid);
		new URL(boardUrl).openStream();
	}
	
	public static ParsedDataset sendMessage(String server, int uid, String session_id, int sid, String message, long lasteid) throws IOException, SAXException, ParserConfigurationException{
		String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=MSG&message=%s&lasteid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, URLEncoder.encode(message,"UTF-8"), lasteid);
		URL url = new URL(lobbyUrl);
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        reader.setContentHandler(xmlHandler);
        reader.parse(new InputSource(url.openStream()));
        
        return xmlHandler.getParsedData();
	}
	
	public static void changePlace(String server, int uid, String session_id, int sid, int place, long lasteid) throws MalformedURLException, IOException{
		String placeUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=PLACE&place=%s&lasteid=%s", URLEncoder.encode(server, "UTF-8"), igGameCenter.id, URLEncoder.encode(igGameCenter.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, place, lasteid);
		new URL(placeUrl).openStream();
	}
}