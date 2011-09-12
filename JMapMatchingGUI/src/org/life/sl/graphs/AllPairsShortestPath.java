package org.life.sl.graphs;

import java.util.Collection;
import java.util.HashMap;

import com.vividsolutions.jts.operation.linemerge.LineMergeEdge;
import com.vividsolutions.jts.planargraph.Node;

/**
 * Implementation of Floyd-Warshall
 * @author Uffe Gram Christensen
 * @author Pimin Konstantin Kefaloukos
 *
 */
public class AllPairsShortestPath {

	HashMap<Node, HashMap<Node, Double>> distances;
	
	public AllPairsShortestPath(PathSegmentGraph graph) {
		distances = new HashMap<Node, HashMap<Node, Double>>();
		Collection<Node> nodes = graph.getNodes();

		// initialize matrix
		for(Node node1 : nodes) {
			distances.put(node1, new HashMap<Node, Double>());
			for(Node node2 : nodes) {
				(distances.get(node1)).put(node2, getDirectDistance(node1, node2));
			}
		}
		
		// Floyd-Warshall
		for(Node node1 : nodes) {
			for(Node node2 : nodes) {
				for(Node node3 : nodes) {
					double distance = Math.min(getDistance(node2, node3), getDistance(node2, node1) + getDistance(node1, node3));
					this.setDistance(node2, node3, distance);
				}
			}
		}
	}
	
	public double getDistance(Node node1, Node node2) {
		return (distances.get(node1)).get(node2);
	}
	
	private void setDistance(Node node1, Node node2, double distance) {
		(distances.get(node1)).put(node2, distance);
	}
	
	
	/**
	 * @param node1
	 * @param node2
	 * @return The shortest direct distance between node1 and node2, or Double.MAX_VALUE if no direct connection exists.
	 */
	private double getDirectDistance(Node node1, Node node2) {
		double distance = Double.MAX_VALUE;
		if(node1 == node2) {
			distance = 0.0;
		}
		for(Object obj : Node.getEdgesBetween(node1, node2)) {
			LineMergeEdge edge = (LineMergeEdge) obj;
			distance = Math.min(distance, edge.getLine().getLength());
		}
		return distance;
	}
}
