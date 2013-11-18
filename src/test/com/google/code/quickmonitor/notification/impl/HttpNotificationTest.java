/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.notification.impl;

import com.google.code.quickmonitor.domain.HttpNotificationMessage;
import com.google.code.quickmonitor.domain.Result;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mukundan
 */
public class HttpNotificationTest {
	public static void main(String[] args){
		try {
			HttpNotificationMessage msg = new HttpNotificationMessage("http","www.cafeaulait.org",-1 , "/books/jnp3/postquery.phtml");
			Map<String, String> reqMap = new HashMap<String, String>();
			reqMap.put("&username", "test");
			reqMap.put("&password", "password");
			msg.setRequestMap(reqMap);
			Result result = new HttpNotifier().notify(msg);
			System.out.println(result);
		} catch (MalformedURLException ex) {
			Logger.getLogger(HttpNotificationTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}
}
