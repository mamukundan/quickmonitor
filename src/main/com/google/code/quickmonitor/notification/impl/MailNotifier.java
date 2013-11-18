/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.notification.impl;

import com.google.code.quickmonitor.domain.Result;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;
import com.google.code.quickmonitor.domain.NotificationMessage;
import com.google.code.quickmonitor.domain.MailNotificationMessage;
import com.google.code.quickmonitor.notification.Notifier;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Mukundan
 */
public class MailNotifier implements Notifier {

	private static final Logger logger = Logger.getLogger(MailNotifier.class.getName());
	private static String mailServerIP;
	private static String mailServerPort;
	private static String mailServerUserName;
	private static String fromAddress;
	private static List<String> toAddress;
	private static volatile boolean enable = true;
	private static List<NotificationMessage> outbox = new ArrayList<NotificationMessage>();
	

	static {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					NotificationMessage msg;
					List<NotificationMessage> swapOutbox;
					logger.fine("starting mail processing thread");
					while (true) {
						try {
							try {
								Thread.sleep(60000); //1 min
							} catch (InterruptedException e) {
								Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, null, e);
							}
							//logger.fine("about to process "+outbox.size() + " message(s)");
							swapOutbox = outbox;
							outbox = new ArrayList<NotificationMessage>();
							Iterator<NotificationMessage> iterator = swapOutbox.iterator();
							while (iterator.hasNext()) {
								msg = iterator.next();
								sendMessage(msg);
							}
						} catch (Throwable e) {
							Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, null, e);
						}
					}
				} catch (Throwable t) {
					Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, null, t);
				}
			}
		};
		t.setName("Message-Sender-Thread");
		t.setDaemon(true);
		t.start();
	}

	@Override
	public Result notify(NotificationMessage message) {
		logger.fine("Message queued for sending..");
		outbox.add(message);
		Result result = new Result();
		result.setSucess(true);
		result.setDetail("Message queued for sending..");
		return result;
	}

	private static Result sendMessage(NotificationMessage message) {
		Result result = new Result();
		if (enable == false) {
			logger.fine("enabled was false");
			result.setSucess(false);
			result.setDetail("enabled was false");
			return result;
		}

		try {
			if (null == getMailServerIP()) {
				logger.severe("Mail Server IP not configured..");
				throw new Exception("Mail Server IP not configured");
			}
			Properties prop = new Properties();
			prop.put("mail.smtp.host", getMailServerIP());
			prop.put("mail.smtp.port", getMailServerPort());
			prop.put("mail.user", getMailServerUserName());
			prop.put("mail.smtp.connectiontimeout", "180000"); //Socket connection timeout - 3 min
			prop.put("mail.smtp.timeout", "180000"); //Socket I/O timeout - 3 min
			MailNotificationMessage mailMessage = (MailNotificationMessage) message;

			Session mail_Session = Session.getDefaultInstance(prop, null);
			MimeMessage myMessage = new MimeMessage(mail_Session);

			InternetAddress toMailAddress = null;
			if (mailMessage.getToAddress() != null) {
				toMailAddress = new InternetAddress(mailMessage.getToAddress());
				myMessage.addRecipient(Message.RecipientType.TO, toMailAddress);
			} else {
				if (null != getToAddress()) {
					for (String toAddr : getToAddress()) {
						toMailAddress = new InternetAddress(toAddr);
						myMessage.addRecipient(Message.RecipientType.TO, toMailAddress);
					}
				}
			}

			InternetAddress fromMailAddress = null;
			if (mailMessage.getFromAddress() != null) {
				fromMailAddress = new InternetAddress(mailMessage.getFromAddress());
			} else {
				fromMailAddress = new InternetAddress(getFromAddress());
			}
			myMessage.setFrom(fromMailAddress);

			if (mailMessage.getCcAddress() != null) {
				InternetAddress tocc = new InternetAddress(mailMessage.getCcAddress());
				myMessage.addRecipient(Message.RecipientType.CC, tocc);
			}

			if (mailMessage.getBccAddress() != null) {
				InternetAddress tobcc = new InternetAddress(mailMessage.getBccAddress());
				myMessage.addRecipient(Message.RecipientType.BCC, tobcc);
			}

			myMessage.setText(mailMessage.getBody());
			myMessage.setSentDate(new java.util.Date());

			if (mailMessage.getSubject() != null) {
				myMessage.setSubject(mailMessage.getSubject());
			}

			myMessage.saveChanges();
			Transport.send(myMessage);
		} catch (MessagingException msg_exc) {
			logger.log(Level.SEVERE, "MessagingException sending message:{0}", msg_exc);
			result.setSucess(false);
			result.setDetail("Exception sending message:" + msg_exc);
			return result;
		} catch (Exception ex) {
			result.setSucess(false);
			result.setDetail("Exception sending message:" + ex);
			return result;
		}
		result.setSucess(true);
		return result;
	}

	public static String getMailServerIP() {
		return mailServerIP;
	}

	public static void setMailServerIP(String aMailServerIP) {
		mailServerIP = aMailServerIP;
	}

	public static String getMailServerUserName() {
		return mailServerUserName;
	}

	public static void setMailServerUserName(String aMailServerUserName) {
		mailServerUserName = aMailServerUserName;
	}

	public static String getFromAddress() {
		return fromAddress;
	}

	public static void setFromAddress(String aFromAddress) {
		fromAddress = aFromAddress;
	}

	public static String getMailServerPort() {
		return mailServerPort;
	}

	public static void setMailServerPort(String aMailServerPort) {
		mailServerPort = aMailServerPort;
	}

	public static List<String> getToAddress() {
		return toAddress;
	}

	public static void setToAddress(List<String> aToAddress) {
		toAddress = aToAddress;
	}

	public static boolean isEnable() {
		return enable;
	}

	public static void setEnable(boolean aEnable) {
		enable = aEnable;
	}

}
