/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.domain;

/**
 *
 * @author mukundan
 */
public class Result {
	
	private boolean success;
	
	private String detail;
	
	private String data;
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("success:");
		sb.append(success);
		sb.append("|detail:");
		sb.append(detail);
		sb.append("|data:");
		sb.append(data);
		return sb.toString();
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param sucess the success to set
	 */
	public void setSucess(boolean success) {
		this.success = success;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * @param detail the detail to set
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}
}
