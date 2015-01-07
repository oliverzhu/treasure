package com.client.customerservicecenter.parser.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;


public class ServiceOutletXmlParsing {
	private ServiceOutletXmlParsing () {}
	
	private static ServiceOutletXmlParsing instance = new ServiceOutletXmlParsing();
	
	public static ServiceOutletXmlParsing getInstance() {
		return instance;
	}
	
	public Map<String, String> parse(String path)
	{
		if(!(new File(path).exists()))
		{
			return null;
		}
		HashMap<String, String> map = null;
		try {
			map = new HashMap<String, String>();
			InputStream in = new FileInputStream(path);
			
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in,"utf-8"); 
			int eventCode = parser.getEventType();
			while(eventCode != XmlPullParser.END_DOCUMENT){
				switch( eventCode ){
					case XmlPullParser.START_TAG:
						String tag = parser.getName();
						if(tag.equals("interval")){
							map.put("interval", parser.nextText());
						}
					    break;
				 }
				 eventCode = parser.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

}
