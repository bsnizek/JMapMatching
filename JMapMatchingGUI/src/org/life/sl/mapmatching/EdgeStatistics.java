package org.life.sl.mapmatching;

import java.util.ArrayList;
import java.util.HashMap;

import org.life.sl.routefinder.RouteFinder;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Edge;

/**
 * EdgeStatistics is a statistics of how many points are associated with each edge
 * @author Bernhard Barkow
 * @author Bernhard Snizek
 */
public class EdgeStatistics {

	HashMap<Edge, Integer> edgePoints = new HashMap<Edge, Integer>();	///> container counting the number of points associated with each edge
	
	/**
	 * default constructor
	 */
	public EdgeStatistics() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * constructer which initializes the EdgeStatistics with a network and a set of GPS points
	 * @param rf RouteFinder which contains the routes (edge network)
	 * @param gpsPoints array of GPS points for which the statistics is created (measure how well the points fit to edges)
	 */
	public EdgeStatistics(RouteFinder rf, ArrayList<Point> gpsPoints) {
		init(rf, gpsPoints);
	}
	
	/**
	 * initialize the EdgeStatistics with a network and a set of GPS points
	 * @param rf RouteFinder which contains the routes (edge network)
	 * @param gpsPoints array of GPS points for which the statistics is created (measure how well the points fit to edges)
	 */
	public void init(RouteFinder rf, ArrayList<Point> gpsPoints) {
		rf.setEdgeStatistics(this);
		// first, clear the statistics:
		edgePoints.clear();
		// then, loop over all GPS points:
		for (Point p : gpsPoints) {
			addPoint(rf.getNearestEdge(p));	// add the point to the associated edge (the edge nearest to each GPS data point)
		}
	}
	
	/**
	 * add a new edge to the statistics table and initialize its counter with 0
	 * @param e the edge to add
	 */
	public void addEdge(Edge e) {
		edgePoints.put(e, 0);
	}
	
	/**
	 * add a point to an associated edge; 
	 * if the edge is not contained in the statistics yet, it is initialized
	 * @param e
	 */
	public void addPoint(Edge e) {
		if (e != null) {
			if (!edgePoints.containsKey(e)) edgePoints.put(e, 1);
			else edgePoints.put(e, edgePoints.get(e) + 1);
		}
	}
	
	/**
	 * @param e the edge whose number of associated points is requested
	 * @return the edge/point-count (the number of points associated with an edge); if the edge does not yet exist in the statistics, 0 is returned
	 */
	public int getCount(Edge e) {
		return (edgePoints.containsKey(e) ? edgePoints.get(e) : 0 );
	}
}
