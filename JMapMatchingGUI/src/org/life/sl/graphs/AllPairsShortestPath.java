package org.life.sl.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.life.sl.utils.Timer;

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
	private static float kShowProgressInterval1 = 2.5f;	///> progress indicator is only updated after this interval, not faster
	private static float kShowProgressInterval2 = 10.f; 

	private boolean bUndirectedEdges = true;
	
	private HashMap<Node, HashMap<Node, Float>> distances;		///> container for the distances; a simple array would probably perform much better...
	private float dist[][];
	
	private Timer timer = new Timer(kShowProgressInterval1, kShowProgressInterval2);
	
	public HashMap<Node, HashMap<Node, Float>> getDistances() {
		return distances;
	}
	// TODO: decide which array to use
	public float[][] getDistancesArr() {
		return dist;
	}
	
	public AllPairsShortestPath(PathSegmentGraph graph) {
		//distances = new HashMap<Node, HashMap<Node, Double>>();

		ArrayList<Node> nodes = graph.getNodes();
		int nNodes = nodes.size();
		dist = new float[nNodes][nNodes];
		Node[] nodesA = new Node[nNodes];
		int i = 0;
		for(Node node1 : nodes) {
			nodesA[i++] = node1;
		}

		double t_tot = 0.f;	// timing variables
		double t_start = timer.init();

		// initialize matrix: each element is assigned the direct distance
		if (bShowProgress) System.out.println("Initializing AllPairsShortestPath-Matrix...");
		int j = 0;
		float d = 0;
		i = 0;
		int j0 = 0;
		double ni = 0, nn2 = nNodes*nNodes;
		if (bUndirectedEdges) nn2 /= 2;
		Node node1, node2;
		
		int kNumThreads = 1, nIncr = (int)Math.round((double)nNodes / (double)kNumThreads + 1.);	// round up
		DistInnerLoop[] runners1 = new DistInnerLoop[kNumThreads];
		ExecutorService executor1 = Executors.newCachedThreadPool(); 

		for(i = 0; i < nNodes; i++) {
			node1 = nodesA[i];
			if (bUndirectedEdges) nIncr = (int)Math.round((double)(nNodes-i) / (double)kNumThreads + 1.);
			int nc = (bUndirectedEdges ? i: 0);
			for (j = 0; j < kNumThreads; j++) {
				runners1[j].init(i, nc, Math.min(nc+nIncr, nNodes), node1, nodesA);	// make sure the last thread does not exceed the range 
				executor1.execute(runners1[j]);
				nc += nIncr;
			}
			executor1.shutdown();
			ni += nNodes;
			if (bShowProgress) timer.showProgress(ni/nn2);	// outer loop progress indicator
		}
		
//		for (i = 0; i < nNodes; i++) {
//			node1 = nodesA[i];
//			j0 = (bUndirectedEdges ? i : 0);
//			//distances.put(node1, new HashMap<Node, Double>());
//			for (j = j0; j < nNodes; j++) {
//				node2 = nodesA[j];
//				d = (float)getDirectDistance(node1, node2);
//				dist[i][j] = d;
//				if (bUndirectedEdges && i != j) dist[j][i] = d;
//				//(distances.get(node1)).put(node2, getDirectDistance(node1, node2));
//				if (bShowProgress) timer.showProgress(0f);	// inner loop progress indicator
//				ni++;
//			}
//			if (bShowProgress) timer.showProgress(ni/nn2);	// outer loop progress indicator
//			//System.out.println(" - " + ni + " " + nn2 + " - ");
//		}

//		for(Node node1 : nodes) {
//			j = 0;
//			//distances.put(node1, new HashMap<Node, Double>());
//			for(Node node2 : nodes) {
//				d = (float)getDirectDistance(node1, node2);
//				dist[i][j] = d;
//				if (bUndirectedEdges) dist[j][i] = d;
//				//(distances.get(node1)).put(node2, getDirectDistance(node1, node2));
//				j++;
//				if (bShowProgress) timer.showProgress(0);	// inner loop progress indicator
//			}
//			i++;
//			if (bShowProgress) timer.showProgress(i/nNodes);	// outer loop progress indicator
//		}
		
		t_tot = timer.getRunTime(true, "Matrix initialization complete");

		// Floyd-Warshall
		if (bShowProgress) System.out.println("Starting Floyd-Warshall search...");
		ni = 0;
		double nn = nNodes*nNodes, nnn = nn*nNodes;
		float distance;
		t_start = timer.init(kShowProgressInterval1, 5.*kShowProgressInterval2);

		FloydWarshallInnerLoop[] runners = new FloydWarshallInnerLoop[kNumThreads];
		ExecutorService executor2 = Executors.newCachedThreadPool(); 

		for(int k = 0; k < nNodes; k++) {
			for(i = 0; i < nNodes; i++) {
				int nc = 0;
				for (j = 0; j < kNumThreads; j++) {
					runners[j].init(k, i, nc, Math.min(nc+nIncr, nNodes));	// make sure the last thread does not exceed the range 
					executor2.execute(runners[j]);
					nc += nIncr;
				}
				executor1.shutdown();
				ni += nNodes;
//				for(j = 0; j < nNodes; j++) {
//					dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
//					ni++;
//				}
				if (bShowProgress) timer.showProgress(0);	// inner loop progress indicator
			}
			if (bShowProgress) timer.showProgress(ni/nnn);	// outer loop progress indicator
		}
		

//		int k = 0;
//		for(Node nodeK : nodes) {
//			i = 0;
//			for(Node nodeI : nodes) {
//				j = 0;
//				for(Node nodeJ : nodes) {
//					//distance = Math.min(getDistance(nodeI, nodeJ), getDistance(nodeI, nodeK) + getDistance(nodeK, nodeJ));
//					//this.setDistance(nodeI, nodeJ, distance);
//					//dist[i][j] = distance;
//					dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
//					j++;
//					ni++;
//				}
//				i++;
//				if (bShowProgress) timer.showProgress(0);	// inner loop progress indicator
//			}
//			k++;
//			if (bShowProgress) timer.showProgress((float)i/nn);	// outer loop progress indicator
//		}
		if (bShowProgress) System.out.println("Floyd-Warshall finished - computed " + (int)ni + " distances in " + t_tot + "s (" + ni/t_tot + "/s)");
	}
	
	private class DistInnerLoop implements Runnable {
		private int idx_i, idx_k, idx_j0, idx_j1;
		Node node1 = null;
		Node[] nodesA;
		
		public void init(int i, int j0, int j1, Node node10, Node[] nodesA0) {
			idx_i = i;
			idx_j0 = j0;
			idx_j1 = j1;
			node1 = node10;
			nodesA = nodesA0;
		}
		public void run() {
			float d;
			Node node2;
			for (int j = idx_j0; j < idx_j1; j++) {
				node2 = nodesA[j];
				d = (float)getDirectDistance(node1, node2);
				dist[idx_i][j] = d;
				if (bUndirectedEdges && idx_i != j) dist[j][idx_i] = d;
			}
			
			for (int j = idx_j0; j < idx_j1; j++) {
				dist[idx_i][j] = Math.min(dist[idx_i][j], dist[idx_i][idx_k] + dist[idx_k][j]);
			}
		}
		
	}
	
	final class FloydWarshallInnerLoop implements Runnable {
		private int idx_i, idx_k, idx_j0, idx_j1;
		
		public void init(int k, int i, int j0, int j1) {
			idx_i = i;
			idx_k = k;
			idx_j0 = j0;
			idx_j1 = j1;
		}
		public void run() {
			for (int j = idx_j0; j < idx_j1; j++) {
				dist[idx_i][j] = Math.min(dist[idx_i][j], dist[idx_i][idx_k] + dist[idx_k][j]);
			}
		}
	}
	
	public float getDistance(Node node1, Node node2) {
		return (distances.get(node1)).get(node2);
	}
	
	private void setDistance(Node node1, Node node2, float distance) {
		(distances.get(node1)).put(node2, distance);
	}
	
	
	/**
	 * @param node1
	 * @param node2
	 * @return The shortest direct distance between node1 and node2, or Double.MAX_VALUE if no direct connection exists.
	 */
	private float getDirectDistance(Node node1, Node node2) {
		float distance = Float.MAX_VALUE;
		if(node1 == node2) {
			distance = 0.0f;
		}
		for(Object obj : Node.getEdgesBetween(node1, node2)) {	// this does the actual work!
			LineMergeEdge edge = (LineMergeEdge) obj;
			distance = Math.min(distance, (float)edge.getLine().getLength());
		}
		return distance;
	}
}
