package com.collibra.fabio;

/**
 *
 *
 */
public class App {
	public static void main(String[] args) {
		Integer portNumber = Integer.parseInt(args[0]);
		Server server = new Server(portNumber);
	}
}
