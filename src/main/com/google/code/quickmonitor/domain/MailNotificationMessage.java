/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.domain;

/**
 *
 * @author Mukundan
 */
public class MailNotificationMessage extends NotificationMessage{
	
	private String fromAddress;
	
	private String toAddress;
	
	private String body;
	
	private String ccAddress;
	
	private String bccAddress;
	
	private String attachment;
	
	private String subject;

	/**
	 * @return the ccAddress
	 */
	public String getCcAddress() {
		return ccAddress;
	}

	/**
	 * @param ccAddress the ccAddress to set
	 */
	public void setCcAddress(String ccAddress) {
		this.ccAddress = ccAddress;
	}

	/**
	 * @return the bccAddress
	 */
	public String getBccAddress() {
		return bccAddress;
	}

	/**
	 * @param bccAddress the bccAddress to set
	 */
	public void setBccAddress(String bccAddress) {
		this.bccAddress = bccAddress;
	}

	/**
	 * @return the attachment
	 */
	public String getAttachment() {
		return attachment;
	}

	/**
	 * @param attachment the attachment to set
	 */
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the fromAddress
	 */
	public String getFromAddress() {
		return fromAddress;
	}

	/**
	 * @param fromAddress the fromAddress to set
	 */
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	/**
	 * @return the toAddress
	 */
	public String getToAddress() {
		return toAddress;
	}

	/**
	 * @param toAddress the toAddress to set
	 */
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}
	
}
