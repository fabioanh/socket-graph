package com.collibra.fabio;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Application class used to interact with the command line to launch all the
 * required functionalities
 * 
 * @author fabio
 *
 */
public class App {
	private static final Integer DEFAULT_PORT = 50000;

	public static void main(String[] args) {
		Integer portNumber = DEFAULT_PORT;
		if (args.length > 0) {
			if (args[0].equals("debug")) {
				Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
			}
		}
		new Server(portNumber);
	}
}
