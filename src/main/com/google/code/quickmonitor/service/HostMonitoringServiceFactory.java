/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.service;

import com.google.code.quickmonitor.domain.MailNotificationMessage;
import com.google.code.quickmonitor.notification.impl.MailNotifier;
import com.google.code.quickmonitor.notification.impl.NotifierFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickserver.net.client.HttpHost;
import org.quickserver.net.client.SocketBasedHost;
import org.quickserver.net.client.monitoring.impl.HttpMonitor;
import org.quickserver.net.client.monitoring.impl.SocketMonitor;
import org.quickserver.net.client.Host;
import org.quickserver.net.client.HostList;
import org.quickserver.net.client.monitoring.HostMonitor;
import org.quickserver.net.client.monitoring.HostMonitoringService;
import org.quickserver.net.client.monitoring.HostStateListener;
import org.quickserver.net.client.monitoring.impl.PingMonitor;
import org.quickserver.net.client.monitoring.impl.UDPMonitor;
import org.quickserver.util.TextFile;

/**
 *
 * @author Mukundan
 */
public class HostMonitoringServiceFactory {

	private static final Logger logger = Logger.getLogger(HostMonitoringServiceFactory.class.getName());

	public static HostMonitoringService getHostMonitoringService(String hostName, Properties config) {
		HostMonitoringService hostMonitoringService = new HostMonitoringService();
		Host host = null;
		HostMonitor hostMonitor = null;
		final HostList hostList = new HostList(hostName);

		logger.log(Level.FINE, "Configuring HostMonitoringService for host:{0}", hostName);

		String type = config.getProperty("type");
		if (null == type || "".equals(type)) {
			logger.warning("type not configured");
			return null;
		}

		String temp = null;
		if (type.equalsIgnoreCase("socket") || type.equalsIgnoreCase("sslsocket")
				|| ("udp".equalsIgnoreCase(type)) ) {
			SocketBasedHost socketHost;
			String ip = config.getProperty("host");
			String _port = config.getProperty("port");
			int port = 0;
			try {
				port = Integer.parseInt(_port);
			} catch (NumberFormatException nfe) {
				logger.log(Level.WARNING, "invalid port configured:{0}", _port);
				return null;
			}
			try {
				socketHost = new SocketBasedHost(ip, port);
				if ("udp".equalsIgnoreCase(type)){
					hostMonitor = new UDPMonitor();
				} else {
					hostMonitor = new SocketMonitor();
					if (type.equalsIgnoreCase("sslsocket")) {
						socketHost.setSecure(true);
					}
				}
				setTextToExpectAndTimeout(config, socketHost);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error: "+e);
				return null;
			}
			host = (Host) socketHost;
		} else if (type.equalsIgnoreCase("http")) {
			try {
				String url = config.getProperty("url");
				if (url == null) {
					logger.log(Level.WARNING, "No url configured! for {0}", hostName);
					return null;
				}
				URL hostUrl = null;
				URL newUrl = null;
				String hostString = null;
				try {
					hostUrl = new URL(url);
				} catch (MalformedURLException e) {
					logger.log(Level.WARNING, "url not valid:{0}", url);
					return null;
				}
				newUrl = setPortInUrl(hostUrl, hostString);
				HttpHost httpHost = new HttpHost(newUrl);
				
				temp = config.getProperty("HttpStatusCode");
				httpHost.setHttpStatusCode(temp);
				
				setTextToExpectAndTimeout(config, httpHost);
				
				host = (Host) httpHost;
				hostMonitor = new HttpMonitor();
			} catch (Exception ex) {
				Logger.getLogger(MonitorService.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else if (type.equalsIgnoreCase("ping")) {
			String ip = config.getProperty("host");
			try {
				host = new Host(ip);
				hostMonitor = new PingMonitor();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "exception:{0}", e);
				return null;
			}
		} else {
			logger.log(Level.WARNING, "Unknown type passed:{0}", type);
			return null;
		}

		temp = config.getProperty("intervalInSec");
		if (temp != null && temp.trim().length() != 0) {
			int intervalInSec = 0;
			try {
				intervalInSec = Integer.parseInt(temp);
				hostMonitoringService.setIntervalInSec(intervalInSec);
			} catch (Exception e) {
				logger.log(Level.WARNING, "intervalInSec invalid. defaulting to 10 secs:{0}", temp);
				hostMonitoringService.setIntervalInSec(10);
			}
		} else {
			logger.warning("intervalInSec not set. defaulting to 10 secs.");
			hostMonitoringService.setIntervalInSec(10);
		}

		hostList.add(host);
		hostMonitoringService.setHostList(hostList);
		hostMonitoringService.setHostMonitor(hostMonitor);

		final String mailNotification = config.getProperty("mailNotification");
		logger.log(Level.FINE, "mailNotification:{0}", mailNotification);

		HostStateListener hsl = new HostStateListener() {
			@Override
			public void stateChanged(Host host, char oldstatus, char newstatus) {
				if (oldstatus != Host.UNKNOWN) {
					logger.log(Level.SEVERE, "State changed: old state: {0}; new state: {1} - {2};",
						new Object[]{oldstatus, newstatus, host});
					
					String body = hostList.getName()+ " State changed for " + host.toString()
						+ ". Old state: " + oldstatus + " "+ MonitorService.getFormatedNameForState(oldstatus)
						+ "; New state: " + newstatus + " "+ MonitorService.getFormatedNameForState(newstatus);
					
					body = body + "\r\nDate: "+new Date();
					
					if ("Y".equalsIgnoreCase(mailNotification) && 
							null != MailNotifier.getMailServerIP()) {
						logger.log(Level.INFO, "Sending email notification for host:{0}", host.toString());
						MailNotificationMessage msg = new MailNotificationMessage();
						msg.setBody(body);
						msg.setSubject("QuickMonitor: "+hostList.getName()+" - State Changed");
						NotifierFactory.getInstance("email").notify(msg);
					} else {
						logger.log(Level.FINE, "email notification is not On {0}", mailNotification); 
					}
				} else {
					logger.log(Level.INFO, "State changed: old state: {0}; new state: {1} - {2};",
						new Object[]{oldstatus, newstatus, host});
				}
			}
		};
		hostMonitoringService.addHostStateListner(hsl);

		return hostMonitoringService;
	}

	private static URL setPortInUrl(URL hostUrl, String hostString) throws MalformedURLException {
		URL newUrl = null;
		if (hostUrl.getPort() == -1) {
			if ("http".equalsIgnoreCase(hostUrl.getProtocol())) {
				hostString = hostUrl.getProtocol() + "://" + hostUrl.getHost() + ":80" + hostUrl.getPath();
				newUrl = new URL(hostString);
			} else if ("https".equalsIgnoreCase(hostUrl.getProtocol())) {
				hostString = hostUrl.getProtocol() + "://" + hostUrl.getHost() + ":443" + hostUrl.getPath();
				newUrl = new URL(hostString);
			}
		} else {
			newUrl = hostUrl;
		}
		return newUrl;
	}

	private static void setTextToExpectAndTimeout(Properties config, 
			SocketBasedHost socketHost) throws NumberFormatException, IOException {
		String temp = config.getProperty("TextToExpect");
		if (temp != null && temp.trim().length() != 0) {
			socketHost.setTextToExpect(temp);
		} else {
			socketHost.setTextToExpect(null);
		}
		
		temp = config.getProperty("timeout");
		if (temp != null && temp.trim().length() != 0) {
			socketHost.setTimeout(Integer.parseInt(temp));
		} 
		
		temp = config.getProperty("RequestSource");
		logger.finest("RequestSource:"+temp);
		
		if (temp != null && temp.trim().length() != 0 && 
				"file".equalsIgnoreCase(temp)){
			temp = config.getProperty("file");
			socketHost.setRequestText(TextFile.read(temp));
		}else{
			temp = config.getProperty("RequestText");
			if (temp != null && temp.trim().length() != 0) {
					temp = temp.replaceAll("\\r", "\r");
					temp = temp.replaceAll("\\n", "\n");
					socketHost.setRequestText(temp);
			} else {
					socketHost.setRequestText(null);
			}
		}
		
		temp = config.getProperty("ResponseTextToExpect");
		if (temp != null && temp.trim().length() != 0) {
			temp = temp.replaceAll("\\r", "\r");
			temp = temp.replaceAll("\\n", "\n");
			socketHost.setResponseTextToExpect(temp);
		} else {
			socketHost.setResponseTextToExpect(null);
		}
	}

    
}
