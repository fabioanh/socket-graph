package com.collibra.fabio.graph;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Node implements Comparator<Node> {

	private Set<Edge> edges;
	private String name;

	public Node() {
		edges = ConcurrentHashMap.newKeySet();
	}

	public Node(String name) {
		this.name = name;
	}

	public void addEdge(Node node, Integer weight) {
		this.edges.add(new Edge(node, weight));
	}

	public void removeEdge(Node node) {
		edges.remove(node);
	}

	@Override
	public int compare(Node o1, Node o2) {
		return Integer.valueOf(o2.edges.size()).compareTo(o1.edges.size());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
