package org.life.sl.importers;


import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.Session;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.SourcePoint;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.openstreetmap.josm.io.IllegalDataException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class OSMWebImporter {


	public OSMWebImporter(int sourcerouteId) throws IOException, URISyntaxException, IllegalDataException, FactoryException, TransformException {



		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query result = session.createQuery("from SourcePoint WHERE sourcerouteid=" + sourcerouteId);
		Iterator<SourcePoint> iter = result.iterate();

		GeometryFactory factory = new GeometryFactory();

		Point[] coordinates = new Point[result.list().size()];

		int i = 0;
		while (iter.hasNext()) {
			SourcePoint sP = iter.next();
			Point geometry = sP.getGeometry();
			coordinates[i] = geometry;
			i = i + 1;
		}

		MultiPoint mP = factory.createMultiPoint(coordinates);
		Geometry boundary = mP.getEnvelope();
		// Coordinate[] coords = boundary.getCoordinates();
		// Coordinate c0 = coords[0];
		// Coordinate c1 = coords[1];

		Double step = 0.0100;
		Double left = 12.4910;
		Double top = 55.6550;
		for (int j=-15; j<=15; j++) {
			for (int k=-15; k<=15;k++) {
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
				System.out.println(j + "/" + 30 + "-" + k + "/" + 30);
			}

		}




	}

	public static void main(String[] args) throws IllegalDataException, IOException, URISyntaxException, FactoryException, TransformException {
		OSMWebImporter osmw = new OSMWebImporter(12158);

	}



}
