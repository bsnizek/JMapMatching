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


public class ShapeFileImporter {

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
			Object geom = data2.get("geometry");

			Object bctyp_s = data2.get("BICYCLETYP");
			if (bctyp_s != null) {
				Integer bint = new Integer((Integer) bctyp_s);
				Short bicycle = bint.shortValue();
				osmEdge.setBicycletype(bicycle);
			}


			Object cycleway_s = data2.get("CYCLEWAYTY");
			if (cycleway_s != null) {
				Integer cwint = new Integer((Integer) cycleway_s);
				Short cycleway = cwint.shortValue();
				osmEdge.setCyclewaytype(cycleway);
			}

			Object foottype_s = data2.get("FOOTTYPE");
			if (foottype_s != null) {
				Integer fint = new Integer((Integer) foottype_s);
				Short foot = fint.shortValue();
				osmEdge.setFoottype(foot);
			}


			Object hwt_s = data2.get("HIGHWAYTYP");
			if (hwt_s != null) {
				Integer hwt = new Integer((Integer) hwt_s);
				Short highway = hwt.shortValue();
				osmEdge.setHighwaytype(highway);

			}

			Object sgrd_s = data2.get("SEGREGATED");
			if (sgrd_s != null) {
				Integer sgrd = new Integer((Integer) sgrd_s);
				Short segregated = sgrd.shortValue();
				osmEdge.setSegregatedtype(segregated);
			}
			
			Object roadname_o = data2.get("ROADNAME");
			if (roadname_o != null) {
				String roadname = (String) roadname_o;
				osmEdge.setRoadname(roadname);
			}

			// The HSP-specific stuff

			Integer envtp = new Integer((Integer) data2.get("EnvType"));
			Short envType = envtp.shortValue();

			Integer cyktp = new Integer((Integer) data2.get("CykType"));
			Short cykType = cyktp.shortValue();

			Double groenPct = new Double((Double) data2.get("GroenPct"));
			Double groenM = new Double((Double) data2.get("GroenM"));


			LineString ls = (LineString) geom;

			osmEdge.setGeometry(ls);
			osmEdge.setId(j);
			osmEdge.setEnvtype(envType);
			osmEdge.setCyktype(cykType);
			osmEdge.setGroenpct(groenPct);
			osmEdge.setGroenm(groenM);

			Node from_node = e.getDirEdge(0).getFromNode();
			Node to_node = e.getDirEdge(0).getToNode();

			Integer from_node_id = node__nodeId.get(from_node);
			Integer to_node_id = node__nodeId.get(to_node);

			osmEdge.setFromnode(from_node_id);
			osmEdge.setTonode(to_node_id);
			osmEdge.setLength(ls.getLength());

			session.save(osmEdge);
			j++;
			System.out.print(".");
		}
		session.getTransaction().commit();
		System.out.println("Import finished");


	}

	public ShapeFileImporter(String shapefile) throws IOException {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		pSg = new PathSegmentGraph(shapefile);
	}

	public static void main(String[] args) throws IOException {
		ShapeFileImporter oSMPI = new ShapeFileImporter("testdata/CPH_OSM/CPH_OSM_Bikeability.shp");
		oSMPI.dumpToPostgresql();
		System.out.println("Finished");
	}

}
