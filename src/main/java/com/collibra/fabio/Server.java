package com.collibra.fabio;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {

	public Server(Integer portNumber) {

		System.out.println("Application start...");

		try (ServerSocket service = new ServerSocket(portNumber);) {
			while (true) {
				ClientWorker worker = new ClientWorker(service.accept());
				Thread t = new Thread(worker);
				t.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
