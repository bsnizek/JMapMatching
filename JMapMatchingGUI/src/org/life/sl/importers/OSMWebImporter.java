package org.life.sl.importers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.Session;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.SourcePoint;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPoint;

public class OSMWebImporter {
	

	public OSMWebImporter(int sourcerouteId) throws IOException, URISyntaxException, IllegalDataException {
	
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		Query result = session.createQuery("from SourcePoint WHERE sourcerouteid=" + sourcerouteId);
		Iterator iter = result.iterate();
		
		GeometryFactory factory = new GeometryFactory();
		
		Point[] coordinates = new Point[result.list().size()];
		
		int i = 0;
		while (iter.hasNext()) {
			SourcePoint sP = (SourcePoint) iter.next();
			Point geometry = sP.getGeometry();
			coordinates[i] = geometry;
			i = i + 1;
		}
		
		MultiPoint mP = factory.createMultiPoint(coordinates);
		Geometry boundary = mP.getEnvelope();
		Coordinate[] coords = boundary.getCoordinates();
		Coordinate c0 = coords[0];
		Coordinate c1 = coords[1];
		
		Double step = 0.0050;
		Double left = 12.4910;
		Double top = 55.6550;
		for (int j=-3; j<15; j++) {
			for (int k=-3; k<15;k++) {
				Double new_left = j*step + left;
				Double new_right = new_left + step;
				Double new_top = k*step + top;
				Double new_bottom = new_top + step;
				String url = "http://api.openstreetmap.org/api/0.6/map?bbox=" + new_left +"," + new_top + "," + new_right + ","+ new_bottom;
				URL osm = new URL(url);
				System.out.println(url);
				URLConnection osmConnection = osm.openConnection(); 
				
				InputStream in = osmConnection.getInputStream();
				
				OSMImporter osmImporter = new OSMImporter();
				osmImporter.readOSMFilefromStream(in);
				System.out.println(j + "/" + 15 + "-" + k + "/" + 15);
			}
			
		}
		
		
		
		
	}
	
	public static void main(String[] args) throws IllegalDataException, IOException, URISyntaxException {
		OSMWebImporter osmw = new OSMWebImporter(12158);
		
	}
	
	

}
