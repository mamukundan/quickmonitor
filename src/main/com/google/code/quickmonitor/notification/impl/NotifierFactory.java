/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.notification.impl;

import com.google.code.quickmonitor.notification.Notifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;



/**
 *
 * @author mukundan
 */
public class NotifierFactory {
	
	private static final Logger logger = Logger.getLogger(NotifierFactory.class.getName());
	
	private static final Map<String, Notifier> notifierImplMap = new HashMap<String, Notifier>();
	
	static{
		notifierImplMap.put("email", new MailNotifier());
		notifierImplMap.put("http", new HttpNotifier());
	}
	
	public static Notifier getInstance(String notifierType){
		Notifier notifier = notifierImplMap.get(notifierType);
		return notifier;
	}
	
}
