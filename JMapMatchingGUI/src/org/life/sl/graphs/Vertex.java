package org.life.sl.graphs;

import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Node;

class Vertex extends com.vividsolutions.jts.planargraph.Node implements Comparable<Vertex> {
	public double minDistance = Double.POSITIVE_INFINITY;
	public Vertex previous;
	public DirectedEdge backEdge;
	public double edgeLength = 0.;

	public Vertex(Node n) {
		super(n.getCoordinate(), n.getOutEdges());	// invoke parent constructor
		backEdge = null;
		previous = null;
		minDistance = Double.POSITIVE_INFINITY;
	}

	public boolean equals(Node n) {
		return (n.getCoordinate() == this.getCoordinate());
	}
	
	public int compareTo(Vertex other) {
		return Double.compare(minDistance, other.minDistance);
	}
}
