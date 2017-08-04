package com.collibra.fabio.graph;

/**
 * Representation for an edge on a graph. Contains the {@link Node} information
 * for origin and destination to keep the edge directed. Contains also the
 * weight information.
 * 
 * @author fabio
 *
 */
public class Edge {
	private Node origin;
	private Node destination;
	private Integer weight;

	public Edge(Node origin, Node destination, Integer weight) {
		this.origin = origin;
		this.destination = destination;
		this.weight = weight;
	}

	// --- GETTERS AND SETTERS ---

	public Node getOrigin() {
		return origin;
	}

	public Node getDestination() {
		return destination;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	// --- HASH CODE & EQUALS ---

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((weight == null) ? 0 : weight.hashCode());
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
		Edge other = (Edge) obj;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		if (weight == null) {
			if (other.weight != null)
				return false;
		} else if (!weight.equals(other.weight))
			return false;
		return true;
	}

}
