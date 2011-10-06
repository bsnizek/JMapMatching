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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.life.sl.orm.Bicycletype;
import org.life.sl.orm.Cyclewaytype;
import org.life.sl.orm.Foottype;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.Highwaytype;
import org.life.sl.orm.OSMEdge;
import org.life.sl.orm.OSMNode;
import org.life.sl.orm.Segregatedtype;
import org.life.sl.utils.CoordinateTransformer;
import org.life.sl.utils.ProjectionUtil;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
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
import com.vividsolutions.jts.geom.Geometry;


//import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;

/**
 * @author Bernhard Snizek <besn@life.ku.dk>
 *
 */
public class OSMImporter {


	public OSMImporter() {
	}

	/**
	 * loads an OSM File and builds up the road Network (Path Segmented Graph)
	 * 
	 * @param osmFileName : OSM File Name as a String
	 * @throws IllegalDataException
	 * @throws TransformException 
	 * @throws FactoryException 
	 * @throws IOException 
	 * 
	 */
	public void readOSMFile(String osmFileName) throws IllegalDataException, IOException, FactoryException, TransformException {

		InputStream fos = new FileInputStream(osmFileName);
		readOSMFilefromStream(fos);
	}




	public void readOSMFilefromStream(InputStream fos) throws IllegalDataException, IOException, FactoryException, TransformException {

		Main.pref = new Preferences();

		Main.proj = new Epsg4326();

		Main.pref.put("tags.direction", false);

		DataSet dsRestriction = OsmReader.parseDataSet(fos, null);

		Collection<Way> ways = dsRestriction.getWays();
		Collection<Node> all_nodes = dsRestriction.getNodes();

		com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
		
		ProjectionUtil pu = new ProjectionUtil();
		
		CoordinateReferenceSystem crs_from = pu.getCRS("prj/osm.prj");
		CoordinateReferenceSystem crs_to = pu.getCRS("prj/gps.prj");
		
		CoordinateTransformer ct = new CoordinateTransformer(crs_from, crs_to);

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		// the bicycletype lookup table
		
		HashMap<String, Short> bicycletype = new HashMap<String, Short>();	
		Query q1 = session.createQuery("from Bicycletype");
		Iterator<Bicycletype> i1 = q1.iterate();
		while (i1.hasNext()) {
			Bicycletype bt = (Bicycletype) i1.next();
			bicycletype.put(bt.getDescr(), bt.getId());
		}
		
		// the cyclewaytype lookup table
		
		HashMap<String, Short> cyclewaytype = new HashMap<String, Short>();
		Query q2 = session.createQuery("from Cyclewaytype");
		Iterator<Cyclewaytype> i2= q2.iterate();
		while (i2.hasNext()) {
			Cyclewaytype cwt = (Cyclewaytype) i2.next();
			cyclewaytype.put(cwt.getDescr(), cwt.getId());
		}
		
		// the foottype lookup table
		
		HashMap<String, Short> foottype = new HashMap<String, Short>();
		Query q3 = session.createQuery("from Foottype");
		Iterator<Foottype> i3 = q3.iterate();
		while (i3.hasNext()) {
			Foottype ftt = (Foottype) i3.next();
			foottype.put(ftt.getDescr(), ftt.getId());
		}
		
		// Segregatedtype lookup
		
		HashMap<String, Short> segregatedtype = new HashMap<String, Short>();
		Query q4 = session.createQuery("from Segregatedtype");
		Iterator<Segregatedtype> i4 = q4.iterate();
		while (i4.hasNext()) {
			Segregatedtype stt = (Segregatedtype) i4.next();
			segregatedtype.put(stt.getDescr(), stt.getId());
		}		
		
		// Highwaytype
		
		HashMap<String, Short> highwaytype = new HashMap<String, Short>();
		Query q5 = session.createQuery("from Highwaytype");
		Iterator<Highwaytype> i5 = q5.iterate();
		while (i5.hasNext()) {
			Highwaytype ht = (Highwaytype) i5.next();
			highwaytype.put(ht.getDescr(), ht.getId());
		}

		for (Node node : all_nodes) {

			Query result = session.createQuery("from OSMNode WHERE id=" + node.getId());
			if (result.list().size() == 0) {

				OSMNode onode = new OSMNode();
				onode.setId((int) node.getId());
				LatLon coor = node.getCoor();
				if (coor != null) {
					Coordinate c = new Coordinate(node.getCoor().lon(), node.getCoor().lat());	
					com.vividsolutions.jts.geom.Point point = fact.createPoint(c);
					Geometry point_transformed = ct.transform(point);
					onode.setGeometry(point_transformed.getCentroid());
					session.save(onode);
				}
			}
		}


		for (Way way : ways) {
			if (way.get("highway") != null) {
				if (
					way.get("highway").equals("primary") || 
					way.get("highway").equals("tertiary") || 
					way.get("highway").equals("secondary") ||
					way.get("highway").equals("residential") || 
					way.get("highway").equals("cycleway") || 
					way.get("highway").equals("footway") ||
					way.get("highway").equals("path") ||
					way.get("highway").equals("service") ||
					way.get("highway").equals("track") ||
					way.get("highway").equals("pedestrian") ||
					way.get("cycleway") != null
					
					) {

					String roadName = way.getName();
					System.out.println(roadName);

					Query result = session.createQuery("from OSMEdge WHERE id=" + way.getId());
					if (result.list().size() == 0) {

						List<Node> nodes = way.getNodes();

						Coordinate[] array1 = new Coordinate[nodes.size()];

						int counter = 0;

						for (Node node : nodes) {
							LatLon ll = node.getCoor();
							Coordinate c = new Coordinate(ll.lon(), ll.lat()); // z = 0, no elevation
							Coordinate coord_transformed = ct.transform(c);
							array1[counter] = coord_transformed;
							counter = counter +1;

						}

						com.vividsolutions.jts.geom.LineString lineString = fact.createLineString(array1);

						OSMEdge oe = new OSMEdge();
						oe.setGeometry(lineString);
						oe.setId((int) way.getId());
						oe.setFromnode((int) way.getNode(0).getId());
						oe.setTonode((int) way.getNode(way.getNodesCount()-1).getId());
						oe.setLength(lineString.getLength());
						oe.setRoadname(way.getName());
						
						oe.setHighwaytype(highwaytype.get(way.get("highway")));
						
						oe.setSegregatedtype(segregatedtype.get(way.get("segregated")));
						
						oe.setBicycletype(bicycletype.get(way.get("bicycle")));
						
						oe.setFoottype(foottype.get(way.get("foot")));
						
						oe.setCyclewaytype(cyclewaytype.get(way.get("cycleway")));
						
						System.out.print(".");
						session.save(oe);
					}
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


	public static void main(String[] args) throws IllegalDataException, IOException, FactoryException, TransformException {
		String filename = "testdata/testnet.osm";
		OSMImporter osm_reader = new OSMImporter();
		osm_reader.readOSMFile(filename);
	}

}