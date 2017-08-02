package com.collibra.fabio.graph;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Graph {

	// private PriorityBlockingQueue<Node> nodes;
	private ConcurrentMap<String, Node> nodes = new ConcurrentHashMap<>();
	private Set<Edge> edges = ConcurrentHashMap.newKeySet();

	public Graph() {
		// nodes = new PriorityBlockingQueue<Node>(10, new Node());
	}

	public Boolean addNode(String nodeName) {
		return nodes.put(nodeName, new Node(nodeName)) == null;
	}

	public Boolean removeNode(String nodeName) {
		Node removed = nodes.remove(nodeName);
		if (removed != null) {
			this.cleanUpEdgesForNode(removed);
			return true;
		}
		return false;
	}

	public void addEdge(Node origin, Node destination, Integer weight) {
		this.edges.add(new Edge(origin, destination, weight));
	}

	public Boolean removeEdge(String nodeA, String nodeB) {
		Node origin = nodes.get(nodeA);
		Node destination = nodes.get(nodeB);
		if (origin == null || destination == null) {
			return false;
		}
		Iterator<Edge> edgeIter = edges.iterator();
		while (edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			if (edge.getOrigin().equals(origin) && edge.getDestination().equals(destination)) {
				edgeIter.remove();
			}
		}
		return true;
	}

	public Boolean addEdge(String nodeA, String nodeB, Integer weight) {
		Node origin = nodes.get(nodeA);
		Node destination = nodes.get(nodeB);
		if (origin == null || destination == null) {
			return false;
		}
		addEdge(origin, destination, weight);
		return true;
	}

	private void cleanUpEdgesForNode(Node node) {
		Iterator<Edge> edgeIter = edges.iterator();
		while (edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			if (edge.getOrigin().equals(node) || edge.getDestination().equals(node)) {
				edgeIter.remove();
			}
		}
	}

	public Integer shortestPath(String originNode, String destinationNode) {
		// TODO Auto-generated method stub
		return null;
	}

	public String closerThan(Integer weight, String node) {
		// TODO Auto-generated method stub
		return null;
	}

}
