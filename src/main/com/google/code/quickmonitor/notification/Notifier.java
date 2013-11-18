/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.notification;

import com.google.code.quickmonitor.domain.NotificationMessage;
import com.google.code.quickmonitor.domain.Result;

/**
 *
 * @author Mukundan
 */
public interface Notifier {
	
	public Result notify(NotificationMessage message);
	
}
