package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.sam.hex.net.igGC.ParsedDataset;
import com.sam.hex.net.igGC.igGameCenter;

import android.os.Handler;

/**
 * @author Will Harmon
 **/
public class RefreshPlayerlist implements Runnable{
	private boolean refresh = true;
	Handler handler;
	Runnable updateResults;
	public RefreshPlayerlist(Handler handler, Runnable updateResults){
		this.handler = handler;
		this.updateResults = updateResults;
		
		new Thread(this).start();
	}

	@Override
	public void run() {
		while(refresh){
			try {
	            ParsedDataset parsedDataset = igGameCenter.refreshLobby(NetGlobal.uid, NetGlobal.session_id, NetGlobal.gid);
	        	if(!parsedDataset.error){
        			NetGlobal.sessions = parsedDataset.sessions;
	        		handler.post(updateResults);
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
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stop(){
		refresh = false;
	}
}