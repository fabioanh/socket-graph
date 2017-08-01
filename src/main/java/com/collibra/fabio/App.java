package com.collibra.fabio;

/**
 *
 *
 */
public class App {
	private static final Integer DEFAULT_PORT = 50000;

	public static void main(String[] args) {
		Integer portNumber = DEFAULT_PORT;
		if (args.length > 0) {
			portNumber = Integer.parseInt(args[0]);
		}
		Server server = new Server(portNumber);
	}
}
