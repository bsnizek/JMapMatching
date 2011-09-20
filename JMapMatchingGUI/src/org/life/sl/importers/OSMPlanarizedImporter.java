package org.life.sl.importers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.hibernate.Session;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.OSMEdge;
import org.life.sl.orm.OSMNode;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.Node;

// Imports any graph into the postgresql, creating new ids for nodes and edges


public class OSMPlanarizedImporter {

	private PathSegmentGraph pSg;
	private Session session;

	private com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();

	public void dumpToPostgresql() {
		ArrayList<Node> nodes = pSg.getNodes();
		Iterator<Node> iter = nodes.iterator();
		
		System.out.println("Writing nodes...");
		int i = 0;
		HashMap<Node, Integer> node__nodeId = new HashMap<Node, Integer>();
		while (iter.hasNext()) {
			Node n = iter.next();
			
			OSMNode osmNode = new OSMNode();
			osmNode.setGeometry(fact.createPoint(n.getCoordinate()));
			// @SuppressWarnings("unchecked")
			// HashMap<String, Integer> data = (HashMap<String, Integer>) n.getData();
			// int id = data.get("id");
			osmNode.setId(i); //  todo set this properly
			
			node__nodeId.put(n, i);
			session.save(osmNode);
			i++;
		}
		
		Collection<Edge> edges = pSg.getEdges();
		Iterator<Edge> iter2 = edges.iterator();
		
		System.out.println("Writing edges...");
		int j=0;
		
		while (iter2.hasNext()){
			Edge e = iter2.next();
			
			OSMEdge osmEdge = new OSMEdge();
			@SuppressWarnings("unchecked")
			HashMap<String, Object> data2 = (HashMap<String, Object>) e.getData();
			Object geom = data2.get("geom");
			LineString ls = (LineString) geom;
			osmEdge.setGeometry(ls);
			osmEdge.setId(j);
			
			Node from_node = e.getDirEdge(0).getFromNode();
			Node to_node = e.getDirEdge(0).getToNode();
			
			Integer from_node_id = node__nodeId.get(from_node);
			Integer to_node_id = node__nodeId.get(to_node);
			
			osmEdge.setFromnode(from_node_id);
			osmEdge.setTonode(to_node_id);
			osmEdge.setLength(ls.getLength());
			
			session.save(osmEdge);
			j++;
		}
		
		session.getTransaction().commit();
		
	}
	
	public OSMPlanarizedImporter(String shapefile) throws IOException {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		pSg = new PathSegmentGraph(shapefile);
	}
	
	public static void main(String[] args) throws IOException {
		OSMPlanarizedImporter oSMPI = new OSMPlanarizedImporter("testdata/osmdata/osmplanarized.shp");
		oSMPI.dumpToPostgresql();
		System.out.println("Finished");
	}
	
}
