package org.life.sl.graphs;


/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

import java.io.IOException;
//import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
//import java.util.List;

//import org.life.sl.routefinder.Label;
//import org.life.sl.routefinder.RouteFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
//import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.Node;

/**
 * A planar graph of edges that is analyzed to sew the edges together. The 
 * <code>marked</code> flag on @{link com.vividsolutions.planargraph.Edge}s 
 * and @{link com.vividsolutions.planargraph.Node}s indicates whether they have been
 * logically deleted from the graph.
 *
 * @version 1.7
 */
public class PathSegmentGraph {

	private double xMin,xMax,yMin,yMax;
	
	// algorithms
	private AllPairsShortestPath allPairsShortestPath;
	private boolean distancesCalculated;
	private LineMergeGraphH4cked lineMergeGraphH4cked;
	
//	private GlobalRegister gr = GlobalRegister.getInstance();
	
	
	public PathSegmentGraph() {
		super();
		distancesCalculated = false;
		setLineMergeGraphH4cked(new LineMergeGraphH4cked());
	}
	
	public PathSegmentGraph(String shapeFile) throws IOException {
		super();
		distancesCalculated = false;
		setLineMergeGraphH4cked(new LineMergeGraphH4cked());
		addLineStringsFromShape(shapeFile);
	}

	/**
	 * Create a new graph, with linestring read from a shapefile 
	 * @param shapeFile
	 * @throws IOException
	 */
	public void addLineStringsFromShape(String shapeFile) throws IOException {
		setLineMergeGraphH4cked(new LineMergeGraphH4cked());
		distancesCalculated = false;
		LineStringReader reader = new LineStringReader(shapeFile);
		
		reader.read();
		boolean first = true;
		for(LineString ls : reader.getLineStrings()) {
			if(first == true) {
				xMax = xMin = ls.getCoordinate().x;
				yMax = yMin = ls.getCoordinate().y;
				first = false;
			}

			addLineString(ls);
		}
	}
	
	/**
	 * Adds an Edge, DirectedEdges, and Nodes for the given LineString representation
	 * of an edge. Snaps all vertices according to GlobalRegister.GLOBAL_SNAP
	 */
	/**
	 * @param lineString
	 */
	public void addLineString(LineString lineString) {

		distancesCalculated = false;
		
		if (lineString.isEmpty()) { return; }
		if(lineString.getCoordinates().length == 1) {
			System.exit(1);
		}
		
		Coordinate[] coordinates = lineString.getCoordinates();
		modifyEnvelope(coordinates);
		
		if (GlobalRegister.SNAP) {
		
			for(Coordinate c : coordinates) {
				c.x = c.x - (c.x % GlobalRegister.GLOBAL_SNAP);
				c.y = c.y - (c.y % GlobalRegister.GLOBAL_SNAP);
			}
		}

		Edge edge = getLineMergeGraphH4cked().addEdge(lineString);
		edge.setData(lineString.getUserData());
	}

	private void modifyEnvelope(Coordinate[] coordinates) {
		for(Coordinate c : coordinates) {
			if(c.x < xMin) xMin = c.x;
			if(c.x > xMax) xMax = c.x;
			if(c.y < yMin) yMin = c.y;
			if(c.y > yMax) yMax = c.y;
		}
	}

	/**
	 * Get the distance from one Node to another Node in the graph.
	 * @param from The Node to get the distance from.
	 * @param to The Node to get the distance to.
	 * @return The distance between two Nodes
	 */
	public double getDistance(Node from, Node to) {
		if(!distancesCalculated) {
			allPairsShortestPath = new AllPairsShortestPath(this);
			distancesCalculated = true;
		}
		return allPairsShortestPath.getDistance(from, to);
	}
	
	public void calculateDistances() {
		if(!distancesCalculated) {
			allPairsShortestPath = new AllPairsShortestPath(this);
			distancesCalculated = true;
		}
	}
	
	/**
	 * Find the node in the graph that is nearest the query coordinate. Implemented as linear search, can be vastly improved using e.g. a kd-tree
	 * @param query The Coordinate used for the query.
	 * @return The Node in the graph that is nearest the query Coordinate.
	 */
	public Node findClosestNode(Coordinate query) {

		double bestDistance = Double.MAX_VALUE;
		Node closestNode = null;
		for(Node n : getNodes()) {
			double currentDistance = n.getCoordinate().distance(query);
			if(currentDistance < bestDistance) {
				closestNode = n;
				bestDistance = currentDistance;
			}
		}
		return closestNode;
	}

	public Collection<Node> getNodes() {
		Collection<Node> nodes = new ArrayList<Node>();
		for(Object obj : getLineMergeGraphH4cked().getNodes()) {
			nodes.add((Node)obj);
		}
		return nodes;
	}
	
	public Envelope getEnvelope() {
		Envelope env = new Envelope(xMin, xMax, yMin, yMax);
		return env;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Edge> getEdges() {
		return (Collection<Edge>) getLineMergeGraphH4cked().getEdges();
	}

	public LineMergeGraphH4cked getLineMergeGraphH4cked() {
		return lineMergeGraphH4cked;
	}

	public void setLineMergeGraphH4cked(LineMergeGraphH4cked lineMergeGraphH4cked) {
		this.lineMergeGraphH4cked = lineMergeGraphH4cked;
	}
	
}
