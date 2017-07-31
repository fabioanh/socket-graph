package com.collibra.fabio;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

	private ServerSocket service;
	private Socket serviceSocket;
	private DataInputStream is;
	private PrintStream os;

	private Set<Session> sessions = new HashSet<Session>();

	public Server(Integer portNumber) {

		try {
			service = new ServerSocket(portNumber);
			serviceSocket = service.accept();
			this.startSessionChecker();

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * Starts the thread that will take care of killing timed-out sessions
	 */
	private void startSessionChecker() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		Runnable task = () -> checkAliveSessions();
		scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
	}

	public void register(Session session) {
		sessions.add(session);
	}

	private void checkAliveSessions() {
		Iterator<Session> iter = sessions.iterator();
		while (iter.hasNext()) {
			Session session = iter.next();
			if (!session.isAlive()) {
				session.getGoodbyeMessage();
				iter.remove();
			}
		}
	}
}
