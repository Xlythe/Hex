package com.sam.hex.net;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sam.hex.GameAction;
     
/**
 * @author Will Harmon
 **/
public class XMLHandler extends DefaultHandler{
	private boolean in_errorMessage = false;
	private boolean in_loginResult = false;
    private boolean in_uid = false;
    private boolean in_name = false;
    private boolean in_session_id = false;
    private boolean in_sessionList = false;
    private boolean in_session = false;
    private boolean in_sessionInfo = false;
    private boolean in_sid = false;
    private boolean in_server = false;
    private boolean in_handlerData = false;
    private boolean in_playerList = false;
    private boolean in_guestList = false;
    private boolean in_eventList = false;
    private boolean in_gameOptions = false;
    private boolean in_boardSize = false;
    private boolean in_timerTotal = false;
    private boolean in_timerInc = false;
    private boolean in_scored = false;
    private long currentTime = 0L;
       
    private ParsedDataset parsedDataset = new ParsedDataset();
 
    public ParsedDataset getParsedData() {
    	return this.parsedDataset;
    }
    
    @Override
    public void startDocument() throws SAXException {
    	this.parsedDataset = new ParsedDataset();
    }
 
    @Override
    public void endDocument() throws SAXException {
    	// Nothing to do
    }
 
    /** Gets be called on opening tags like:
     * <tag>
     * Can provide attribute(s), when xml is like:
     * <tag attribute="attributeValue">*/
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    	if(localName.equals("errorMessage")) {
    		this.in_errorMessage = true;
    		parsedDataset.error = true;
        }
    	else if(localName.equals("loginResult")) {
    		this.in_loginResult = true;
        }
    	else if(localName.equals("uid")) {
    		this.in_uid = true;
        }
    	else if(localName.equals("name")) {
        	this.in_name = true;
        }
    	else if(localName.equals("session_id")){
    		this.in_session_id = true;
    	}
    	//For list of available games
    	else if(localName.equals("sessionList")){
    		this.in_sessionList = true;
    	}
    	else if(in_sessionList){
	    	if(localName.equals("session")){
	    		if((atts.getValue("stat").equals("ACTIVE") || atts.getValue("stat").equals("INIT")) && atts.getValue("priv").equals("0")){
	    			parsedDataset.addSession(atts.getValue("stat"), Integer.parseInt(atts.getValue("sid")), Integer.parseInt(atts.getValue("uid")), atts.getValue("serv"));
		    		this.in_session = true;
	    		}
	    	}
	    	if(in_session){
		    	if(localName.equals("member")){
		    		parsedDataset.addSessionMember(Integer.parseInt(atts.getValue("plc")), Integer.parseInt(atts.getValue("uid")), atts.getValue("nam"), atts.getValue("stat"), 0, 0);
		    	}
	    	}
    	}
    	//For playing a game
    	else if(localName.equals("handlerData")){
    		this.in_handlerData = true;
    	}
    	else if(in_handlerData){
    		//Game status
    		if(localName.equals("sessionInfo")){
    			currentTime = Long.parseLong(atts.getValue("curtime"));
    			if(atts.getValue("status").equals("INIT")){
    				parsedDataset.gameActive = false;
    			}
    			else{
    				parsedDataset.gameActive = true;
    			}
    		}
    		//Players in game
    		if(localName.equals("playerList")){
    			this.in_playerList = true;
    		}
    		else if(in_playerList){
    			if(localName.equals("player")){
    				parsedDataset.addPlayer(Integer.parseInt(atts.getValue("place")), Integer.parseInt(atts.getValue("uid")), atts.getValue("name"), atts.getValue("stat"), Integer.parseInt(atts.getValue("timerLeft")),currentTime-Long.parseLong(atts.getValue("lastRefresh")));
    			}
    		}
    		if(localName.equals("guestList")){
    			this.in_guestList = true;
    		}
    		else if(in_guestList){
    			if(localName.equals("guest")){
    				parsedDataset.addPlayer(0, Integer.parseInt(atts.getValue("uid")), atts.getValue("name"), "Spectating", 0, 0);
    			}
    		}
    		//Events during game
    		if(localName.equals("eventList")){
    			this.in_eventList = true;
    		}
    		else if(in_eventList){
    			if(localName.equals("event")){
    				parsedDataset.lasteid = Integer.parseInt(atts.getValue("eid"));
    				
    				//Messages
    				if(atts.getValue("type").equals("MSG")){
    					int uid = Integer.parseInt(atts.getValue("uid"));
    					String name = "";
    					for(int i=0;i<parsedDataset.players.size();i++){
    						if(parsedDataset.players.get(i).uid==uid){
    							name = parsedDataset.players.get(i).name;
    						}
    					}
    					parsedDataset.addMessage(atts.getValue("data"), uid, name);
    				}
    				
    				//Moves
    				else if(atts.getValue("type").equals("MOVE") && NetGlobal.game!=null){
    					String point = atts.getValue("data");
    					for(int i=0;i<parsedDataset.players.size();i++){
    						if(parsedDataset.players.get(i).uid==Integer.parseInt(atts.getValue("uid"))){
    							if(parsedDataset.players.get(i).place==1){
    								parsedDataset.p1moves.add(GameAction.stringToPoint(point, NetGlobal.game));
    							}
    							else if(parsedDataset.players.get(i).place==2){
    								parsedDataset.p2moves.add(GameAction.stringToPoint(point, NetGlobal.game));
    							}
    							break;
    						}
    					}
    				}
    				
    				//Undo
    				else if(atts.getValue("type").equals("UNDOASK")){
    					parsedDataset.undoRequested=true;
    				}
    				else if(atts.getValue("type").equals("UNDODONE")){
    					parsedDataset.undoAccepted=true;
    				}
    				
    				//New game
    				else if(atts.getValue("type").equals("RESTART")){
    					parsedDataset.restart=true;
    					parsedDataset.setSid(Integer.parseInt(atts.getValue("data")));
    				}
    				
    				//Player gives up
    				else if(atts.getValue("type").equals("NOTICE")){
    					if(atts.getValue("data").contains("PHRASE_GIVES_UP")){
    						for(int i=0; i<parsedDataset.players.size();i++){
    							if(parsedDataset.players.get(i).uid == Integer.parseInt(atts.getValue("uid"))){
    								if(parsedDataset.players.get(i).place==1){
    									parsedDataset.p1GaveUp = true;
    								}
    								else if(parsedDataset.players.get(i).place==2){
    									parsedDataset.p2GaveUp = true;
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    		//Changing board configurations
			else if(localName.equals("gameOptions")){
				this.in_gameOptions = true;
    			parsedDataset.optionsChanged = true;
			}
			else if(in_gameOptions){
				if(localName.equals("boardSize")){
					this.in_boardSize = true;
				}
				else if(localName.equals("timerTotal")){
					this.in_timerTotal = true;
				}
				else if(localName.equals("timerInc")){
					this.in_timerInc = true;
				}
				else if(localName.equals("scored")){
					this.in_scored = true;
				}
			}
    	}
    	//For creating a new game
    	else if(localName.equals("sessionInfo")){
    		this.in_sessionInfo = true;
    	}
    	else if(in_sessionInfo){
    		if(localName.equals("sid")){
    			this.in_sid = true;
    		}
    		else if(localName.equals("server")){
    			this.in_server = true;
    		}
    	}
    }
       
    /** Gets be called on closing tags like:
     * </tag> */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    	if(localName.equals("errorMessage")){
    		this.in_errorMessage = false;
        }
    	else if(localName.equals("loginResult")){
    		this.in_loginResult = false;
        }
    	else if(localName.equals("uid")){
    		this.in_uid = false;
        }
    	else if(localName.equals("name")){
        	this.in_name = false;
        }
    	else if(localName.equals("session_id")){
    		this.in_session_id = false;
    	}
    	else if(localName.equals("sessionList")){
    		this.in_sessionList = false;
    	}
    	else if(localName.equals("session")){
    		this.in_session = false;
    	}
    	else if (localName.equals("sessionInfo")){
    		this.in_sessionInfo = false;
    	}
    	else if (localName.equals("sid")){
			this.in_sid = false;
		}
		else if (localName.equals("server")){
			this.in_server = false;
		}
		else if(localName.equals("handlerData")){
    		this.in_handlerData = false;
    	}
    	else if(localName.equals("playerList")){
    		this.in_playerList = false;
    	}
    	else if(localName.equals("guestList")){
    		this.in_guestList = false;
    	}
    	else if(localName.equals("eventList")){
    		this.in_eventList = false;
    	}
    	else if(localName.equals("gameOptions")){
    		this.in_gameOptions = false;
    	}
    	else if(localName.equals("boardSize")){
    		this.in_boardSize = false;
    	}
    	else if(localName.equals("timerTotal")){
    		this.in_timerTotal = false;
    	}
    	else if(localName.equals("timerInc")){
    		this.in_timerInc = false;
    	}
    	else if(localName.equals("scored")){
    		this.in_scored = false;
    	}
    }
       
    /** Gets be called on the following structure:
     * <tag>characters</tag> */
    @Override
    public void characters(char ch[], int start, int length) {
    	if(this.in_loginResult){
	    	if(this.in_uid){
	    		parsedDataset.setUid(Integer.parseInt(new String(ch, start, length)));
	        }
	    	else if(this.in_name){
	    		parsedDataset.setName(new String(ch, start, length));
	        }
	    	else if(this.in_session_id){
	    		parsedDataset.setSession_id(new String(ch, start, length));
	    	}
    	}
    	else if(this.in_handlerData){
    		if(this.in_gameOptions){
    			if(this.in_boardSize){
    				NetGlobal.gridSize = Integer.parseInt(new String(ch, start, length));
    			}
    			else if(this.in_timerTotal){
    				NetGlobal.timerTime = Integer.parseInt(new String(ch, start, length))/60;
    			}
    			else if(this.in_timerInc){
    				NetGlobal.additionalTimerTime = Integer.parseInt(new String(ch, start, length));
    			}
    			else if(this.in_scored){
    				NetGlobal.ratedGame = new String(ch, start, length).equals("1");
    			}
    		}
    	}
    	else if(this.in_sessionInfo){
    		if(this.in_sid) {
    			parsedDataset.setSid(Integer.parseInt(new String(ch, start, length)));
    		}
    		else if(this.in_server) {
    			parsedDataset.setServer(new String(ch, start, length));
    		}
    	}
    	else if(this.in_errorMessage){
    		parsedDataset.setErrorMessage(new String(ch, start, length));
    	}
    }
}