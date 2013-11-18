/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.quickmonitor.qs;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickserver.net.server.ClientBinaryHandler;
import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;

/**
 *
 * @author Mukundan
 */
public class CommandHandler implements ClientCommandHandler, ClientBinaryHandler{

	private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());
	private static String VERSION = "1.0";
	
	@Override
	public void handleCommand(ClientHandler ch, String command) throws SocketTimeoutException, IOException {
		logger.log(Level.FINEST, "inside handle command:{0}", command);
		if ("version".equalsIgnoreCase(command)){
			ch.sendClientMsg(VERSION);
		}
	}

	@Override
	public void handleBinary(ClientHandler ch, byte[] bytes) throws SocketTimeoutException, IOException {
		String command = new String(bytes);
		logger.log(Level.FINEST, "inside handle binary{0}", command);
		if ("version".equalsIgnoreCase(command)){
			ch.sendClientMsg(VERSION);
		}
	}
	
}
