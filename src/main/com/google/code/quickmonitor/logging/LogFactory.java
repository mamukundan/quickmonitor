package com.google.code.quickmonitor.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickserver.util.logging.MiniFormatter;
import org.quickserver.util.logging.SimpleConsoleFormatter;
import org.quickserver.util.logging.SimpleTextFormatter;

/**
 *
 * @author Mukundan
 */
public class LogFactory {
	
	public static void init () {
		File logFile = new File("./log/");
		if(logFile.canRead()==false) {
			logFile.mkdirs();
		}
		Logger logger = null;
		try {
			logger = Logger.getLogger("");
			Handler[] handlers = logger.getHandlers();
			handlers[0].setFormatter(new MiniFormatter());
			//logger.removeHandler(handlers[0]);
			
			logger = Logger.getLogger("org.quickserver");
			logger.setLevel(Level.FINEST);
			
			logger = Logger.getLogger("");
			FileHandler fileHandler = new FileHandler("log/Monitor_%u%g.txt", 1024*1024, 100, true);
			fileHandler.setFormatter(new SimpleTextFormatter());
			
			logger.setLevel(Level.FINE);
			fileHandler.setLevel(Level.FINE);
			logger.addHandler(fileHandler);
		} catch (IOException ex) {
			Logger.getLogger(LogFactory.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SecurityException ex) {
			Logger.getLogger(LogFactory.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
}
