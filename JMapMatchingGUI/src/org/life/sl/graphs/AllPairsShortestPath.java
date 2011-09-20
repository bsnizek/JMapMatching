package org.life.sl.graphs;

import java.util.ArrayList;
import java.util.HashMap;

import com.vividsolutions.jts.operation.linemerge.LineMergeEdge;
import com.vividsolutions.jts.planargraph.Node;

/**
 * Implementation of Floyd-Warshall algorithm
 * @author Uffe Gram Christensen
 * @author Pimin Konstantin Kefaloukos
 * @author Bernhard Barkow
 *
 */
public class AllPairsShortestPath {

	private static boolean bShowProgress = true;		///> show progress during computation?
	private static double kShowProgressInterval1 = 2.5;	///> progress indicator is only updated after this interval, not faster
	private static double kShowProgressInterval2 = 5.; 

	HashMap<Node, HashMap<Node, Double>> distances;		///> container for the distances; a simple array would probably perform much better...
	double dist[][];
	
	public HashMap<Node, HashMap<Node, Double>> getDistances() {
		return distances;
	}
	// TODO: decide which array to use
	public double[][] getDistancesArr() {
		return dist;
	}
	
	public AllPairsShortestPath(PathSegmentGraph graph) {
		distances = new HashMap<Node, HashMap<Node, Double>>();
		ArrayList<Node> nodes = graph.getNodes();
		dist = new double[nodes.size()][nodes.size()];

		// initialize matrix: each element is assigned the direct distance
		if (bShowProgress) System.out.println("Initializing AllPairsShortestPath-Matrix...");
		int i = 0, j = 0;
		for(Node node1 : nodes) {
			j = 0;
			distances.put(node1, new HashMap<Node, Double>());
			for(Node node2 : nodes) {
				dist[i][j] = getDirectDistance(node1, node2);
				(distances.get(node1)).put(node2, getDirectDistance(node1, node2));
				j++;
			}
			i++;
		}
		
		// Floyd-Warshall
		if (bShowProgress) System.out.println("Starting Floyd-Warshall search...");
		double distance;
		double ni = 0;
		double n = (double)(nodes.size());
		double nn = n*n, nnn = nn*n;
		long t_start = System.nanoTime();
		double t_tot = 0., t_tot_last1 = 0., t_tot_last2 = 0.;
		for(Node nodeK : nodes) {
			i = 0;
			for(Node nodeI : nodes) {
				j = 0;
				for(Node nodeJ : nodes) {
					distance = Math.min(getDistance(nodeI, nodeJ), getDistance(nodeI, nodeK) + getDistance(nodeK, nodeJ));
					this.setDistance(nodeI, nodeJ, distance);
					dist[i][j] = distance;
					j++;
					ni++;
				}
				i++;
				t_tot = (double)(System.nanoTime() - t_start) * 1.e-9;
				if (bShowProgress) {	// inner loop progress indicator
					if (t_tot - t_tot_last1 > kShowProgressInterval1) {	// show indicator every x seconds
						System.out.print(".");
						t_tot_last1 = t_tot;
					}
				}
			}
			if (bShowProgress) {	// outer loop progress indicator
				if (t_tot - t_tot_last2 > kShowProgressInterval2) {	// show indicator every x seconds
					System.out.printf(" %f%%\n", 100.*i/n);
					t_tot_last2 = t_tot;
				}
			}
		}
		if (bShowProgress) System.out.println("Floyd-Warshall finished - computed " + (int)ni + " distances in " + t_tot + "s (" + ni/t_tot + "/s)");
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
