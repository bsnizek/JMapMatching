package org.life.sl.routefinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Edge;

public class PathSizeSet {

	private ArrayList<Label> labels;
	private HashMap<Integer, Integer> edgeOccurrences = new HashMap<Integer, Integer>();
	
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
				// use the edge ID as key for the HashMap 
				// (this is better than using the Edge itself, because these change between runs (due to SplitGraphAtPoint etc.)):
				@SuppressWarnings("unchecked")
				HashMap<String, Object> userdata = (HashMap<String, Object>) e.getData();	// the user data object of the Edge
				Integer edgeID = (Integer)userdata.get("id");
				if (edgeOccurrences.containsKey(edgeID)) edgeOccurrences.put(edgeID, edgeOccurrences.get(edgeID) + 1);
				else edgeOccurrences.put(edgeID, 1);
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
