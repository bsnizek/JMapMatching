package org.life.sl.routefinder;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;

import java.util.ArrayList;
//import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Properties;

import java.util.Stack;

import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.routefinder.Label;

//import org.life.sl.shapefilereader.ShapeFileReader;

//import cern.jet.random.Uniform;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;

import com.infomatiq.jsi.test.SpatialIndexFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import com.vividsolutions.jts.operation.linemerge.LineMergeEdge;
//import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.Node;

import org.geotools.graph.path.Path;
import org.geotools.graph.path.AStarShortestPathFinder;
import org.geotools.graph.traverse.standard.AStarIterator;
import org.geotools.graph.traverse.standard.AStarIterator.AStarNode;

/**
 * A Routefinder. Finds all routes in a graph from origin to destination.
 * 
 * @author Pimin Kostas Kefaloukos
 * @author Bernhard Snizek
 * @author Bernhard Barkow
 */
public class RouteFinder {
	// Initialization:
	Constraints constraints;// = new Constraints();
	
	private Node startNode = null;	///> route start node (Origin)	
	private Node endNode = null;	///> route end node (Destination)
	private PathSegmentGraph network = null;	///> graph to search (all routes are in this network)
	// articulation points identified in graph
//	private Collection<Node> articulationPoints = new ArrayList<Node>();
	// bridges identified in graph
//	private Collection<Edge> bridges = new ArrayList<Edge>();

	private int numLabels = 0;	///> number of labels (states) generated when running algorithm
	private SpatialIndex si;
	private HashMap<Integer, Edge> counter__edge;
	TIntObjectHashMap m_map = new TIntObjectHashMap();
	
	private double shortestPathLength = 0;
	private boolean alwaysUseShortestPath = false;
	
	/**
	 * create a Routefinder from a PathSegmentGraph
	 * @param network the input PathSegmentGraph
	 */
	public RouteFinder(PathSegmentGraph network) {
		// initialize constraint fields:
		constraints = new Constraints();

		constraints.setInt(Constraints.Type.MaximumNumberOfRoutes, 100);	///> maximum number of routes to find
		constraints.setInt(Constraints.Type.BridgeOverlap, 1);
		constraints.setInt(Constraints.Type.EdgeOverlap, 1);		///> how often each edge may be used
//		constraints.setInt(Constraints.Type.ArticulationPointOverlap, 2);
		constraints.setInt(Constraints.Type.NodeOverlap, 1);		///> how often each single node may be crossed
		constraints.setInt(Constraints.Type.AlwaysUseShortestPath, 0);	///> if the shortest path is calculated for each label (0=no)
		constraints.setDouble(Constraints.Type.DistanceFactor, 1.2);	///> how much the route may deviate from the shortest possible
		constraints.setDouble(Constraints.Type.MinimumLength, 0.0);		///> minimum route length
		constraints.setDouble(Constraints.Type.MaximumLength, 1.e20);	///> maximum route length (quasi no limit here)
		
		this.network = network;	// set network for further use
	}
	
	/**
	 * external interface to set constraints for the algorithm
	 * @param ic HashMap of integer constraints
	 * @param dc HashMap of double constraints
	 */
	public void setConstraints(HashMap<Constraints.Type, Integer> ic, HashMap<Constraints.Type, Double> dc) {
		constraints.setConstraints(ic, dc);
	}

	/**
	 * Find all routes between startNode and endNode in the network this.network as list of labels; 
	 * Label.getRouteAsEdges() can then be used to get a list of DirectedEdges that represent the route.
	 * @param startNode Routes should start in this node
	 * @param endNode Routes should end in this node
	 * @return A list of the routes found (represented by a list of Labels) 
	 */
	public Vector<Label> findRoutes(Node startNode, Node endNode) {

		// check that startNode and endNode belong to same graph
		if (network.findClosestNode(startNode.getCoordinate()) == null ||
				network.findClosestNode(endNode.getCoordinate()) == null) {
			System.out.println("---- origin and destination are not in network!");	// indicate failure
			return new Vector<Label>();
		}

		///////////////////////////////////////////////
		// INITIALIZE
		///////////////////////////////////////////////

		this.startNode = startNode;
		this.endNode = endNode;
		numLabels = 0;
		Vector<Label> result = new Vector<Label>();
		Stack<Label> stack = new Stack<Label>();

		// precalculate the minimum path length, for use as a constraint in label expansion:
		shortestPathLength = shortestDistanceBetweenNodes(startNode, endNode);
		// if there was a problem, use the given maximum length constraint:
		double minDist = startNode.getCoordinate().distance(endNode.getCoordinate());	// compare to Euclidian distance
		if (shortestPathLength < minDist) shortestPathLength = constraints.getDouble(Constraints.Type.MaximumLength) / constraints.getDouble(Constraints.Type.DistanceFactor);
		System.out.println("Shortest path length = " + shortestPathLength);

		alwaysUseShortestPath = (constraints.getInt(Constraints.Type.AlwaysUseShortestPath) != 0);

		///////////////////////////////////////////////
		// START ALGORITHM
		///////////////////////////////////////////////

		stack.push(new Label(startNode));	// push start node to stack

		//////////////////////////////////////////////
		// ALGORITHM MAIN LOOP
		//////////////////////////////////////////////

		while (!stack.empty()) {

			/////////////////////////////////////////
			// CREATE NEW LABELS
			/////////////////////////////////////////

			Label expandingLabel = stack.pop();	// get last label from stack and expand it:
			List<Label> expansion = expandLabel(expandingLabel);	// calculate the expansion of the label (continuation from last node along all available edges)
			Collections.shuffle(expansion);		// randomize the order in which the labels are treated
	
			/////////////////////////////////////////
			// CHECK NEW LABELS
			/////////////////////////////////////////

			int nMaxRoutes = constraints.getInt(Constraints.Type.MaximumNumberOfRoutes);
			for (Label currentLabel : expansion) {	// loop over all next-generation labels
				// is label a new valid route?
				if (isValidRoute(currentLabel))	{	// valid route means: it ends in the destination node and fulfills the length constraints
					result.add(currentLabel);		// add the valid route to list of routes
					if (nMaxRoutes > 0 && result.size() >= nMaxRoutes) {	// stop after the defined max. number of routes
						System.out.println("Maximum number of routes reached (Constraint.MaximumNumberOfRoutes = " + constraints.getInt(Constraints.Type.MaximumNumberOfRoutes) + ")");
						return result;
					}
				}
				stack.push(currentLabel);	// store on stack for the next iteration
			}
		}
		if (result.isEmpty()) result.add(new Label(startNode));	// if nothing was found, return only the start node
		return result;
	}

	/**
	 * Checks if the labeled route is valid, i.e. if it ends in the end node and fulfills the length constraints
	 * @param label the label to check
	 * @return true if the route is valid 
	 */
	private boolean isValidRoute(Label label) {	
		return (	
			label.getNode() == endNode &&
			label.getLength() >= constraints.getDouble(Constraints.Type.MinimumLength) &&
			label.getLength() <= constraints.getDouble(Constraints.Type.MaximumLength)
		);
	}

	/**
	 * create the expansion from a label, i.e. the list of routes leaving from the current node (next generation labels);
	 * ignore expansion along the backEdge! (BB)
	 * @param parentLabel the label where from to expand
	 * @return
	 */
	private List<Label> expandLabel(Label parentLabel) {
		ArrayList<Label> expansion = new ArrayList<Label>();

		Label newLabel;

		/////////////////////////////////////
		// MAKE LABELS FROM NEIGHBORS
		/////////////////////////////////////			

		for(Object obj : 
			parentLabel.getNode().getOutEdges().getEdges()) {	// loop over all edges leaving from the current node

			DirectedEdge currentEdge = (DirectedEdge) obj;
			// check if the current edge is identical to the parent node, where we just came from:
			DirectedEdge pe = parentLabel.getBackEdge();
			if (pe != null && currentEdge.getEdge() == pe.getEdge()) continue;	// going back the same edge where we come from is forbidden!

			double length = parentLabel.getLength() + ((LineMergeEdge)currentEdge.getEdge()).getLine().getLength();	// new length
			newLabel = new Label(parentLabel, currentEdge.getToNode(), currentEdge, length);

			/////////////////////////////////////
			// EDGE CONSTRAINTS
			/////////////////////////////////////

//			int edgeOccurances = newLabel.getOccurancesOfEdge(currentEdge);

//			// bridge has been visited the maximum number of times already?
//			if(this.bridges.contains(currentEdge) && (edgeOccurances > getIntegerConstraint(Constraint.BridgeOverlap))) {
//				continue;
//			}

//			// edge has been visited the maximum number of times already?
//			if(!this.bridges.contains(currentEdge) && edgeOccurances > getIntegerConstraint(Constraint.EdgeOverlap)) {
//				continue;
//			}

			/////////////////////////////////////
			// PATH-LENGTH CONSTRAINTS
			/////////////////////////////////////			

			double maxLen = shortestPathLength * constraints.getDouble(Constraints.Type.DistanceFactor);	// use exact estimate for maximum path length
			// 1. path length exceeded maxLength constraint with this edge?
			if (checkPathLength(length, maxLen, "length")) continue;	// don't store the label at all, because the route is too long anyway

			// 2. Euclidian distance to endNode greater than maxLength? (can't reach endNode)
			// (Using the Euclidian distance is a quick worst-case check; using the shortest path is exact, but slower.)
			double distanceToEndNode = newLabel.getNode().getCoordinate().distance(endNode.getCoordinate());
			if (checkPathLength(length + distanceToEndNode, maxLen, "eucl.")) continue;

			// 3. try with the shortest realistic path: AStar
			// TODO: check how this performs - it might be faster to calculate more labels instead of calculating the path distance every time!!
			if (alwaysUseShortestPath) {
				distanceToEndNode = shortestDistanceBetweenNodes(newLabel.getNode(), endNode);
				if (checkPathLength(length + distanceToEndNode, maxLen, "AStar")) continue;
			}

			/////////////////////////////////////
			// NODE CONSTRAINTS
			/////////////////////////////////////						

			// rules for node occurrences: 
			// 1) the start-node is counted except in the beginning of the route
			// 2) the end-node is counted except if at the end of the route
			// 3) all other occurances are counted

			// get current total overlap for node
			int nodeOccurrences = newLabel.getOccurrencesOfNode(newLabel.getNode());

			// adjust the number of time the node occours, according to cases above
			if (newLabel.getNode() == startNode) nodeOccurrences--;
			if (newLabel.getNode() == endNode) nodeOccurrences--;

			// Now, check if a constraint is broken:
//			if(this.articulationPoints.contains(newLabel.getNode())) {	// node is an articulation point, so we check for a special max. overlap:
//				if (nodeOccurances > getIntegerConstraint(Constraint.ArticulationPointOverlap)) continue;
//			} else {	// node is not an articulation point, so we have to check for max. overlap:
				if (nodeOccurrences > constraints.getInt(Constraints.Type.NodeOverlap)) {
					System.out.println("Constraint reached: NodeOccurrences = " + nodeOccurrences);
					continue;	// don't consider this label
				}
//			}

			// no constraints broken:
			this.numLabels++; 			// debug info
			expansion.add(newLabel);	// store newLabel (i.e., it is a valid expansion)
		}
		return expansion;		
	}
	
	/**
	 * check if the given minimum path length exceeds the limit, and write out a corresponding log message
	 * @param len minimum (or real) path length
	 * @param maxLen path length limit
	 * @param msg additional description for log message
	 * @return true if the path is too long
	 */
	private boolean checkPathLength(double len, double maxLen, String msg) {
		boolean b = (len > maxLen);
		if (b) {
			System.out.println("Constraint reached: distance too large (" + msg + "). length > " + len);
		}
		return b;
	}

	/**
	 * calculate the shortest distance between two nodes in the network using the A Star algorithm
	 * @param srcNode source node
	 * @param destNode destinateion node
	 * @return the minimum distance between the two nodes
	 */
	public double shortestDistanceBetweenNodes(Node srcNode, Node destNode) {
		double d = 0;
		AStarIterator.AStarFunctions afuncs = new AStarIterator.AStarFunctions(target) {
			public double cost(AStarNode n1, AStarNode n2){
				return 1;
			}
			public double h(Node n){
				double s = n.getCoordinate().distance((Node)(this.getDest()).g ;
				return s;
				//return Double.POSITIVE_INFINITY;
			}
       	};
		AStarShortestPathFinder asp = new AStarShortestPathFinder(network, srcNode, destNode, afuncs);
		return d;		
	}
	
	/**
	 * @return Get the number of Labels generated when finding routes during the last call to findRoutes()
	 */
	public int getNumLabels() {
		return numLabels;
	}
	
	/**
	 * ??
	 * set up SpatialIndex, which is then used to calculate nearest edges (?)
	 */
	public void calculateNearest() {
		Properties p = new Properties();
	    p.setProperty("MinNodeEntries", "1");
	    p.setProperty("MaxNodeEntries", "10");
		si = SpatialIndexFactory.newInstance("rtree.RTree", p);
		counter__edge = new HashMap<Integer, Edge>();
		int counter = 0;
		// loop over all edges in the network
		for (Edge edge : network.getEdges()) {
			counter++;
			counter__edge.put(counter, edge);	// store edges in the hash map
			LineMergeEdge de = (LineMergeEdge)edge;
			Geometry env = de.getLine().getBoundary();
			//  (minx, miny), (maxx, miny), (maxx, maxy), (minx, maxy), (minx, miny).
			
			if (env.getNumGeometries() > 0) {			
				Point p1 = (Point) env.getGeometryN(0);
				Point p2 = (Point) env.getGeometryN(1);
	
				si.add(new Rectangle((float) p1.getX(),
									 (float) p1.getY(), 
									 (float) p2.getX(), 
									 (float) p2.getY()), 
									 counter);	
			}
		}
	}
	
	/**
	 * Find the edge nearest to a given point
	 * @param p point for which to calculate the nearest edge
	 * @return the nearest edge to the point
	 */
	public Edge getNearestEdge(Point p) {
		com.infomatiq.jsi.Point pp = new com.infomatiq.jsi.Point((float) p.getX(), (float) p.getY());
	
		Return r = new Return();
		this.si.nearest(pp, r, 10.f);	// TODO: decide how to choose value for furthestDistance? 10 meters is just a guess.
		return (Edge) this.counter__edge.get(r.getResult());
	}

}

/**
 * Result container; looks like a bit of overkill, but is required by SpatialIndex...
 */
class Return implements TIntProcedure {

	private int result;
	
	public boolean execute(int value) {
      this.result = value;
      return true;
    }
	
	int getResult() {
		return this.result;
	}
}

