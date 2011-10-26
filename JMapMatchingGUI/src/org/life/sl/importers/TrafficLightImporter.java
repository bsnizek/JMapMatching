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

import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.Session;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.Trafficlight;
import org.life.sl.utils.CoordinateTransformer;
import org.life.sl.utils.ProjectionUtil;
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
import com.vividsolutions.jts.geom.Point;


public class TrafficLightImporter {

	public TrafficLightImporter() {

		//		http://download.bbbike.org/osm/bbbike/Copenhagen/
		// 		2011-Sep-29 14:13:52


	}

	public void read(String filename) throws IllegalDataException, IOException, FactoryException, ParserConfigurationException, SAXException, TransformException, XMLStreamException, FactoryConfigurationError {
		//readOSMFilefromStream(filename);
		readOSMFileWithStax(filename);

	}

	private void readOSMFileWithStax(String filename) throws XMLStreamException, FactoryConfigurationError, IOException, FactoryException, TransformException {


		com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();

		ProjectionUtil pu = new ProjectionUtil();

		CoordinateReferenceSystem crs_from = pu.getCRS("prj/osm.prj");
		CoordinateReferenceSystem crs_to = pu.getCRS("prj/gps.prj");

		CoordinateTransformer ct = new CoordinateTransformer(crs_from, crs_to);

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		FileInputStream fileInputStream = 
				new FileInputStream(filename);
		XMLStreamReader staxXmlReader =
				XMLInputFactory.newInstance().
				createXMLStreamReader(fileInputStream);

		String lat_string = null;
		String lon_string = null;
		String id_string = null;
		boolean hasSignal = false;

		int counter = 0;
		
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

					if (v.equals("traffic_signals")) {
						hasSignal = true;
					}
				}


				break;
			case XMLStreamConstants.END_ELEMENT:
				// System.out.println("End element " + staxXmlReader.getLocalName());
				if (staxXmlReader.getLocalName() == "node") {
					if (hasSignal) {
						System.out.println ("WRITING node " + id_string + " (" + lat_string + "/" + lon_string + ")");
						int id = Integer.valueOf(id_string);

						Float lat = Float.valueOf(lat_string);
						Float lon = Float.valueOf(lon_string);


						Coordinate c = new Coordinate(lon, lat); // z = 0, no elevation
						Coordinate coord_transformed = ct.transform(c);

						Point point = fact.createPoint(coord_transformed);

						Trafficlight l = new Trafficlight();

						l.setGeometry(point);
						l.setId(id);

						System.out.print(".");
						session.save(l);
						
						counter++;
						if (counter == 20) {
							session.getTransaction().commit();
							session = HibernateUtil.getSessionFactory().getCurrentSession();
							session.beginTransaction();
							counter = 0;
							System.out.println("flushed");
						}
						
					
					}
					hasSignal = false;
				}
				break;
			default:
				break;
			}
	
			
		}
		
		System.out.println("import finished");



	}

	public static void main(String[] args) throws IllegalDataException, IOException, FactoryException, ParserConfigurationException, SAXException, TransformException, XMLStreamException, FactoryConfigurationError {

		TrafficLightImporter tli = new TrafficLightImporter();
		tli.read("/Users/besn/Downloads/Copenhagen2.osm");
		// tli.read("/Users/besn/Downloads/trafficlights.osm");

	}

}
