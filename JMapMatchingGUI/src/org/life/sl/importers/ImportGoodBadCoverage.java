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
import java.sql.Timestamp;
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
import org.hibernate.Session;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.SourcePoint;
import org.life.sl.orm.SourceRoute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.openstreetmap.josm.io.IllegalDataException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;

/*
 * Imports a shapefile (to be defined in the main method) into the database. Remember to empty tables 
 * sourcepoint and sourceroute.
 */

public class ImportGoodBadCoverage {

	private static final int LENGTH = 10;

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

				int nCoord = cs.length;	// number of coordinates
				int length = LENGTH;

				SourceRoute sr = new SourceRoute();
				Double d =  (Double) feature.getAttribute("rspid");
				long srid = d.longValue();
				sr.setRespondentid((int) srid);
				sr.setId(counter);	// the new sourcerouteID

				session.save(sr);

				Date t = new Date();	// current date as base for the increasing timestamp
				long timestamp0 = t.getTime();
				int pcounter = 0;		// point counter
				
				double l = 0;			// length on the current polyline segment
				
				for (int i=1; i<nCoord; i++) {
					System.out.print(".");
					Coordinate pt0 = cs[i-1];
					Coordinate pt1 = cs[i];
					double distance = pt0.distance(pt1);
					double deltaX = (pt1.x - pt0.x) / distance;
					double deltaY = (pt1.y - pt0.y) / distance;
					
					while (l <= distance) {	// loop up to the end of the current segment
						pcounter++;

						double newX = pt0.x + l*deltaX;
						double newY = pt0.y + l*deltaY;
						Coordinate c1 = new Coordinate(newX, newY);
						
						SourcePoint sp = new SourcePoint();
						sp.setGeometry(fact.createPoint(c1));
						sp.setSourcerouteid(counter);
						sp.setT(new Timestamp(timestamp0 + pcounter*1000));	// increment timestamp (1 point per second)
						pcounter++;
						session.save(sp);
						l += length;
					}
					l -= distance;	// = remaining distance on the next segment 

					session.getTransaction().commit();
					setUp();	// re-open database session
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
		PathSegmentGraph psg = new PathSegmentGraph();
	}



}