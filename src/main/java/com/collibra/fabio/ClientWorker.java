package com.collibra.fabio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientWorker implements Runnable {

	private Socket clientSocket;
	private Session session;

	public ClientWorker(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {

		try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {

			String inputLine, outputLine;

			session = new Session();
			out.println(session.getHiMessage());

			this.startSessionChecker(out);

			while ((inputLine = in.readLine()) != null) {
				System.out.println("Client: " + inputLine);
				session.processInput(inputLine);
				outputLine = session.getCurrentMessage();
				System.out.println("Server: " + outputLine);
				out.println(outputLine);
				if (!session.isAlive()) {
					break;
				}
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
		Runnable task = () -> checkAliveSession(out);
		scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
	}

	private void checkAliveSession(PrintWriter out) {
		if (!session.checkAlive()) {
			System.out.println("Session is not alive");
			out.println(session.getCurrentMessage());
			throw new RuntimeException();
		}
	}

}
