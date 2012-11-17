/*
JMapMatcher

Copyright (c) 2011 Bernhard Barkow, Hans Skov-Petersen, Bernhard Snizek and Contributors

mail: bikeability@life.ku.dk
web: http://www.bikeability.dk
*/

package org.life.sl.graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.List;

import org.apache.log4j.Logger;
import org.life.sl.mapmatching.EdgeStatistics;
import org.life.sl.routefinder.Label;
import org.life.sl.utils.Timer;

import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Node;
import com.vividsolutions.jts.operation.linemerge.LineMergeEdge;

public class Dijkstra {
	// all methods are defined static - TODO: check if that makes sense!

	private static Logger logger = Logger.getLogger("JMapMatcher");
	static List<Vertex> vertices = new ArrayList<Vertex>();
	
	public static void init(PathSegmentGraph graph) {
		vertices.clear();
		List<Node> nodes = graph.getNodes();
		for (Node n : nodes) {
			vertices.add(new Vertex(n));
		}
	}
	
	public static void init(PathSegmentGraph graph, Node source) {
		init(graph);
		computePaths(findVertex(source));
	}
	
    public static void computePaths(Vertex source) {
		source.minDistance = 0.;
		PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
		vertexQueue.add(source);

		int i = 0;
		logger.info("Dijkstra / shortest path - initializing ");
		Timer timer = new Timer();	// timer to observe total runtime
		timer.init();
		while (!vertexQueue.isEmpty()) {
			Vertex u = vertexQueue.poll();

			// Visit each edge exiting u
			@SuppressWarnings("unchecked")
			List<DirectedEdge> edges = (List<DirectedEdge>) u.getOutEdges().getEdges();
			for (DirectedEdge e : edges) {
				Vertex v = findVertex(e.getToNode());
				double weight = ((LineMergeEdge)e.getEdge()).getLine().getLength();
				double distanceThroughU = u.minDistance + weight;
				if (distanceThroughU < v.minDistance) {
					vertexQueue.remove(v);
					v.minDistance = distanceThroughU;
					v.previous = u;
					v.backEdge = e;
					v.edgeLength = weight;
					vertexQueue.add(v);	// push to queue again (in right order), for the next iteration
				}
			}
			i++;
			timer.showProgress((double)i/(double)vertices.size());
		}
		timer.getRunTime(true, "Dijkstra / shortest path finished: " + i + " steps");
	}
    
    public static void computePaths(Node n) {
    	Vertex v = new Vertex(n);
    	computePaths(v);
    }

    public static Vertex findVertex(Node n) {
    	for (Vertex v : vertices) {
    		if (v.equals(n)) return v;
    	}
		return null;
    }
    
	/**
	 * retrieve the shortest path from source to target 
	 * @param target the destination Node
	 * @return list of vertices from source to target
	 */
	public static List<Vertex> getShortestPathTo(Vertex target) {
		List<Vertex> path = new ArrayList<Vertex>();
		for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
			if (vertex.backEdge != null) path.add(vertex);
		Collections.reverse(path);
		return path;
	}
	/**
	 * the same for a Node as argument
	 * @param target the destination Node
	 * @return list of vertices from source to target
	 */
	public static List<Vertex>getShortestPathTo(Node target) {
		return getShortestPathTo(findVertex(target));
	}

	public static List<DirectedEdge> getShortestPathTo_Edges(Node target) {
		List<Vertex> path = getShortestPathTo(target);
		List<DirectedEdge> edgeList = new ArrayList<DirectedEdge>();
		for (Vertex v : path) {
			if (v.backEdge != null) edgeList.add(v.backEdge);
		}
		return edgeList;
	}
	
	public static Label getShortestPathTo_Label(Node target, EdgeStatistics edgeStatistics) {
		List<Vertex> path = getShortestPathTo(target);
		Label label = new Label(path.get(0));
		for (Vertex v : path) {
			label = new Label(label, (Node)v, v.backEdge, label.getLength() + v.edgeLength, v.edgeLength);
			if (edgeStatistics != null) label.calcScore(edgeStatistics);
		}
		return label;
	}
}
