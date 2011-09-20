package org.life.sl.importers;

import java.util.HashMap;
import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.Session;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.OSMEdge;
import org.life.sl.orm.OSMNode;
import org.life.sl.orm.ShortestPathLength;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.Node;

public class CalculateODMatrix {

	PathSegmentGraph psg = null;

	// a hashmap mapping OSM ids to OSM Edges
	HashMap<Integer, OSMEdge> ids_edges = this.loadEdgesFromOSM();
	HashMap<Integer, OSMNode> ids_nodes = this.loadNodesFromOSM();

	public CalculateODMatrix() {

		psg = new PathSegmentGraph(1);	// 1 = read from database...
		psg.calculateDistances();

		HashMap<Node, HashMap<Node, Double>> distances = psg.getAPSDistances();
		//double dist[][] = psg.getAPSDistancesArr();//new double[nodes.size()][nodes.size()];

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		int osmNodeID1 = 0, osmNodeID2 = 0;
		Double length = 0.;
		HashMap<Node, Double> hm1;
		
		System.out.println("Starting database import...");
		long t_start = System.nanoTime();
		for (Node n1 : distances.keySet()) {	// outer loop over all nodes
			osmNodeID1 = getOSMNodeIDForNode(n1);
			if (osmNodeID1 != 0) {				// check if node exists in OSM network at all...
				hm1 = distances.get(n1);		// a HashMap<Node, Double>
				for (Node n2 : hm1.keySet()) {	// inner loop over all nodes
					if (n2 != n1) {				// no connection to self!
						length = hm1.get(n2);	// the path length
						if (length > 1.e-8 && length < 1.e100) {	// ignore 0 (= self) and infinity (= no connection)
							osmNodeID2 = getOSMNodeIDForNode(n2);

							// store length(n1, n2)
							if (osmNodeID2 != 0) {
								ShortestPathLength sPl1 = new ShortestPathLength(osmNodeID1, osmNodeID2, length);
								session.save(sPl1);
								
								// the same path in reverse direction: not necessary
								/*ShortestPathLength sPl2 = new ShortestPathLength(osmNodeID2, osmNodeID1, length);
								session.save(sPl2);*/
							}
						}
					}
				}
			}
		}
		session.getTransaction().commit();
		System.out.println("... finished: t = " + (double)(System.nanoTime() - t_start)*1.e-9 + "s");
		System.out.println("YEAH !");

	}

	private int getOSMNodeIDForNode(Node n1) {
		int osmID = 0;

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
	}

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
		System.out.println("Edges read from database: " + id_edge.size());
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
		System.out.println("Nodes read from database: " + id_node.size());
		return id_node;
	}


	public static void main(String[] args) {	
		CalculateODMatrix codm = new CalculateODMatrix();
	}

}
