package com.collibra.fabio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.collibra.fabio.graph.Graph;

/**
 * Intended to run as a thread by the server to listen to the communication of
 * each client connected to the server
 * 
 * @author fabio
 *
 */
public class ClientWorker implements Runnable {

	private static final Logger LOGGER = LogManager.getLogger(ClientWorker.class);

	private Socket clientSocket;
	private Session session;
	private Graph graph;

	public ClientWorker(Socket clientSocket, Graph graph) {
		this.clientSocket = clientSocket;
		this.graph = graph;
	}

	/**
	 * Handles the in/out communication with the client
	 */
	@Override
	public void run() {

		try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {

			String inputLine, outputLine;

			session = new Session(graph);
			out.println(session.getHiMessage());

			this.startSessionChecker(out);

			while ((inputLine = in.readLine()) != null) {
				LOGGER.debug("Client: " + inputLine);
				session.processInput(inputLine);
				outputLine = session.getCurrentMessage();
				LOGGER.debug("Server: " + outputLine);
				out.println(outputLine);
				if (!session.isAlive()) {
					return;
				}
			}

		} catch (IOException e) {
			LOGGER.error("Error reading input from client", e);
		}
	}

	/**
	 * Starts the thread that will take care of killing timed-out sessions
	 */
	private void startSessionChecker(PrintWriter out) {
		LOGGER.trace("Starting session checker ...");
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		Runnable task = () -> checkAliveSession(out);
		scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * Run by the session expired thread. Checks if the session is still alive
	 * if not ends the session-alive-checker thread and marks the session as
	 * finalised
	 * 
	 * @param out
	 */
	private void checkAliveSession(PrintWriter out) {
		if (!session.checkAlive()) {
			LOGGER.debug("Session expired.");
			out.println(session.getCurrentMessage());
			throw new RuntimeException();
		}
	}

}
