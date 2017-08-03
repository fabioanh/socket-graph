package com.collibra.fabio;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.junit.Test;

import com.collibra.fabio.graph.Graph;
import com.collibra.fabio.graph.Node;

public class GraphTest {

	@Test
	public void addNodeTest() {
		Graph graph = new Graph();
		graph.addNode("1");
		graph.addNode("2");
		graph.addNode("3");
		graph.addNode("4");
		graph.addNode("5");
		graph.addNode("6");
		graph.addNode("7");
		graph.addNode("8");
		graph.addNode("9");
		graph.addNode("10");
		graph.addNode("11");
		graph.addEdge("1", "2", 3);
		graph.addEdge("2", "5", 7);
		graph.addEdge("2", "3", 1);
		graph.addEdge("2", "3", 5);
		// graph.addEdge("3", "1", 2);
		graph.addEdge("3", "4", 5);
		graph.addEdge("5", "6", 4);
		graph.addEdge("5", "7", 6);
		graph.addEdge("5", "8", 3);
		graph.addEdge("5", "9", 5);
		graph.addEdge("9", "10", 2);
		graph.addEdge("11", "10", 7);
		// assertEquals(graph.getNodes().size(), 11);
		System.out.println(graph.closerThan(9, "10"));
		assertEquals(11, graph.getNodes().size());
		assertEquals(10, graph.getEdges().size());
	}

	@Test
	public void closerThanTest() {
		Graph graph = new Graph("test-graph-tiny.txt");

		System.out.println(adjacencyMatrixToString(graph.getAdjacencyMatrix()));

		assertEquals(8, graph.getNodes().size());
	}

	/**
	 * Test method intended to be used only with nodes named with sequential
	 * integers starting from 0
	 * 
	 * @return
	 */
	public String adjacencyMatrixToString(ConcurrentMap<Node, ConcurrentMap<Node, Integer>> matrix) {
		int[][] result = new int[matrix.keySet().size()][matrix.keySet().size()];
//		Arrays.fill(result, 0);
		for (Entry<Node, ConcurrentMap<Node, Integer>> e : matrix.entrySet()) {
			for (Entry<Node, Integer> ee : e.getValue().entrySet()) {
				result[Integer.valueOf(e.getKey().getName())][Integer.valueOf(ee.getKey().getName())] = ee.getValue();
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("|\t");
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result.length; j++) {
				sb.append(result[i][j] + "\t");
			}

			sb.append("|\n");
			sb.append("|\t");
		}

		return sb.toString();
	}
}
