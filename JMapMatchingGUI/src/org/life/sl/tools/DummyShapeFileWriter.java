package org.life.sl.tools;

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

import org.geotools.data.DataUtilities;

import org.geotools.data.simple.SimpleFeatureCollection;

import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class DummyShapeFileWriter {

	/**
	 * @param args
	 * @throws SchemaException 
	 */
	public static void main(String[] args) throws SchemaException {
		 /*
         * We use the DataUtilities class to create a FeatureType that will describe the data in our
         * shapefile.
         * 
         * See also the createFeatureType method below for another, more flexible approach.
         */
        final SimpleFeatureType TYPE = DataUtilities.createType("Location",
                "location:LineString:srid=4326," + // <- the geometry attribute: Point type
                        "name:String," + // <- a String attribute
                        "number:Integer" // a number attribute
        );
        
        SimpleFeatureCollection collection = FeatureCollections.newCollection();
        /*
         * GeometryFactory will be used to create the geometry attribute of each feature (a Point
         * object for the location)
         */
//        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        
        GeometryFactory factory = new GeometryFactory();
        
        LineString geom = factory.createLineString(new Coordinate[] {
        										  new Coordinate(0.0, 0.0),
        										  new Coordinate(1.0, 1.0)
        									});
        featureBuilder.add(geom);

        SimpleFeature feature = featureBuilder.buildFeature(null);
        collection.add(feature);

	}

}
