/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.domain;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author mukundan
 */
public class HttpNotificationMessage extends NotificationMessage {
	
	private static final Logger logger = Logger.getLogger(HttpNotificationMessage.class.getName());
	private URL url;
	
	private Map<String, String> requestMap;
	
	public HttpNotificationMessage(String protocol, String host, int port, String path) throws MalformedURLException {
		this.url = new URL(protocol, host, port, path);
	}
	
	public HttpNotificationMessage(URL url){
		this.url = url;
	}
	
	public HttpNotificationMessage(String url) throws MalformedURLException{
		this.url = new URL(url);
	}

	/**
	 * @return the requestMap
	 */
	public Map<String, String> getRequestMap() {
		return requestMap;
	}

	/**
	 * @param requestMap the requestMap to set
	 */
	public void setRequestMap(Map<String, String> requestMap) {
		this.requestMap = requestMap;
	}

	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}
}
