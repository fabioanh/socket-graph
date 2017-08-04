package com.collibra.fabio.graph;

/**
 * Representation of a Node. Contains the name/id of the object
 * 
 * @author fabio
 *
 */
public class Node {

	private String name;

	public Node() {
	}

	public Node(String name) {
		this.name = name;
	}

	// --- GETTERS AND SETTERS ---

	public String getName() {
		return name;
	}

	// --- HASH CODE & EQUALS ---

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
