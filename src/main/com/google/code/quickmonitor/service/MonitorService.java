package com.google.code.quickmonitor.service;

import com.google.code.quickmonitor.domain.MailNotificationMessage;
import com.google.code.quickmonitor.logging.LogFactory;
import com.google.code.quickmonitor.notification.Notifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickserver.util.FileChangeListener;
import org.quickserver.util.FileChangeMonitor;
import org.quickserver.net.client.monitoring.HostMonitoringService;
import com.google.code.quickmonitor.notification.impl.MailNotifier;
import com.google.code.quickmonitor.notification.impl.NotifierFactory;
import java.util.*;
import org.quickserver.net.server.QuickServer;

public class MonitorService {
	private static final Logger logger = Logger.getLogger(MonitorService.class.getName());
	
	private static final String path = "./conf/";
	private static boolean waitFlag = true;
	private static String quickServerPort;
	private static boolean isConfigFileTypeXml = false;
	
	private static Map<String,String> nameDescMap = new HashMap<String,String>();
	
	public static String getNameForState(char state) {
		return nameDescMap.get("NAME_STATE_"+state);
	}
	
	public static String getFormatedNameForState(char state) {
		return "("+nameDescMap.get("NAME_STATE_"+state)+")";
	}
	
	static {
		nameDescMap.put("NAME_STATE_E", "Error");
		nameDescMap.put("NAME_STATE_U", "Unknown");
		nameDescMap.put("NAME_STATE_A", "Active");
		nameDescMap.put("NAME_STATE_D", "Down");
		
		FileChangeListener fcl = new FileChangeListener() {

			@Override
			public void changed() {
				logger.info("reload.txt file has changed.. re-init monitor service..");
				MonitorService.initFromPropertyFiles();
			}
		};
		FileChangeMonitor.addListener(path + "reload.txt", fcl);
		
		FileChangeListener fc2 = new FileChangeListener() {

			@Override
			public void changed() {
				logger.info("conf.ini has changed.. reload conf parms..");
				MonitorService.loadConfiguration();
			}
		};
		FileChangeMonitor.addListener(path + "conf.ini", fc2);
		
		LogFactory.init();
	}
	
	public static void main(String[] args) {
		logger.info("Monitor starting..");
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				logger.info("in shutdown hook");
				logger.info("Sending email notification for QuickMonitor stop.");
				sendEmailNotification("QuickMonitor: Was Stopped", "Stopped at "+new Date());
			}
		}));
		logger.fine("loading conf.ini..");
		loadConfiguration();
		logger.info("loading conf.ini done.");
		if (isConfigFileTypeXml){
			initFromXmlFile();
		}else {
			initFromPropertyFiles();
		}
		
		logger.info("Sending email notification for QuickMonitor start.");
		sendEmailNotification("QuickMonitor: Was Started", "Started at "+new Date());
		
		try{
			if (null != quickServerPort && "".equals(quickServerPort)==false){
				QuickServer myServer = new QuickServer();
				myServer.setClientCommandHandler("com.google.code.quickmonitor.qs.CommandHandler");
				myServer.setPort(Integer.parseInt(quickServerPort));
				myServer.setName("QuickMonitor Server");
				myServer.startServer();
				logger.log(Level.INFO, "Started quickserver at port:{0}", quickServerPort);
			}
		}catch(Exception e){
			logger.log(Level.SEVERE, "Error starting quickserver at port:{0}:{1}", new Object[]{quickServerPort, e});
		}
		logger.log(Level.INFO, "Monitor server started..{0}", new Date());
		try {
			synchronized(Thread.currentThread()){
				while (waitFlag){
					Thread.currentThread().wait();
					logger.warning("after wait");
				}
			}
		} catch (InterruptedException ex) {
			waitFlag = false;
			logger.fine("Monitor interrupted. Exiting..");
			System.exit(1);
		}
		logger.info("Monitor done..");
	}

	public static void initFromPropertyFiles() {
		logger.fine("init from property files start..");
		
		File file = null;
		HostMonitoringService hostMonitoringService = null;
		Properties config = null;
		
		HostMonitoringService.clear();
		
		File[] list = getFilesList(path+"/host");
		logger.fine("loading host files to monitor.");
		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory()) {
				continue;
			}
			file = list[i];
			if (file.getName().endsWith(".txt")) {
				config = loadPropertiesFromFile(file);
				String hostName = file.getName();
				int extIndex = hostName.lastIndexOf(".");
				if (extIndex != -1) {
					hostName = hostName.substring(0, extIndex);
				}
				hostMonitoringService = HostMonitoringServiceFactory.getHostMonitoringService(hostName, config);
			}
			if (null == hostMonitoringService){
				continue;
			}
			HostMonitoringService.add(hostMonitoringService);
		}
		logger.fine("loading host files done.");
		
		logger.fine("init from property files done");
	}

	private static Properties loadPropertiesFromFile(File file) {
		FileInputStream myInputStream = null;
		Properties config = null;
		try {
			logger.log(Level.FINE, "loading properties for host:{0}", file.getName());
			config = new Properties();
			myInputStream = new FileInputStream(file);
			config.load(myInputStream);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not load[{0}] {1}", new Object[]{file.getAbsolutePath(), e});
		} finally {
			if (myInputStream != null) {
				try {
					myInputStream.close();
				} catch (IOException ex) {
					Logger.getLogger(MonitorService.class.getName()).log(Level.SEVERE, "Error", ex);
				}
			}
		}
		return config;
	}

	private static File[] getFilesList(String filePath) throws RuntimeException {
		File domainDir = new File(filePath);
		if (domainDir.canRead() == false) {
			logger.log(Level.SEVERE, "can''t read domain  config dir: {0}", domainDir);
			throw new RuntimeException("can't read domain  config dir: " + domainDir);
		}
		File list[] = domainDir.listFiles();
		if (list == null) {
			logger.log(Level.SEVERE, "Can''t read domains config dir.. got null list: {0}", domainDir);
			throw new RuntimeException("Can't read domains config dir.. got null list: "
				+ domainDir);
		}
		return list;
	}

	private static void loadConfiguration() {
		File confFile = new File(path+"/conf.ini");
		Properties config = loadPropertiesFromFile(confFile);
		if (null == config){
			logger.warning("conf.ini file not present or empty...");
			return;
		}
		logger.log(Level.FINE, "config:{0}", config);

		String mailServerIP = config.getProperty("MAIL_SERVER_IP");
		String mailServerPort = config.getProperty("MAIL_SERVER_PORT");
		String mailServerUserName = config.getProperty("MAIL_SERVER_USER_NAME");
		String fromAddress = config.getProperty("MAIL_FROM_ADDRESS");
		String toAddress = config.getProperty("MAIL_TO_ADDRESS");
		quickServerPort = config.getProperty("QUICK_SERVER_PORT");
		String configFileType = config.getProperty("CONFIG_FILE_TYPE");
		if (null != configFileType && "xml".equalsIgnoreCase(configFileType)) {
			isConfigFileTypeXml = true;
		}
		String mailEnable = config.getProperty("MAIL_ENABLE");
		if(mailEnable==null) mailEnable = "Y";
		
		if("Y".equals(mailEnable) || "y".equals(mailEnable)) {
			MailNotifier.setEnable(true);
		} else {
			MailNotifier.setEnable(false);
		}
		
		if (null == mailServerIP || "".equals(mailServerIP)){
			logger.warning("Mail notification not configured..");
			return;
		}else{
			MailNotifier.setMailServerIP(mailServerIP);	
		}
		
		if (null == mailServerPort && "".equals(mailServerPort)){
			logger.warning("mail server port not configured.. defaulting to 25.");
			MailNotifier.setMailServerPort("25");
		}else{
			MailNotifier.setMailServerPort(mailServerPort);
		}
		
		if (null != mailServerUserName && "".equals(mailServerUserName)==false){
			MailNotifier.setMailServerUserName(mailServerUserName);
		}
		
		if (null != fromAddress && "".equals(fromAddress)==false){
			MailNotifier.setFromAddress(fromAddress);
		}
		
		if (null != toAddress && "".equals(toAddress)==false){
			String [] toAddrArray = toAddress.split(",");
			List<String> toAddrList = Arrays.asList(toAddrArray);
			MailNotifier.setToAddress(toAddrList);
		}
		
		if(config.getProperty("NAME_STATE_E")!=null) {
			nameDescMap.put("NAME_STATE_E", config.getProperty("NAME_STATE_E"));
		} 
		if(config.getProperty("NAME_STATE_U")!=null) {
			nameDescMap.put("NAME_STATE_U", config.getProperty("NAME_STATE_U"));
		} 
		if(config.getProperty("NAME_STATE_A")!=null) {
			nameDescMap.put("NAME_STATE_A", config.getProperty("NAME_STATE_A"));
		} 
		if(config.getProperty("NAME_STATE_D")!=null) {
			nameDescMap.put("NAME_STATE_D", config.getProperty("NAME_STATE_D"));
		} 
	}
	
	private static void sendEmailNotification(String subject, String body) {
		logger.fine("sending email notification..");
		MailNotificationMessage msg = new MailNotificationMessage();
		msg.setSubject(subject);
		msg.setBody(body);
		Notifier notifier = NotifierFactory.getInstance("email");
		notifier.notify(msg);
	}

	private static void initFromXmlFile() {
		//TODO - implement loading from xml file
		//HostMonitoringService.clear();
		
	}
}
