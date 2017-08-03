package com.collibra.fabio.graph;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public class Graph {

	// private PriorityBlockingQueue<Node> nodes;
	private ConcurrentMap<String, Node> nodes = new ConcurrentHashMap<>();
	private Set<Edge> edges = ConcurrentHashMap.newKeySet();
	private ConcurrentMap<Node, ConcurrentMap<Node, Integer>> adjacencyMatrix = new ConcurrentHashMap<>();

	public Graph() {
		// nodes = new PriorityBlockingQueue<Node>(10, new Node());
	}

	public Graph(String fileLocation) {
		loadFromFile(fileLocation);
	}

	public synchronized Boolean addNode(String nodeName) {
		Node newNode = new Node(nodeName);
		Boolean present = nodes.put(nodeName, newNode) != null;
		if (!present) {
			adjacencyMatrix.put(newNode, new ConcurrentHashMap<>());
		}
		return !present;
	}

	public synchronized Boolean removeNode(String nodeName) {
		Node removed = nodes.remove(nodeName);
		if (removed != null) {
			this.adjacencyMatrix.remove(removed);
			this.cleanUpEdgesForNode(removed);
			this.cleanUpAdjacencyMatrixForNode(removed);
			return true;
		}
		return false;
	}

	public synchronized Boolean removeEdge(String nodeA, String nodeB) {
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

	public synchronized Boolean addEdge(String nodeA, String nodeB, Integer weight) {
		Node origin = nodes.get(nodeA);
		Node destination = nodes.get(nodeB);
		if (origin == null || destination == null) {
			return false;
		}

		// Filter useless duplicate edges
		Iterator<Edge> edgeIter = edges.iterator();
		Boolean found = false;
		while (edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			if (edge.getOrigin().equals(origin) && edge.getDestination().equals(destination)) {
				found = true;
				if (edge.getWeight() > weight) {
					edge.setWeight(weight);
					this.adjacencyMatrix.get(origin).put(destination, weight);
				}
			}
		}
		if (!found) {
			this.edges.add(new Edge(origin, destination, weight));
			this.adjacencyMatrix.get(origin).put(destination, weight);
		}
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

	private void cleanUpAdjacencyMatrixForNode(Node removed) {
		for (Entry<Node, ConcurrentMap<Node, Integer>> e : this.adjacencyMatrix.entrySet()) {
			e.getValue().remove(removed);
		}
	}

	public synchronized Integer shortestPath(String originNode, String destinationNode) {
		Node origin = nodes.get(originNode);
		Node destination = nodes.get(destinationNode);
		if (origin == null || destination == null) {
			return null;
		}
		DijkstraAlgorithm shortestPathAlgorithm = new DijkstraAlgorithm(this);
		shortestPathAlgorithm.execute(origin);
		return shortestPathAlgorithm.getShortestDistance(destination);

	}

	public String closerThan(Integer weight, String node) {
		Node origin = nodes.get(node);
		if (origin == null) {
			return null;
		}
		List<String> neighbours = findNeighbourss(origin, weight, new HashSet<>());
		neighbours = new ArrayList<>(new HashSet<>(neighbours));
		neighbours.sort(Comparator.naturalOrder());
		return String.join(",", neighbours);
	}

	private List<String> findNeighbours(Node origin, Integer thresholdDistance) {
		List<String> result = new ArrayList<>();
		if (thresholdDistance > 0) {
			for (Edge e : edges) {
				if (e.getOrigin().equals(origin) && e.getWeight() <= thresholdDistance) {
					result.add(e.getDestination().getName());
					result.addAll(findNeighbours(e.getDestination(), thresholdDistance - e.getWeight()));
				}
			}
		}
		return result;
	}

	private List<String> findNeighbourss(Node origin, Integer thresholdDistance, HashSet<Node> visited) {
		List<String> result = new ArrayList<>();
		if (thresholdDistance > 0) {
			for (Entry<Node, Integer> e : this.adjacencyMatrix.get(origin).entrySet()) {
				if (!visited.contains(e.getKey())) {
					visited.add(e.getKey());
					if (e.getValue() <= thresholdDistance) {
						result.add(e.getKey().getName());
						result.addAll(findNeighbourss(e.getKey(), thresholdDistance - e.getValue(), visited));
					}
				}
			}
		}
		return result;
	}

	private void loadFromFile(String fileLocation) {
		try {
			Path path = Paths.get(getClass().getClassLoader().getResource(fileLocation).toURI());

			Stream<String> lines;
			lines = Files.lines(path);
			lines.forEach(line -> loadEdgeFromFile(line));
			lines.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private Object loadEdgeFromFile(String line) {
		line = line.trim().replaceAll("( )+", " ");
		String[] data = line.split(" ");
		try {
			if (data.length > 0) {
				this.addNode(data[0]);
				this.addNode(data[1]);
				this.addEdge(data[0], data[1], Integer.valueOf(data[2]));
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ConcurrentMap<String, Node> getNodes() {
		return nodes;
	}

	public Set<Edge> getEdges() {
		return edges;
	}

	public ConcurrentMap<Node, ConcurrentMap<Node, Integer>> getAdjacencyMatrix() {
		return adjacencyMatrix;
	}

}
