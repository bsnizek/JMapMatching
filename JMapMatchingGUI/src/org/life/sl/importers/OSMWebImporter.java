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

import com.vividsolutions.jts.geom.Point;

public class OSMWebImporter {


	public OSMWebImporter(int sourcerouteId) throws IOException, URISyntaxException, IllegalDataException, FactoryException, TransformException {
		
		// String OSM_API_STRING = "http://api.openstreetmap.org/api/0.6/map?bbox=";
		String OSM_API_STRING = "http://open.mapquestapi.com/xapi/api/0.6/map?bbox=";

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query result = session.createQuery("from SourcePoint WHERE sourcerouteid=" + sourcerouteId);
		@SuppressWarnings("unchecked")
		Iterator<SourcePoint> iter = result.iterate();

		//GeometryFactory factory = new GeometryFactory();

		Point[] coordinates = new Point[result.list().size()];

		int i = 0;
		while (iter.hasNext()) {
			SourcePoint sP = iter.next();
			Point geometry = sP.getGeometry();
			coordinates[i] = geometry;
			i = i + 1;
		}

		//MultiPoint mP = factory.createMultiPoint(coordinates);
		//Geometry boundary = mP.getEnvelope();
		// Coordinate[] coords = boundary.getCoordinates();
		// Coordinate c0 = coords[0];
		// Coordinate c1 = coords[1];

		Double step = 0.0100;
		Double left = 12.4910;
		Double top = 55.6350;
		for (int j=-19; j<=20; j++) {  //left-right
			for (int k=-22; k<=16;k++) {  // bottom -> top
				Double new_left = j*step + left;
				Double new_right = new_left + step;
				Double new_top = k*step + top;
				Double new_bottom = new_top + step;
				String url = OSM_API_STRING + new_left +"," + new_top + "," + new_right + ","+ new_bottom;
				URL osm = new URL(url);
				System.out.println(url);

				boolean ok = false;
				int counter = 0; 
				InputStream in = null;

				while (!ok && counter<5) {
					try {
						URLConnection osmConnection = osm.openConnection(); 
						in = osmConnection.getInputStream();
						ok = true;
					} catch (java.io.IOException e) {
						System.out.println("OSM timeout - waiting for 5 minutes ...");
						try {
							Thread.sleep(300000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						counter++;
					}
				}

				if (in != null) {
					OSMImporter osmImporter = new OSMImporter();
					osmImporter.readOSMFilefromStream(in);
					System.out.println(j + "/" + 30 + "-" + k + "/" + 30);
				} else {
					System.out.println("Timeout at import!");
				}
			} 

		}

	}

	public static void main(String[] args) throws IllegalDataException, IOException, URISyntaxException, FactoryException, TransformException {
		@SuppressWarnings("unused")
		OSMWebImporter osmw = new OSMWebImporter(12158);
	}

}
