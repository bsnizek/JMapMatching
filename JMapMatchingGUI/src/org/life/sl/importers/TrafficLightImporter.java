package org.life.sl.importers;


import java.io.IOException;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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


import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;


public class TrafficLightImporter {

	/**
	 * @param args
	 */
	
	public TrafficLightImporter() {
		
//		http://download.bbbike.org/osm/bbbike/Copenhagen/
// 		2011-Sep-29 14:13:52
		
		
	}
	
	public void read(String filename) throws IllegalDataException, IOException, FactoryException, ParserConfigurationException, SAXException, TransformException {
		readOSMFilefromStream(filename);
		
		
	}
	
	private void readOSMFilefromStream(String filename) throws ParserConfigurationException, IOException, FactoryException, SAXException, TransformException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = dbf.newDocumentBuilder();

		com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
		
		ProjectionUtil pu = new ProjectionUtil();
		
		Document dom = db.parse(filename);
		
		CoordinateReferenceSystem crs_from = pu.getCRS("prj/osm.prj");
		CoordinateReferenceSystem crs_to = pu.getCRS("prj/gps.prj");
		
		CoordinateTransformer ct = new CoordinateTransformer(crs_from, crs_to);

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		org.w3c.dom.Element docEle = dom.getDocumentElement();
		NodeList nl = docEle.getElementsByTagName("node");
		System.out.println(nl.getLength());
		
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
		
				Element el = (Element) nl.item(i);
				NodeList tags = el.getElementsByTagName("tag");
				
				boolean hasTrafficLight = false;
				
				if(tags != null && tags.getLength() > 0) {
					
					for(int j = 0 ; j < nl.getLength();j++) {
						
						Element tag_el = (Element) tags.item(j);
						if (tag_el != null) {
						String v = tag_el.getAttribute("v");
							hasTrafficLight = true;
						}					
					}
				}
				
				if (hasTrafficLight) {
					String lat_s = el.getAttribute("lat");
					String lon_s = el.getAttribute("lon");
					String id_s = el.getAttribute("id");
					
					int id = Integer.valueOf(id_s);
					
					Float lat = Float.valueOf(lat_s);
					Float lon = Float.valueOf(lon_s);
					
					
					Coordinate c = new Coordinate(lon, lat); // z = 0, no elevation
					Coordinate coord_transformed = ct.transform(c);
					
					Point point = fact.createPoint(coord_transformed);
					
					Trafficlight l = new Trafficlight();
					
					l.setGeometry(point);
					l.setId(id);
					
					System.out.print(".-");
					session.save(l);
				}
				
			}
		}
		
		session.getTransaction().commit();

	}

	public static void main(String[] args) throws IllegalDataException, IOException, FactoryException, ParserConfigurationException, SAXException, TransformException {
		
		TrafficLightImporter tli = new TrafficLightImporter();
		tli.read("/Users/besn/Downloads/Copenhagen2.osm");
		// tli.read("/Users/besn/Downloads/trafficlights.osm");
		
	}

}
