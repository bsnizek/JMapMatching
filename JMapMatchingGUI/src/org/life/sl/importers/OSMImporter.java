package org.life.sl.importers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.Collection;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.OSMEdge;
import org.life.sl.orm.OSMNode;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.Epsg4326;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;


import com.vividsolutions.jts.geom.Coordinate;

import com.vividsolutions.jts.geom.GeometryFactory;


//import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;

/**
 * @author Bernhard Snizek <besn@life.ku.dk>
 *
 */
public class OSMImporter {

	private static PathSegmentGraph psg;
	private static GeometryFactory gf;

	public OSMImporter() {
		// initialize the geometry factory
		gf = new GeometryFactory();
		psg = new PathSegmentGraph();
	}

	/**
	 * loads an OSM File and builds up the road Network (Path Segmented Graph)
	 * 
	 * @param osmFileName : OSM File Name as a String
	 * @throws FileNotFoundException
	 * @throws IllegalDataException
	 * 
	 */
	public void readOSMFile(String osmFileName)  throws FileNotFoundException, IllegalDataException {

		Main.pref = new Preferences();

		Main.proj = new Epsg4326();

		Main.pref.put("tags.direction", false);

		DataSet dsRestriction = OsmReader.parseDataSet(new FileInputStream(osmFileName), null);

		Collection<Way> ways = dsRestriction.getWays();
		Collection<Node> all_nodes = dsRestriction.getNodes();

		com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		for (Node node : all_nodes) {
			OSMNode onode = new OSMNode();
			onode.setId((int) node.getId());
			LatLon coor = node.getCoor();
			if (coor != null) {
				Coordinate c = new Coordinate(node.getCoor().lat(), node.getCoor().lon());	
				com.vividsolutions.jts.geom.Point point = fact.createPoint(c);
				onode.setGeometry(point);
				session.save(onode);
			}
		}
		
		
		for (Way way : ways) {
			if (way.get("highway") != null) {
				if (way.get("highway").equals("residential") || way.get("highway").equals("tertiary") || way.get("highway").equals("cycleway") || way.get("highway").equals("footway")) {

					String roadName = way.getName();
					System.out.println(roadName);

					List<Node> nodes = way.getNodes();

					Coordinate[] array1 = new Coordinate[nodes.size()];

					int counter = 0;

					for (Node node : nodes) {
						LatLon ll = node.getCoor();
						Coordinate c = new Coordinate(ll.lon(), ll.lat()); // z = 0, no elevation	
						array1[counter] = c;
						counter = counter +1;

					}
					
					com.vividsolutions.jts.geom.LineString lineString = fact.createLineString(array1);

					OSMEdge oe = new OSMEdge();
					oe.setGeometry(lineString);
					oe.setId((int) way.getId());
					oe.setFromnode((int) way.getNode(0).getId());
					oe.setTonode((int) way.getNode(way.getNodesCount()-1).getId());
					oe.setLength(lineString.getLength());
					
					
					session.save(oe);
					
					//HashMap<String, Object> hm = new HashMap<String, Object>();

					//hm.put("roadname", roadName);
					//hm.put("geometry", lineString);
					//lineString.setUserData(hm);
				}
			}
		}
		session.getTransaction().commit();
		
		Session session3 = HibernateUtil.getSessionFactory().getCurrentSession();
		session3.beginTransaction();
		
		// let us bulk-delete the nodes that have nothing to do with our edges:
		String delete_query = "DELETE FROM osmnode AS osmn WHERE NOT EXISTS (SELECT * FROM osmedge AS osme WHERE (osme.fromNode = osmn.id OR osme.toNode = osmn.id) )";
		SQLQuery xx = session3.createSQLQuery(delete_query); //.iterate().next();
		// System.out.println(number);
		xx.executeUpdate();
		System.out.println("Unnecessary Nodes flushed");
		session3.getTransaction().commit();
		
	}
	

	public static void main(String[] args) throws FileNotFoundException, IllegalDataException {
		String filename = "testdata/testnet.osm";
		OSMImporter osm_reader = new OSMImporter();
		osm_reader.readOSMFile(filename);
	}

}