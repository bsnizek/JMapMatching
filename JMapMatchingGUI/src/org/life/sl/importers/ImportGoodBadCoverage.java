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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.Respondent;
import org.life.sl.orm.ResultRoute;
import org.life.sl.orm.SourcePoint;
import org.life.sl.orm.SourceRoute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.openstreetmap.josm.io.IllegalDataException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;

/*
 * Imports a shapefile (to be defined in the main method) into the database. Remember to empty tables 
 * sourcepoint and sourceroute.
 */

public class ImportGoodBadCoverage {

	private Session session;

	private com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();

	public ImportGoodBadCoverage(String directory) {
		// TODO: loop over files.
	}

	public ImportGoodBadCoverage(File file) throws IOException {

		setUp();

		Map<String,Serializable> connectParameters = new HashMap<String,Serializable>();
		connectParameters.put("url", file.toURI().toURL());
		// connectParameters.put("create spatial index", true );
		DataStore dataStore = DataStoreFinder.getDataStore(connectParameters);

		String[] typeNames = dataStore.getTypeNames();
		String typeName = typeNames[0];

		System.out.println("Reading content " + typeName);

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
			if (ln != "geom") {
				fieldnames.add(ln);
			}
		}

		int counter = 0;

		try {
			while (iterator.hasNext()) {
				System.out.print("=");

				SimpleFeature feature = iterator.next();
				Geometry geometry = (Geometry) feature.getDefaultGeometry();

				MultiLineString ls = (MultiLineString) geometry;

				Coordinate[] cs = ls.getCoordinates();

				int lngth = cs.length;

				int length = 25;

				double shootOver = 0;

				SourceRoute sr = new SourceRoute();
				Double d =  (Double) feature.getAttribute("rspid");
				long srid = (new Double(d)).longValue();
				sr.setRespondentid((int) srid);
				sr.setId(counter);

				session.save(sr);

				Date date = new Date();

				for (int i=1; i<lngth; i++) {
					System.out.print(".");
					Coordinate pt0 = cs[i-1];
					Coordinate pt1 = cs[i];
					double deltaX = pt1.x - pt0.x;
					double deltaY = pt1.y - pt0.y;

					double distance = pt0.distance(pt1);

					double step = length/distance;

					double l = 0;
					while (l < 1) {

						date.setSeconds(date.getSeconds()+1);

						double newX = pt0.x + l*deltaX;
						double newY = pt0.y + l*deltaY;

						SourcePoint sp = new SourcePoint();
						Point pnt = fact.createPoint(new Coordinate(newX,newY));
						sp.setGeometry(pnt);
						sp.setSourcerouteid((int) counter);
						sp.setT(date);
						session.save(sp);
						l = l + step;
					}



					session.getTransaction().commit();
					setUp();

				}
				counter++;
				System.out.println(" ");

			}

		}
		finally {
			if( iterator != null ){
				// YOU MUST CLOSE THE ITERATOR!
				iterator.close();

			}
		}
		session.getTransaction().commit();
		System.out.println("saved");
	}

	public void setUp() {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
	}

	public static void main(String[] args) throws IllegalDataException, IOException {
		String filename = "/Users/besn/Dropbox/Bikeability/CopenhagenExperiencePoints/2-RoutesClippedToMunicpalityBorder/poly.shp";
		@SuppressWarnings("unused")
		ImportGoodBadCoverage gfi = new ImportGoodBadCoverage(new File(filename));
		System.out.println("Shapefile imported !");
	}



}