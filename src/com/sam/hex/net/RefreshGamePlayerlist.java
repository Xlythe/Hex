package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.sam.hex.R;
import com.sam.hex.net.igGC.ParsedDataset;
import com.sam.hex.net.igGC.igGameCenter;

import android.content.Context;
import android.os.Handler;

/**
 * @author Will Harmon
 **/
public class RefreshGamePlayerlist implements Runnable{
	private boolean refresh = true;
	private Handler handler;
	private Runnable updateResults;
	private Runnable startGame;
	private Context context;
	public RefreshGamePlayerlist(Handler handler, Runnable updateResults, Runnable startGame, Context context){
		this.handler = handler;
		this.updateResults = updateResults;
		this.startGame = startGame;
		this.context = context;
	}

	@Override
	public void run() {
		while(refresh){
			try {
	            ParsedDataset parsedDataset = igGameCenter.refresh(NetGlobal.server, NetGlobal.uid, NetGlobal.session_id, NetGlobal.sid, NetGlobal.lasteid);
	        	if(!parsedDataset.error){
	        		if(parsedDataset.lasteid!=0) NetGlobal.lasteid = parsedDataset.lasteid;
        			NetGlobal.members = parsedDataset.players;
        			if(parsedDataset.optionsChanged){
        				WaitingRoomActivity.messages.add(context.getString(R.string.optionsChanged));
        			}
        			for(int i=0;i<parsedDataset.messages.size();i++){
        				WaitingRoomActivity.messages.add(parsedDataset.messages.get(i).name+": "+parsedDataset.messages.get(i).msg);
        			}
        			if(parsedDataset.gameActive) handler.post(startGame);
        			else handler.post(updateResults);
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
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void start(){
		refresh = true;
		new Thread(this).start();
	}
	
	public void stop(){
		refresh = false;
	}
}