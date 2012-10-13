package org.life.sl.importers;

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

import java.util.HashMap;
import java.util.Iterator;

import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.OSMEdge;
import org.life.sl.orm.OSMNode;
import org.life.sl.orm.ShortestPathLength;
import org.life.sl.utils.Timer;

//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.planargraph.Edge;
//import com.vividsolutions.jts.planargraph.Node;

public class CalculateODMatrix {

	private PathSegmentGraph psg = null;
	
	// a hashmap mapping OSM ids to OSM Edges
	HashMap<Integer, OSMEdge> ids_edges = null;
	HashMap<Integer, OSMNode> ids_nodes = null;
	
	public CalculateODMatrix() {
		Timer timer = new Timer();
		timer.init();
		ids_edges = this.loadEdgesFromOSM();
		timer.getRunTime(true, "Edges read from database: " + ids_edges.size());
		ids_nodes = this.loadNodesFromOSM();
		timer.getRunTime(true, "Nodes read from database: " + ids_nodes.size());

		psg = new PathSegmentGraph(1);	// 1 = read from database...
		timer.getRunTime(true, "PathSegmentGraph initialized");
		psg.calculateDistances();

		//HashMap<Node, HashMap<Node, Double>> distances = psg.getAPSDistances();
		float dist[][] = psg.getAPSDistancesArr();//new double[nodes.size()][nodes.size()];

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		// first, empty the database table:
		session.beginTransaction();
		session.setCacheMode(CacheMode.IGNORE);
		int nDel = session.createQuery("delete ShortestPathLength").executeUpdate();
		session.flush();
		//session.getTransaction().commit();
		System.out.println("Deleted " + nDel + " records from shortestpathlength");

		//session.beginTransaction();

		int osmNodeID1 = 0, osmNodeID2 = 0;
		float length = 0.f;
		//HashMap<Node, Double> hm1;
		
		System.out.println("Starting database export...");
		Integer batchSize = Integer.getInteger(new Configuration().getProperty("hibernate.jdbc.batch_size"), 30);
		System.out.println("Database batch size: " + batchSize);

		timer.init(2.5, 50.);
		double nn = dist.length*dist.length/2;	// approximate number of steps
		long n = 0, nc = 0;
		for (int i = 0; i < dist.length - 1; i++) {	// outer loop over all nodes
			osmNodeID1 = i;//getOSMNodeIDForNode(n1);
			if (osmNodeID1 >= 0) {				// check if node exists in OSM network at all...
				for (int j = i+1; j < dist.length; j++) {	// inner loop over all nodes
					if (i != j) {				// no connection to self!
						length = dist[i][j];	// the path length
						if (length > 1.e-8 && length < .5f*Float.MAX_VALUE) {	// ignore 0 (= self) and infinity (= no connection)
							osmNodeID2 = j;//getOSMNodeIDForNode(n2);

							// store length(n1, n2)
							if (osmNodeID2 >= 0) {
								// TODO: can this be optimized by reusing sPl1 instead of creating it (new)?
								ShortestPathLength sPl1 = new ShortestPathLength(osmNodeID1, osmNodeID2, length);
								session.save(sPl1);
								if (++n % batchSize == 0) {
									session.flush();
									session.clear();
								}
								
								// the same path in reverse direction: not necessary
								/*ShortestPathLength sPl2 = new ShortestPathLength(osmNodeID2, osmNodeID1, length);
								session.save(sPl2);*/
							}
						}
						nc++;
					}
				}
			}
			timer.showProgress(nc/nn);
		}
//		for (Node n1 : distances.keySet()) {	// outer loop over all nodes
//			osmNodeID1 = getOSMNodeIDForNode(n1);
//			if (osmNodeID1 != 0) {				// check if node exists in OSM network at all...
//				hm1 = distances.get(n1);		// a HashMap<Node, Double>
//				for (Node n2 : hm1.keySet()) {	// inner loop over all nodes
//					if (n2 != n1) {				// no connection to self!
//						length = hm1.get(n2);	// the path length
//						if (length > 1.e-8 && length < 1.e100) {	// ignore 0 (= self) and infinity (= no connection)
//							osmNodeID2 = getOSMNodeIDForNode(n2);
//
//							// store length(n1, n2)
//							if (osmNodeID2 != 0) {
//								ShortestPathLength sPl1 = new ShortestPathLength(osmNodeID1, osmNodeID2, length);
//								session.save(sPl1);
//								
//								// the same path in reverse direction: not necessary
//								/*ShortestPathLength sPl2 = new ShortestPathLength(osmNodeID2, osmNodeID1, length);
//								session.save(sPl2);*/
//							}
//						}
//					}
//				}
//			}
//		}
		session.getTransaction().commit();	// TODO: complete the transaction in the outer loop above, to prevent it from getting too big?
		timer.getRunTime(true, "... finished");
		System.out.println("YEAH !");

	}

	/*private int getOSMNodeIDForNode(Node n1) {
		int osmID = -1;

		Edge e = psg.getSingleEdgeAtNode(n1);	// get one edge connected to Node n2
		if (e != null) {
			// get Edge ID (OSM ID):
			Object o = e.getData();

			@SuppressWarnings("unchecked")
			HashMap<String, Integer> hm = (HashMap<String, Integer>) o;
			int id = (Integer) hm.get("id");

			// get OSM node IDs from Edge:
			OSMEdge osmEdge = ids_edges.get(id);
			int fromNode = osmEdge.getFromnode();
			int toNode = osmEdge.getTonode();

			OSMNode on1 = ids_nodes.get(fromNode);
			OSMNode on2 = ids_nodes.get(toNode);

			// check which OSMNode corresponds to Node n2:
			// (there should be a more elegant way to accomplish this...)
			Coordinate on1coord = on1.getGeometry().getCoordinate();
			Coordinate on2coord = on2.getGeometry().getCoordinate();
			double x = n1.getCoordinate().x;
			double y = n1.getCoordinate().y;
			if (on1coord.x == y && on1coord.y == x) osmID = fromNode;
			if (on2coord.x == y && on2coord.y == x) osmID = toNode;	
			//System.out.printf("%f\t%f\t%f\t%d\n", on1coord.x, on2coord.x, y, osmID);
		}
		return osmID;
	}*/

	public HashMap<Integer, OSMEdge> loadEdgesFromOSM() {

		HashMap<Integer, OSMEdge> id_edge = new HashMap<Integer, OSMEdge>();

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query result = session.createQuery("from OSMEdge");
		@SuppressWarnings("unchecked")
		Iterator<OSMEdge> iter = result.iterate();
		while (iter.hasNext() ) {
			OSMEdge e = iter.next();
			id_edge.put(e.getId(), e);
		}
		session.disconnect();
		return id_edge;
	}

	public HashMap<Integer, OSMNode> loadNodesFromOSM() {

		HashMap<Integer, OSMNode> id_node = new HashMap<Integer, OSMNode>();

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query result = session.createQuery("from OSMNode");
		@SuppressWarnings("unchecked")
		Iterator<OSMNode> iter = result.iterate();
		while (iter.hasNext() ) {
			OSMNode e = iter.next();
			id_node.put(e.getId(), e);
		}
		session.disconnect();
		return id_node;
	}


	public static void main(String[] args) {	
		//CalculateODMatrix codm = 
				new CalculateODMatrix();
	}

}
