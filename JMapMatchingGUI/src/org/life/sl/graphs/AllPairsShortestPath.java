package org.life.sl.graphs;

/*
JMapMatcher

Copyright (c) 2011 Bernhard Barkow, Hans Skov-Petersen, Bernhard Snizek and Contributors

mail: bikeability@life.ku.dk
web: http://www.bikeability.dk

This program is free software; you can redistribute it and/or modify it under 
the terms of the GNU General Public License as published by the Free Software 
Foundation; either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT 
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
this program; if not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

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
	private Node[] nodesA;
	long validRoutes;

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
		nodesA = new Node[nNodes];
		int i = 0;
		for(Node node1 : nodes) {
			nodesA[i++] = node1;
		}

		double t_tot = 0.f;	// timing variables
		timer.init();

		// initialize matrix: each element is assigned the direct distance
		if (bShowProgress) System.out.println("Initializing AllPairsShortestPath-Matrix...");
		i = 0;
		int j = 0;
		double ni = 0, nn2 = nNodes*nNodes;
		if (bUndirectedEdges) nn2 /= 2;
		Node node1;
		
		int kNumThreads = 4;
		int nIncr = (int)Math.round((double)nNodes / (double)kNumThreads + 1.);	// round up
		ArrayList<Callable<Integer>> runners1 = new ArrayList<Callable<Integer>>();//new DistInnerLoop[kNumThreads];
		for (j = 0; j < kNumThreads; j++) runners1.add(new DistInnerLoop());
		ExecutorService executor1 = Executors.newFixedThreadPool(kNumThreads); 

		for (i = 0; i < nNodes; i++) {
			node1 = nodesA[i];
			if (bUndirectedEdges) nIncr = (int)Math.round((double)(nNodes-i) / (double)kNumThreads + 1.);
			int nc = (bUndirectedEdges ? i: 0);
			for (j = 0; j < kNumThreads; j++) {
				DistInnerLoop dil = (DistInnerLoop)runners1.get(j);
				dil.init(i, nc, Math.min(nc+nIncr, nNodes), node1, nodesA);	// make sure the last thread does not exceed the range 
				nc += nIncr;
			}
			try {
				for (Future<Integer> f : executor1.invokeAll(runners1)) {
					ni += f.get();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (bShowProgress) timer.showProgress(ni/nn2);	// outer loop progress indicator
		}
//		executor1.shutdown();

//		int j0 = 0;
//		float d = 0;
//		Node node2;
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
//		float distance;
		timer.init(kShowProgressInterval1, 5.*kShowProgressInterval2);
		validRoutes = 0;
//		kNumThreads = 1;
//		nIncr = nNodes;
		nIncr = (int)Math.round((double)nNodes / (double)kNumThreads + 1.);	// round up

		ArrayList<Callable<Integer>> runners2 = new ArrayList<Callable<Integer>>();//new DistInnerLoop[kNumThreads];
		for (j = 0; j < kNumThreads; j++) runners2.add(new FloydWarshallInnerLoop());
//		ExecutorService executor2 = Executors.newFixedThreadPool(kNumThreads); 

		for(int k = 0; k < nNodes; k++) {
			for (i = 0; i < nNodes; i++) {
				int nc = 0;
				for (int t = 0; t < kNumThreads; t++) {
					FloydWarshallInnerLoop fwil = (FloydWarshallInnerLoop)runners2.get(t);
					fwil.init(k, i, nc, Math.min(nc+nIncr, nNodes));	// make sure the last thread does not exceed the range 
					nc += nIncr;
				}
				try {
					for (Future<Integer> f : executor1.invokeAll(runners2)) {
						ni += f.get();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//ni += nn;
//				for(j = 0; j < nNodes; j++) {
//					dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
//					ni++;
//				}
//			if (bShowProgress) timer.showProgress(0);	// inner loop progress indicator
			if (bShowProgress) timer.showProgress(ni/nnn);	// outer loop progress indicator
		}
		executor1.shutdown();
//		ni *= nNodes;

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
		if (bShowProgress) {
			t_tot = timer.getRunTime(true, "Floyd-Warshall finished");
			System.out.println("Floyd-Warshall finished - computed " + ni + " distances in " + t_tot + "s (" + ni/t_tot + "/s) - " + validRoutes);
		}
	}
	
	private class DistInnerLoop implements Callable<Integer> {
		private int idx_i, idx_j0, idx_j1;
		//private int nNodes;
		private Node node1 = null;
		private Node[] nodesA;
		
		public void init(int i, int j0, int j1, Node node10, Node[] nodesA0) {
			idx_i = i;
			idx_j0 = j0;
			idx_j1 = j1;
			node1 = node10;
			nodesA = nodesA0;
			//nNodes = nodesA.length;
		}
		public Integer call() {
			Integer n = 0;
			float d;
			Node node2;
//			int nIncr;
//			for (int i = idx_i0; i < idx_i1; i++) {
//				node1 = nodesA[i];
//				if (bUndirectedEdges) nIncr = (int)Math.round((double)(nNodes-i) / (double)kNumThreads + 1.);
//				int nc = (bUndirectedEdges ? i: 0);
			for (int j = idx_j0; j < idx_j1; j++) {
				node2 = nodesA[j];
				d = (float)getDirectDistance(node1, node2);
				dist[idx_i][j] = d;
				if (bUndirectedEdges && idx_i != j) dist[j][idx_i] = d;
				n++;
			}
			return n;
		}
	}
	
	final class FloydWarshallInnerLoop implements Callable<Integer> {
		private int idx_k, idx_i, idx_j0, idx_j1;
		
		public void init(int k, int i, int j0, int j1) {
			idx_k = k;
			idx_i = i;
			idx_j0 = j0;
			idx_j1 = j1;
		}
		/**
		 * @return the number of steps performed in the outer loop
		 */
		public Integer call() {
			float d;
			for (int j = idx_j0; j < idx_j1; j++) {
				d = Math.min(dist[idx_i][j], dist[idx_i][idx_k] + dist[idx_k][j]);
				dist[idx_i][j] = d;
				if (d < Float.MAX_VALUE) validRoutes++;
			}
			return idx_j1 - idx_j0 + 1;
		}
	}
	
	public float getDistance(Node node1, Node node2) {
		//return (distances.get(node1)).get(node2);
		int[] ni = getNodeIndex2(node1, node2);
		return dist[ni[0]][ni[1]];
	}
	
	/*private void setDistance(Node node1, Node node2, float distance) {
		//(distances.get(node1)).put(node2, distance);
		int[] ni = getNodeIndex2(node1, node2);
		dist[ni[0]][ni[1]] = distance;
	}
	
	private int getNodeIndex(Node node) {
		int idx = -1;
		for (int i = 0; i < nodesA.length; i++) {
			if (nodesA[i] == node) {
				idx = i;
				break;
			}
		}
		return idx;
	}*/
	private int[] getNodeIndex2(Node node1, Node node2) {
		int[] idx = { -1, -1 };
		for (int i = 0; (i < nodesA.length) && (idx[0] < 0 || idx[1] < 0); i++) {
			if (nodesA[i] == node1) idx[0] = i;
			if (nodesA[i] == node2) idx[1] = i;
		}
		return idx;
	}
	
	/**
	 * @param node1
	 * @param node2
	 * @return The shortest direct distance between node1 and node2, or Double.MAX_VALUE if no direct connection exists.
	 */
	private float getDirectDistance(Node node1, Node node2) {
		float d = Float.MAX_VALUE;		// default: "no connection"
		if(node1 == node2) d = 0.0f;	// "connection to self"
		for(Object obj : Node.getEdgesBetween(node1, node2)) {	// Node.getEdgesBetween() does the actual work here! (returns a list of connecting paths)
			LineMergeEdge edge = (LineMergeEdge) obj;
			d = Math.min(d, (float)edge.getLine().getLength());	// the shortest of all connecting paths
		}
		return d;
	}
}
