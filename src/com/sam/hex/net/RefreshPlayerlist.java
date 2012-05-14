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
				String lobbyUrl = String.format("http://www.iggamecenter.com/api_board_list.php?app_id=%s&app_code=%s&uid=%s&session_id=%s&gid=%s", NetGlobal.id, URLEncoder.encode(NetGlobal.passcode,"UTF-8"), NetGlobal.uid, URLEncoder.encode(NetGlobal.session_id,"UTF-8"), NetGlobal.gid);
				URL url = new URL(lobbyUrl);
				SAXParserFactory spf = SAXParserFactory.newInstance();
	            SAXParser parser = spf.newSAXParser();
	            XMLReader reader = parser.getXMLReader();
	            XMLHandler xmlHandler = new XMLHandler();
	            reader.setContentHandler(xmlHandler);
	            reader.parse(new InputSource(url.openStream()));
	            
	            ParsedDataset parsedDataset = xmlHandler.getParsedData();
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