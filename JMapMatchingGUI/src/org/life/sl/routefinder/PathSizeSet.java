package org.life.sl.routefinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Edge;

public class PathSizeSet {

	private ArrayList<Label> labels;
	private HashMap<Edge, Integer> edgeOccurrences = new HashMap<Edge, Integer>();
	
	public PathSizeSet(ArrayList<Label> labels0) {
		labels = labels0;
		calcPathSizeAttributes();
	}
	
	/**
	 * initialize the edge counters (in how many labels (routes) each edge is contained)
	 */
	private void initEdges() {
		for (Label label : labels) {
			List<DirectedEdge> edges = label.getRouteAsEdges();
			for (DirectedEdge de : edges) {
				Edge e = de.getEdge();
				if (edgeOccurrences.containsKey(e)) edgeOccurrences.put(e, edgeOccurrences.get(e) + 1);
				else edgeOccurrences.put(e, 1);
			}
		}
		// now we have a HashMap containing the Edges as keys and the number of their occurrence as values
	}
	
	/**
	 * Calculate the Path Size Attribute for all labels (routes)
	 */
	public void calcPathSizeAttributes() {
		initEdges();
		for (Label label : labels) {
			label.getPathSize_global(edgeOccurrences);
			// additionally, we could store the PS in a HashMap here?
		}
	}
}
