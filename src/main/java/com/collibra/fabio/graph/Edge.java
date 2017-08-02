package com.collibra.fabio.graph;

public class Edge {
	private Node origin;
	private Node destiny;
	private Integer weight;

	public Edge(Node origin, Node destiny, Integer weight) {
		this.origin = origin;
		this.destiny = destiny;
		this.weight = weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destiny == null) ? 0 : destiny.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((weight == null) ? 0 : weight.hashCode());
		return result;
	}

	public Node getOrigin() {
		return origin;
	}

	public Node getDestiny() {
		return destiny;
	}

	public Integer getWeight() {
		return weight;
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
		if (destiny == null) {
			if (other.destiny != null)
				return false;
		} else if (!destiny.equals(other.destiny))
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
