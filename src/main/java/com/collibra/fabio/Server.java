package com.collibra.fabio;

import java.io.IOException;
import java.net.ServerSocket;

import com.collibra.fabio.graph.Graph;

public class Server {

	private Graph graph;

	public Server(Integer portNumber) {

		System.out.println("Application start...");

		graph = new Graph();

		try (ServerSocket service = new ServerSocket(portNumber);) {
			while (true) {
				ClientWorker worker = new ClientWorker(service.accept(), graph);
				Thread t = new Thread(worker);
				t.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
