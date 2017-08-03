package com.collibra.fabio.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class SuroundingNeighboursAlgorithm {

	private Graph graph;

	public SuroundingNeighboursAlgorithm(Graph graph) {
		this.graph = graph;
	}

	public String closerThan(Integer weight, Node origin) {
		List<String> neighbours = findNeighbours(origin, weight, new ArrayList<>());
		neighbours = new ArrayList<>(new HashSet<>(neighbours));
		neighbours.sort(Comparator.naturalOrder());
		return String.join(",", neighbours);
	}

	private List<String> findNeighbours(Node origin, Integer thresholdDistance, final List<Edge> visited) {
		List<Edge> vstd = new ArrayList<>(visited);
		List<String> result = new ArrayList<>();
		if (thresholdDistance > 0) {
			for (Edge e : graph.getEdges()) {
				if (vstd.contains(e)) {
					return result;
				}
				vstd.add(e);
				if (e.getOrigin().equals(origin) && e.getWeight() <= thresholdDistance) {
					result.add(e.getDestination().getName());
					result.addAll(findNeighbours(e.getDestination(), thresholdDistance - e.getWeight(), vstd));
				}
			}
		}
		return result;
	}
}
