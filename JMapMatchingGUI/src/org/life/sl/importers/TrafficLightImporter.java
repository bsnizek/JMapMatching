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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.Trafficlight;
import org.life.sl.utils.CoordinateTransformer;
import org.life.sl.utils.ProjectionUtil;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import org.openstreetmap.josm.io.IllegalDataException;



import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class TrafficLightImporter {

	private static final double kTlDist = 20.0;	// radius around an OSMNode inside which a trafficlight must be in order to be associated with the node
	private static final int batchSize = 50;
	private static Logger logger = Logger.getLogger("TrafficLightImporter");

	private Session session;
	private int nTot;	// counter

	public TrafficLightImporter() {
		//		http://download.bbbike.org/osm/bbbike/Copenhagen/
		// 		2011-Sep-29 14:13:52
	}

	private void clearTrafficlights() {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		int nDel = session.createQuery("delete Trafficlight").executeUpdate();
		session.flush();
		session.getTransaction().commit();
		System.out.println("Deleted " + nDel + " records from OSMNode");
	}

	public void read(String filename) throws IllegalDataException, IOException, FactoryException, ParserConfigurationException, SAXException, TransformException, XMLStreamException, FactoryConfigurationError {
		//readOSMFilefromStream(filename);
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		logger.info("Database batch size: " + batchSize);

		File tlFile = new File(filename);
		if (filename.toLowerCase().endsWith(".osm")) readOSMFileWithStax(tlFile);
		if (filename.toLowerCase().endsWith(".shp")) readShapeFile(tlFile);
		// commit the transaction!
		if (session.getTransaction().isActive()) session.getTransaction().commit();
		System.out.println();
		logger.info("Imported " + nTot+ " traffic lights");		
		logger.info("Import finished");
	}
	
	/**
	 * read trafficlight data from a shape file
	 * @param inputFile the File containing the data (.shp)
	 * @throws IOException
	 */
	private void readShapeFile(File inputFile) throws IOException {
		logger.info("Reading data from Shape file " + inputFile.getName());
		Map<String,Serializable> connectParameters = new HashMap<String,Serializable>();
		connectParameters.put("url", inputFile.toURI().toURL());
		// connectParameters.put("create spatial index", true );
		DataStore dataStore = DataStoreFinder.getDataStore(connectParameters);

		String[] typeNames = dataStore.getTypeNames();
		String typeName = typeNames[0];

		logger.info("Reading content " + typeName);

		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
		FeatureIterator<SimpleFeature> iterator;

		featureSource = dataStore.getFeatureSource(typeName);
		collection = featureSource.getFeatures();
		iterator = collection.features();

		// let us get the schema and pop it into a simple list of strings
		SimpleFeatureType schema = featureSource.getSchema();
		List<AttributeDescriptor> ads = schema.getAttributeDescriptors();
		ArrayList<String> fieldnames = new ArrayList<String>();

		for (AttributeDescriptor ad : ads) {
			String ln = ad.getLocalName();
			if (ln != "geom") fieldnames.add(ln);
		}

		nTot = 0;
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				Geometry geometry = (Geometry) feature.getDefaultGeometry();
				if(geometry.getClass() != Point.class && geometry.getClass() != Point.class) {
					//    				ErrorHandler.getInstance().error("Error, shapefile must contain lines", 2);
					logger.error("ERROR: Shapefile must contain points, but does not.");
					return;
				}
				
				HashMap<String, Object> attributes = new HashMap<String, Object>();
				for (String fn : fieldnames) {
					attributes.put(fn, feature.getAttribute(fn));
				}
				int id = ((Double) attributes.get("id")).intValue();
				
				// store attribute values in new SourcePoint:
				saveNewTrafficLight(id, (Point)(geometry.reverse()));
			}
		}
		finally {
			if( iterator != null ){
				if (nTot % batchSize != 0) {
					session.flush();
					session.clear();
				}
				// YOU MUST CLOSE THE ITERATOR!
				iterator.close();
			}
		}
	}

	/**
 	 * read trafficlight data from an OSM file
	 * @param inputFile the File containing the data (.osm)
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws FactoryException
	 * @throws TransformException
	 */
	private void readOSMFileWithStax(File inputFile) throws XMLStreamException, FactoryConfigurationError, IOException, FactoryException, TransformException {
		logger.info("Reading data from OSM file " + inputFile.getName());

		com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();

		ProjectionUtil pu = new ProjectionUtil();

		CoordinateReferenceSystem crs_from = pu.getCRS("prj/osm.prj");
		CoordinateReferenceSystem crs_to = pu.getCRS("prj/gps.prj");
		CoordinateTransformer ct = new CoordinateTransformer(crs_from, crs_to);

		FileInputStream fileInputStream = 
				new FileInputStream(inputFile);
		XMLStreamReader staxXmlReader =
				XMLInputFactory.newInstance().
				createXMLStreamReader(fileInputStream);

		String lat_string = null;
		String lon_string = null;
		String id_string = null;
		boolean hasSignal = false;

		nTot = 0;
		
		for (int event = staxXmlReader.next(); event != XMLStreamConstants.END_DOCUMENT; event = staxXmlReader.next()) {
			switch (event) {
			case XMLStreamConstants.START_DOCUMENT:
				// System.out.println("Start document " + staxXmlReader.getLocalName());
				break;
			case XMLStreamConstants.START_ELEMENT:
				// System.out.println("Start element " + staxXmlReader.getLocalName());
				if (staxXmlReader.getLocalName() == "node") {
					lat_string = staxXmlReader.getAttributeValue("", "lat");
					lon_string = staxXmlReader.getAttributeValue("", "lon");
					id_string = staxXmlReader.getAttributeValue("", "id");
				}

				if (staxXmlReader.getLocalName() == "tag") { 
					String v = staxXmlReader.getAttributeValue("", "v");
					if (v.equals("traffic_signals")) hasSignal = true;
				}

				break;
			case XMLStreamConstants.END_ELEMENT:
				// System.out.println("End element " + staxXmlReader.getLocalName());
				if (staxXmlReader.getLocalName() == "node") {
					if (hasSignal) {
						logger.info("WRITING node " + id_string + " (" + lat_string + "/" + lon_string + ")");
						int id = Integer.valueOf(id_string);

						Float lat = Float.valueOf(lat_string);
						Float lon = Float.valueOf(lon_string);

						Coordinate c = new Coordinate(lon, lat); // z = 0, no elevation
						Coordinate coord_transformed = ct.transform(c);

						Point point = fact.createPoint(coord_transformed);
						
						saveNewTrafficLight(id, point);

					}
					hasSignal = false;
				}
				break;
			default:
				break;
			}
		}
	}
	
	private void saveNewTrafficLight(int id, Point point) {
		Trafficlight l = new Trafficlight();
		l.setGeometry(point);
		l.setId(id);

		System.out.print(".");
		session.save(l);
		nTot++;
		if (nTot % batchSize == 0) {
			session.flush();
			session.clear();
			System.out.println();
		}
	}
	
	/**
	 * get the number of trafficlights in the database that are not associated to OSMNodes
	 * @param session the current database session (must be opened before)
	 * @return number of trafficlights without OSMNodes
	 */
	private Long getLightsWithoutNodes(Session session) {
		String s = "select count(*) from Trafficlight where nodeid=0 or nodeid is null";
		Query n1 = session.createQuery(s);
		return (Long)n1.uniqueResult();
	}

	/**
	 * assign OSMNode IDs to the trafficlights
	 */
	private void assignOSMNodes(double tlDist) {
		// get the IDs of the OSMNodes at the trafficlight coordinates:
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// check the number of empty nodes:
		logger.info(getLightsWithoutNodes(session) + " unassigned nodes");
		// update them:
		String sUpd = "update Trafficlight set nodeid=(select id from OSMNode where (ST_DWithin(OSMNode.geom, Trafficlight.geom, "+tlDist+")) limit 1) where (nodeid=0) or (nodeid is null)";
		System.out.println(sUpd);
		long nUpd = session.createSQLQuery(sUpd).executeUpdate();
		logger.info("assigned " + nUpd + " node IDs");
		// check again:
		logger.info(getLightsWithoutNodes(session) + " unassigned nodes in table");
		session.flush();
		session.getTransaction().commit();
	}
	
	public static void main(String[] args) throws IllegalDataException, IOException, FactoryException, ParserConfigurationException, SAXException, TransformException, XMLStreamException, FactoryConfigurationError {
		TrafficLightImporter tli = new TrafficLightImporter();

		// first, empty the trafficlight table:
		tli.clearTrafficlights();
		// then, read the new data:
//		tli.read("geodata/CopenhagenTrafficlights/Copenhagen.osm");
		tli.read("geodata/CopenhagenTrafficlights/SnappedGeneralisedTrafficLights.shp");
		// finally, assign node IDs:
		if (tli.nTot > 0) tli.assignOSMNodes(kTlDist);
	}
}
