package org.life.sl.routefinder;

import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import java.util.Stack;

import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.mapmatching.EdgeStatistics;
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

/**
 * A Routefinder. Finds all routes in a graph from origin to destination.
 * 
 * @author Pimin Kostas Kefaloukos
 * @author Bernhard Snizek
 * @author Bernhard Barkow
 */
public class RouteFinder {
	private static enum LabelTraversal {
		None,			///> no special sorting
		Shuffle,		///> shuffle labels before advancing in the iteration
		BestFirst,		///> traverse label in reverse natural order (highest score first) - this should find the globally best route first
		WorstFirst,		///> traverse label in natural order (highest score last)
		BestLastEdge,	///> traverse label in reverse natural order, considering only the last edge
	}

	private static boolean bLogAll = false;			///> output all warnings etc.?
	private static boolean bShowProgress = true;	///> show a progress indicator while finding routes?

	private LabelTraversal itLabelOrder = LabelTraversal.BestFirst;
	private float kNearestEdgeDistance = 100.f;	// the larger the slower
	
	// Initialization:
	private RFParams rfParams;// = new Constraints();
	
	private Node startNode = null;	///> route start node (Origin)	
	private Node endNode = null;	///> route end node (Destination)
	private PathSegmentGraph network = null;	///> graph to search (all routes are in this network)
	
	// articulation points identified in graph
//	private Collection<Node> articulationPoints = new ArrayList<Node>();
	// bridges identified in graph
//	private Collection<Edge> bridges = new ArrayList<Edge>();

	private long numLabels = 0;			///> number of labels (states) generated when running algorithm
	private long numLabels_rejected = 0;	///> number of labels (states) that have been rejected due to constraints
	private SpatialIndex si;
	private HashMap<Integer, Edge> counter__edge;
	
	private double gpsPathLength = 0.;
	private double maxPathLength = 0.;
	private EdgeStatistics edgeStatistics = null;

	/**
	 * create a Routefinder from a PathSegmentGraph, using default parameters
	 * @param network the input PathSegmentGraph
	 */
	public RouteFinder(PathSegmentGraph network) {
		initDefaults();			// initialize algorithm parameters
		this.network = network;	// set network for further use
	}
	
	/**
	 * create a Routefinder from a PathSegmentGraph, using a defined parameter set
	 * @param network the input PathSegmentGraph
	 */
	public RouteFinder(PathSegmentGraph network, RFParams rfParams0) {
		this(network);
		rfParams = rfParams0;
	}
	
	/**
	 * create default parameter set for the algorithm 
	 */
	private void initDefaults() {
		rfParams = new RFParams();

		rfParams.setInt(RFParams.Type.MaximumNumberOfRoutes, 100);	///> maximum number of routes to find
		rfParams.setInt(RFParams.Type.BridgeOverlap, 1);
		rfParams.setInt(RFParams.Type.EdgeOverlap, 1);		///> how often each edge may be used
//		constraints.setInt(Constraints.Type.ArticulationPointOverlap, 2);
		rfParams.setInt(RFParams.Type.NodeOverlap, 1);		///> how often each single node may be crossed
		rfParams.setDouble(RFParams.Type.DistanceFactor, 1.2);	///> how much the route may deviate from the shortest possible
		rfParams.setDouble(RFParams.Type.MinimumLength, 0.0);		///> minimum route length
		rfParams.setDouble(RFParams.Type.MaximumLength, 1.e20);	///> maximum route length (quasi no limit here)
	}
	
	/**
	 * external interface to set constraints for the algorithm
	 * @param ic HashMap of integer constraints
	 * @param dc HashMap of double constraints
	 */
	public void setConstraints(HashMap<RFParams.Type, Integer> ic, HashMap<RFParams.Type, Double> dc) {
		rfParams.setConstraints(ic, dc);
	}
	
	public void setEdgeStatistics(EdgeStatistics eStat) {
		edgeStatistics = eStat;
	}

	public double getMaxPathLength() {
		return maxPathLength;
	}
	
	public double getGPSPathLength() {
		return gpsPathLength;
	}
	
	/**
	 * Find all routes between startNode and endNode in the network this.network as list of labels; 
	 * Label.getRouteAsEdges() can then be used to get a list of DirectedEdges that represent the route.
	 * @param startNode Routes should start in this node
	 * @param endNode Routes should end in this node
	 * @return A list of the routes found (represented by a list of Labels) 
	 */
	public ArrayList<Label> findRoutes(Node startNode, Node endNode, double gpsPathLength0) {

		// check that startNode and endNode belong to same graph
		if (network.findClosestNode(startNode.getCoordinate()) == null ||
				network.findClosestNode(endNode.getCoordinate()) == null) {
			System.out.println("---- origin and/or destination are not in network!");	// indicate failure
			return new ArrayList<Label>();
		}

		///////////////////////////////////////////////
		// INITIALIZE
		///////////////////////////////////////////////

		this.startNode = startNode;
		this.endNode = endNode;
		this.gpsPathLength = gpsPathLength0;
		numLabels = 0;
		int numDeadEnds = 0;
		ArrayList<Label> result = new ArrayList<Label>();
		Stack<Label> stack = new Stack<Label>();

		// precalculate the minimum path length, for use as a constraint in label expansion:
		maxPathLength = gpsPathLength * rfParams.getDouble(RFParams.Type.DistanceFactor);
		// if there was a problem, use the given maximum length constraint:
		double minDist = startNode.getCoordinate().distance(endNode.getCoordinate());	// compare to Euclidian distance
		if (maxPathLength < minDist) {
			System.err.println("Warning: invalid GPS data? referencePathLength < minDist (" + maxPathLength + " < " + minDist + ")");
			maxPathLength = 0.;
		}
		System.out.println("Euclidian GPS Path length = " + gpsPathLength + ", path length limit = " + maxPathLength);

		System.out.println("Network size (nodes): " + network.getNodes().size());

		///////////////////////////////////////////////
		// START ALGORITHM
		///////////////////////////////////////////////

		stack.push(new Label(startNode));	// push start node to stack

		//////////////////////////////////////////////
		// ALGORITHM MAIN LOOP
		//////////////////////////////////////////////

		stackLoop:
		while (!stack.empty()) {
			// create label expansion (next generation):
			Label expandingLabel = stack.pop();	// get last label from stack and expand it:
			ArrayList<Label> expansion = expandLabel(expandingLabel);	// calculate the expansion of the label (continuation from last node along all available edges)
			if (expansion.size() > 0) {
				if (itLabelOrder == LabelTraversal.Shuffle) Collections.shuffle(expansion);				// randomize the order in which the labels are treated
				// Attention: sorting the labels affects two parts:
				// 1. the expansion array, which is processed linearly
				// 2. the stack, which is effectively processed in reverse order!
				else if (itLabelOrder == LabelTraversal.BestFirst) Collections.sort(expansion);			// order labels in ascending order (lowest score is treated first), so that the highest score ends up on top of the stack
				else if (itLabelOrder == LabelTraversal.WorstFirst) Collections.sort(expansion, Collections.reverseOrder());	// order labels in descending order (highest score first), so the best label ends up at the bottom of the stack
				else if (itLabelOrder == LabelTraversal.BestLastEdge) Collections.sort(expansion, new Label.LastEdgeComparator());
				//System.out.println(expansion.get(0).getScore(edgeStatistics)*1000. + "\t" + expandingLabel.getScore()*1000. + "\t" + expansion.size() + "\t" + treeLevel);
				//System.out.println(expandingLabel.getTreeLevel());
		
				// test the newly created labels:
				for (Label currentLabel : expansion) {	// loop over all next-generation labels
					//System.out.println(currentLabel.getLastScore());
					numLabels++;
					// is label a new valid route?
					if (isValidRoute(currentLabel))	{	// valid route means: it ends in the destination node and fulfills the length constraints
						result.add(currentLabel);		// add the valid route to list of routes
						System.out.println("## " + result.size());
						int nMaxRoutes = rfParams.getInt(RFParams.Type.MaximumNumberOfRoutes);
						if (nMaxRoutes > 0 && result.size() >= nMaxRoutes) {	// stop after the defined max. number of routes
							System.out.println("Maximum number of routes reached (Constraint.MaximumNumberOfRoutes = " + rfParams.getInt(RFParams.Type.MaximumNumberOfRoutes) + ")");
							break stackLoop;
						}
					}
					else stack.push(currentLabel);		// destination is not reached yet: store the label on stack for the next iteration
				}
				//System.out.println("--");
				
				if (bShowProgress) {	// some log output?
					if (numLabels%10000 == 0) System.out.print(".");
					if (numLabels%500000 == 0) System.out.println(numLabels + " - " + (expandingLabel.getLength() / gpsPathLength) + " - " + expandingLabel.getTreeLevel());
	//				System.out.println(stack.size() + " - " + (expandingLabel.getLength() / gpsPathLength));
				}
			} else {	// "Sackgasse" - this label is invalid
				numDeadEnds++;
			}
		}
		if (result.isEmpty()) result.add(new Label(startNode));	// if nothing was found, return only the start node
		// some statistics on the computation:
		System.out.printf("\nlabels analyzed:\t%14d\ndead end-labels:\t%14d\t(%2.2f%%)\nlabels rejected:\t%14d\t(%2.2f%%)\nnodes in network:\t%14d\n\n",
				numLabels, numDeadEnds, (100.*numDeadEnds/numLabels), numLabels_rejected, (100.*numLabels_rejected/numLabels), 
				network.getNodes().size());
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
			label.getLength() >= rfParams.getDouble(RFParams.Type.MinimumLength) &&
			label.getLength() <= rfParams.getDouble(RFParams.Type.MaximumLength)
		);
	}

	/**
	 * create the expansion from a label, i.e. the list of routes leaving from the current node (next generation labels);
	 * ignore expansion along the backEdge! (BB)
	 * @param parentLabel the label where from to expand
	 * @return
	 */
	private ArrayList<Label> expandLabel(Label parentLabel) {
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

			double lastEdgeLength = ((LineMergeEdge)currentEdge.getEdge()).getLine().getLength();	// length of new last edge
			double length = parentLabel.getLength() + lastEdgeLength;	// new total length
			newLabel = new Label(parentLabel, currentEdge.getToNode(), currentEdge, length, lastEdgeLength);
			numLabels_rejected++;	// increment by default, decrement again if label is not rejected
			
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

			double maxLen = rfParams.getDouble(RFParams.Type.MaximumLength);	// use exact estimate for maximum path length
			// 1. path length exceeded maxLength constraint with this edge?
			if (checkPathLength(length, maxLen, "length")) continue;	// don't store the label at all, because the route is too long anyway

			if (maxPathLength > 0.) {	// only if we have a valid path length
				// 2. Length + Euclidian distance to endNode greater than referencePathLength? (can't reach endNode)
				// (Using the Euclidian distance is a quick worst-case check; using the shortest path is exact, but slower.)
				double distanceToEndNode = newLabel.getNode().getCoordinate().distance(endNode.getCoordinate());
				if (checkPathLength(length + distanceToEndNode, maxPathLength, "eucl.")) continue;
	
				/*// 3. try with the shortest realistic path: AStar
				// TODO: get this value from database, after it has been precalculated and stored in a table
				// TODO: check how this performs - it might be faster to calculate more labels instead of calculating the path distance every time!!
				if (alwaysUseShortestPath) {
					distanceToEndNode = shortestDistanceBetweenNodes(newLabel.getNode(), endNode);
					if (checkPathLength(length + distanceToEndNode, referencePathLength, "AStar")) continue;
				}*/
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
				if (nodeOccurrences > rfParams.getInt(RFParams.Type.NodeOverlap)) {
					if (bLogAll) System.out.println("Constraint reached: NodeOccurrences = " + nodeOccurrences);
					continue;	// don't consider this label
				}
//			}

			// no constraints broken:
			newLabel.calcScore(edgeStatistics);	// shall we do this here and sort the list in findRoutes(), or shall we rather shuffle?
			expansion.add(newLabel);	// store newLabel (i.e., it is a valid expansion)
			numLabels++;
			numLabels_rejected--;		// inelegantly reset to previous state
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
		if (b && bLogAll) {
			System.out.println("Constraint reached: distance too large (" + msg + "). " + len + " > " + maxLen);
		}
		return b;
	}
	
	/**
	 * @return Get the number of Labels generated when finding routes during the last call to findRoutes()
	 */
	public long getNumLabels() {
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
		this.si.nearest(pp, r, kNearestEdgeDistance);	// TODO: decide how to choose value for furthestDistance? 10 meters is just a guess.
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

