package com.collibra.fabio.graph;

import java.util.concurrent.ConcurrentHashMap;

public class Graph {

	// private PriorityBlockingQueue<Node> nodes;
	private ConcurrentHashMap<String, Node> nodes = new ConcurrentHashMap<>();

	public Graph() {
		// nodes = new PriorityBlockingQueue<Node>(10, new Node());
	}

	public Boolean addNode(String nodeName) {
		return nodes.put(nodeName, new Node(nodeName)) == null;
	}

	public Boolean removeNode(String nodeName) {
		return nodes.remove(nodeName) != null;
	}

	public Boolean addEdge(String nodeA, String nodeB, Integer weight) {
		Node origin = nodes.get(nodeA);
		Node destiny = nodes.get(nodeB);
		if (origin == null || destiny == null) {
			return false;
		}
		origin.addEdge(destiny, weight);
		return true;
	}

	public Boolean removeEdge(String nodeA, String nodeB) {
		Node origin = nodes.get(nodeA);
		Node destiny = nodes.get(nodeB);
		if (origin == null || destiny == null) {
			return false;
		}
		origin.removeEdge(destiny);
		return true;
	}

}
