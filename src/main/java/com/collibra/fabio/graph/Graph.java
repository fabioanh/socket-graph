package com.collibra.fabio.graph;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Directed Graph representation. Keeps the graph information with structures
 * intended to avoid concurrent manipulations
 * 
 * @author fabio
 *
 */
public class Graph {

	private static final Logger LOGGER = LogManager.getLogger(Graph.class);

	private ConcurrentMap<String, Node> nodes = new ConcurrentHashMap<>();
	private Set<Edge> edges = ConcurrentHashMap.newKeySet();
	// Matrix representation used to compute the closer-than algorithm
	private ConcurrentMap<Node, ConcurrentMap<Node, Integer>> adjacencyMatrix = new ConcurrentHashMap<>();

	public Graph() {
	}

	public Graph(String fileLocation) {
		loadFromFile(fileLocation);
	}

	/**
	 * Adds a new {@link Node} to the graph if non existent. Updates the
	 * adjacency matrix as required
	 * 
	 * @param nodeName
	 * @return
	 */
	public synchronized Boolean addNode(String nodeName) {
		Node newNode = new Node(nodeName);
		Boolean present = nodes.put(nodeName, newNode) != null;
		if (!present) {
			adjacencyMatrix.put(newNode, new ConcurrentHashMap<>());
		}
		return !present;
	}

	/**
	 * Removes a {@link Node} from the graph by its name.
	 * 
	 * @param nodeName
	 * @return
	 */
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

	/**
	 * Adds an {@link Edge} to the graph based on the names of the intended
	 * origin and destination {@link Node}s.
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param weight
	 * @return
	 */
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

	/**
	 * Removes an {@link Edge} from the graph based on the name of the nodes
	 * forming it
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
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

	/**
	 * Cleans the {@link Edge}s when a {@link Node} is removed to avoid having
	 * useless information
	 * 
	 * @param node
	 */
	private void cleanUpEdgesForNode(Node node) {
		Iterator<Edge> edgeIter = edges.iterator();
		while (edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			if (edge.getOrigin().equals(node) || edge.getDestination().equals(node)) {
				edgeIter.remove();
			}
		}
	}

	/**
	 * Cleans the adjacency matrix when a {@link Node} is removed to avoid
	 * having wrong information
	 * 
	 * @param removed
	 */
	private void cleanUpAdjacencyMatrixForNode(Node removed) {
		for (Entry<Node, ConcurrentMap<Node, Integer>> e : this.adjacencyMatrix.entrySet()) {
			e.getValue().remove(removed);
		}
	}

	/**
	 * Computes the shortest path using the {@link DijkstraAlgorithm}
	 * 
	 * @param originNode
	 * @param destinationNode
	 * @return
	 */
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

	/**
	 * Gives the list of {@link Node} names closer than a given weight. Output
	 * the values ordered alphabetically and comma separated
	 * 
	 * @param weight
	 * @param node
	 * @return
	 */
	public synchronized String closerThan(Integer weight, String node) {
		Node origin = nodes.get(node);
		if (origin == null) {
			return null;
		}
		List<String> neighbours = findNeighbours(origin, weight, new HashMap<>());
		neighbours = new ArrayList<>(new HashSet<>(neighbours));
		neighbours.sort(Comparator.naturalOrder());
		return String.join(",", neighbours);
	}

	/**
	 * Recursive function used to compute the closer-than operation using the
	 * adjacency matrix
	 * 
	 * @param origin
	 * @param thresholdDistance
	 * @param visited
	 * @return
	 */
	private List<String> findNeighbours(Node origin, Integer thresholdDistance, HashMap<Node, Integer> visited) {
		List<String> result = new ArrayList<>();
		if (thresholdDistance > 0) {
			for (Entry<Node, Integer> e : this.adjacencyMatrix.get(origin).entrySet()) {
				if (!visited.containsKey(e.getKey()) || visited.get(e.getKey()) < thresholdDistance) {
					visited.put(e.getKey(), thresholdDistance);
					if (e.getValue() <= thresholdDistance) {
						result.add(e.getKey().getName());
						result.addAll(findNeighbours(e.getKey(), thresholdDistance - e.getValue(), visited));
					}
				}
			}
		}
		return result;
	}

	/**
	 * Loads a graph from a file.
	 * 
	 * Utility function used to test sample graphs loaded from files.
	 * 
	 * @param fileLocation
	 */
	private void loadFromFile(String fileLocation) {
		try {
			Path path = Paths.get(getClass().getClassLoader().getResource(fileLocation).toURI());

			Stream<String> lines;
			lines = Files.lines(path);
			lines.forEach(line -> loadEdgeFromFile(line));
			lines.close();
		} catch (IOException e) {
			LOGGER.error("Error getting the lines from the file", e);
		} catch (URISyntaxException e) {
			LOGGER.error("Error in the specified file location", e);
		}
	}

	/**
	 * Helper to parse a single line when loading a graph from a file
	 * 
	 * @param line
	 * @return
	 */
	private Object loadEdgeFromFile(String line) {
		line = line.trim().replaceAll("( )+", " ");
		String[] data = line.split(" ");
		if (data.length > 0) {
			this.addNode(data[0]);
			this.addNode(data[1]);
			this.addEdge(data[0], data[1], Integer.valueOf(data[2]));
		}
		return null;
	}

	// --- GETTERS AND SETTERS ---

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
