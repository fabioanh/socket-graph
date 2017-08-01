package com.collibra.fabio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.collibra.fabio.protocol.State;

public class Server {

	private Set<Session> sessions = new HashSet<Session>();

	public Server(Integer portNumber) {

		System.out.println("Application start...");
		try (ServerSocket service = new ServerSocket(portNumber);
				Socket clientSocket = service.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {

			String inputLine, outputLine;

			Session session = new Session();
			this.register(session);
			out.println(session.getHiMessage());

			this.startSessionChecker(out);

			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
				session.processInput(inputLine);
				outputLine = session.getCurrentMessage();
				System.out.println(outputLine);
				out.println(outputLine);
			}

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * Starts the thread that will take care of killing timed-out sessions
	 */
	private void startSessionChecker(PrintWriter out) {
		System.out.println("Starting session checker ...");
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		Runnable task = () -> checkAliveSessions(out);
		scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
	}

	public void register(Session session) {
		sessions.add(session);
	}

	private void checkAliveSessions(PrintWriter out) {
		Iterator<Session> iter = sessions.iterator();
		while (iter.hasNext()) {
			Session session = iter.next();
			if (!session.checkAlive()) {
				System.out.println("Session is not alive");
				out.println(session.getCurrentMessage());
			}
		}
	}
}
