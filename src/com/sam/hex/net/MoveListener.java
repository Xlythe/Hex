package com.sam.hex.net;

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

import android.content.DialogInterface;
import android.os.Handler;

import com.sam.hex.DialogBox;
import com.sam.hex.GameAction;
import com.sam.hex.GameObject;
import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class MoveListener implements Runnable{
	private boolean listen = true;
	private int team;
	private Handler handler;
	private Runnable newgame;
	private NetPlayerObject player;
	private String server;
	private int lasteid;
	private int uid;
	private String session_id;
	private int sid;
	private GameObject game;
	public boolean giveup = false;
	private boolean dontAskTwice = false;
	public MoveListener(GameObject game, int team, Handler handler, Runnable newgame, NetPlayerObject player, String server, int uid, String session_id, int sid){
		this.game = game;
		this.team = team;
		this.handler = handler;
		this.newgame = newgame;
		this.player = player;
		this.server = server;
		this.uid = uid;
		this.session_id = session_id;
		this.sid = sid;
		new Thread(this).start();
	}

	@Override
	public void run() {
		while(listen){
			try {
				String lobbyUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&lasteid=%s&cmd=REFRESH", URLEncoder.encode(server,"UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, lasteid);
				URL url = new URL(lobbyUrl);
				SAXParserFactory spf = SAXParserFactory.newInstance();
	            SAXParser parser = spf.newSAXParser();
	            XMLReader reader = parser.getXMLReader();
	            XMLHandler xmlHandler = new XMLHandler();
	            reader.setContentHandler(xmlHandler);
	            reader.parse(new InputSource(url.openStream()));
	            
	            final ParsedDataset parsedDataset = xmlHandler.getParsedData();
	        	if(!parsedDataset.error){
	        		if(lasteid!=0){
	        			for(int i=0;i<parsedDataset.messages.size();i++){
		        			WaitingRoomActivity.messages.add(parsedDataset.messages.get(i).name+": "+parsedDataset.messages.get(i).msg);
		        		}
	        		}
	        		if(parsedDataset.lasteid!=0){
	        			lasteid = parsedDataset.lasteid;
	        			NetGlobal.lasteid = parsedDataset.lasteid;
	        		}
        			NetGlobal.members = parsedDataset.players;
	        		for(int i=0;i<parsedDataset.players.size();i++){
	        			if(parsedDataset.players.get(i).place==1){
	        				game.player1.setTime(parsedDataset.players.get(i).timerLeft*1000);
	        			}
	        			else if(parsedDataset.players.get(i).place==2){
	        				game.player2.setTime(parsedDataset.players.get(i).timerLeft*1000);
	        			}
	        		}
	        		game.timer.totalTime = (game.player1.getTime() + game.player2.getTime())/2;
	        		game.timer.startTime = System.currentTimeMillis();
	        		if(team==1){
	        			if(parsedDataset.p1moves!=null)
		        			for(int i=0;i<parsedDataset.p1moves.size();i++)
		        				player.setMove(this, parsedDataset.p1moves.get(i));
	        			if(!(game.player2 instanceof NetPlayerObject))
	        				if(parsedDataset.p2moves!=null)
			        			for(int i=0;i<parsedDataset.p2moves.size();i++)
			        				game.player2.setMove(this, parsedDataset.p2moves.get(i));
	        		}
	        		else if(team==2){
	        			if(parsedDataset.p2moves!=null)
		        			for(int i=0;i<parsedDataset.p2moves.size();i++)
		        				player.setMove(this, parsedDataset.p2moves.get(i));
	        			if(!(game.player1 instanceof NetPlayerObject))
	        				if(parsedDataset.p1moves!=null)
			        			for(int i=0;i<parsedDataset.p1moves.size();i++)
			        				game.player1.setMove(this, parsedDataset.p1moves.get(i));
	        		}
        			if(parsedDataset.undoRequested){
        				try {
	    	        		String undoUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=UNDO&type=FORBID&lasteid=%s", URLEncoder.encode(server,"UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, NetGlobal.lasteid);
							new URL(undoUrl).openStream();
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
//        				new DialogBox(game.board.getContext(), 
//    	    					GameAction.insert(game.board.getContext().getString(R.string.LANUndo), player.getName()), 
//    	    					new DialogInterface.OnClickListener() {
//    	    	    	    	    public void onClick(DialogInterface dialog, int which) {
//    	    	    	    	        switch (which){
//    	    	    	    	        case DialogInterface.BUTTON_POSITIVE:
//    	    	    	    	            //Yes button clicked
//    	    	    		    			NetGlobal.undoRequested = true;
//    	    	    	    	        	GameAction.undo(NetGlobal.GAME_LOCATION,NetGlobal.game);
//    	    	    	    	        	try {
//    	    	    	    	        		String undoUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=UNDO&type=ACCEPT", URLEncoder.encode(server,"UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, lasteid);
//												new URL(undoUrl).openStream();
//											} catch (MalformedURLException e) {
//												e.printStackTrace();
//											} catch (IOException e) {
//												e.printStackTrace();
//											}
//    	    	    	    	            break;
//    	    	    	    	        case DialogInterface.BUTTON_NEGATIVE:
//    	    	    	    	            //No button clicked
//    	    	    	    	        	try {
//    	    	    	    	        		String undoUrl = String.format("http://%s.iggamecenter.com/api_handler.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&sid=%s&cmd=UNDO&type=DENY", URLEncoder.encode(server,"UTF-8"), NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), uid, URLEncoder.encode(session_id,"UTF-8"), sid, lasteid);
//												new URL(undoUrl).openStream();
//											} catch (MalformedURLException e) {
//												e.printStackTrace();
//											} catch (IOException e) {
//												e.printStackTrace();
//											}
//    	    	    	    	            break;
//    	    	    	    	        }
//    	    	    	    	    }
//    	    	    	    	}, 
//    	    					game.board.getContext().getString(R.string.yes), 
//    	    					game.board.getContext().getString(R.string.no));
        			}
    				if(parsedDataset.undoAccepted){
//    					GameAction.undo(NetGlobal.GAME_LOCATION,NetGlobal.game);
//	    				new DialogBox(game.board.getContext(), 
//		    					game.board.getContext().getString(R.string.LANundoAccepted), 
//		    					null, 
//		    					game.board.getContext().getString(R.string.okay));
    				}
    				if(team==1){
	    				if(parsedDataset.p1GaveUp){
	    					giveup = true;
		    	        	GameAction.getPlayer(game.currentPlayer, game).endMove();
	    				}
    				}
    				else if(team==2){
    					if(parsedDataset.p2GaveUp){
	    					giveup = true;
		    	        	GameAction.getPlayer(game.currentPlayer, game).endMove();
	    				}
    				}
    				if(parsedDataset.restart){
    					if(NetHexGame.justStart){
    						NetHexGame.justStart = false;
    						NetGlobal.sid = parsedDataset.getSid();
        					handler.post(newgame);
    					}
    					else if(!dontAskTwice){
    						dontAskTwice=true;
	    					new DialogBox(game.board.getContext(),
	    							GameAction.insert(game.board.getContext().getString(R.string.newLANGame), player.getName()),
	    	    					new DialogInterface.OnClickListener() {
	    	    	    	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	    	    	        switch (which){
	    	    	    	    	        case DialogInterface.BUTTON_POSITIVE:
	    	    	    	    	            //Yes button clicked
	    	    	    	    	        	NetGlobal.sid = parsedDataset.getSid();
	    	    	        					handler.post(newgame);
	    	    	    	    	            break;
	    	    	    	    	        case DialogInterface.BUTTON_NEGATIVE:
	    	    	    	    	            //No button clicked
	    	    	    	    	            break;
	    	    	    	    	        }
	    	    	    	    	    }
	    	    	    	    	},
	    	    	    	    	game.board.getContext().getString(R.string.yes), 
	    	    					game.board.getContext().getString(R.string.no));
    					}
    				}
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
			
			try {
				Thread.sleep(8000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stop(){
		listen = false;
	}
}