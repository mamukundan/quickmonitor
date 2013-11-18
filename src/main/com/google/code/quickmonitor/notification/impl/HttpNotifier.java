/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.notification.impl;

import com.google.code.quickmonitor.domain.HttpNotificationMessage;
import com.google.code.quickmonitor.domain.NotificationMessage;
import com.google.code.quickmonitor.domain.Result;
import com.google.code.quickmonitor.notification.Notifier;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mukundan
 */
public class HttpNotifier implements Notifier {
	
	private static final Logger logger = Logger.getLogger(HttpNotifier.class.getName());
	
	@Override
	public Result notify(NotificationMessage message) {
		HttpNotificationMessage httpMessage = (HttpNotificationMessage)message;
		
		Result result = new Result();
		URLConnection urlconn =null;
		
		InputStream inStream = null;
		OutputStream outStream = null;
		
		try {
			urlconn = (HttpURLConnection)httpMessage.getUrl().openConnection();
			urlconn.setDoOutput(true);
			urlconn.connect();
			outStream = urlconn.getOutputStream();
			sendRequestData(outStream, httpMessage);
			inStream = urlconn.getInputStream();
			result = readResponseData(inStream);
		} catch (IOException ioe) {
			result.setSucess(false);
			result.setDetail("IOException during HttpNotification.."+ioe);
			ioe.printStackTrace();
		} finally{
			try{
				if (null != inStream){
					inStream.close();
				}
				if (null != outStream){
					outStream.close();
				}
			}catch(IOException ioe){
				logger.log(Level.FINE, "Exception closing stream:{0}", ioe);
			}
		}
		return result;
	}

	private Result readResponseData(InputStream inStream) throws IOException {
		Result result = new Result();
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
		String resp;
		while ((resp= reader.readLine()) != null){
			sb.append(resp);
			sb.append("/r/n");
		}
		result.setData(sb.toString());
		result.setSucess(true);
		return result;
	}

	private void sendRequestData(OutputStream outStream, HttpNotificationMessage httpMessage) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
		Map<String, String> requestData = httpMessage.getRequestMap();
		Iterator<String> iter = requestData.keySet().iterator();
		String reqParm = null;
		String reqValue = null;
		while (iter.hasNext()){
			reqParm = iter.next();
			reqValue = requestData.get(reqParm);
			writer.write(reqParm+"="+reqValue);	
		}
		writer.flush();
		writer.close();
	}

}
