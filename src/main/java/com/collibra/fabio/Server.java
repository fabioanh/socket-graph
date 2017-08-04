package com.collibra.fabio;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.collibra.fabio.graph.Graph;

/**
 * Initialises the server socket to listen to any tcp communication in a given
 * port number.
 * 
 * In order to keep separate conversations, starts a thread
 * ({@link ClientWorker}) for each new client connecting to the server
 * 
 * @author fabio
 *
 */
public class Server {

	private static final Logger LOGGER = LogManager.getLogger(Server.class);

	private Graph graph;

	public Server(Integer portNumber) {

		graph = new Graph();

		try (ServerSocket service = new ServerSocket(portNumber);) {
			LOGGER.info("Application listening on port " + portNumber + "...");
			while (true) {
				ClientWorker worker = new ClientWorker(service.accept(), graph);
				Thread t = new Thread(worker);
				t.start();
			}

		} catch (IOException e) {
			LOGGER.error("Error making the communication link with the client", e);
		}

	}

}
