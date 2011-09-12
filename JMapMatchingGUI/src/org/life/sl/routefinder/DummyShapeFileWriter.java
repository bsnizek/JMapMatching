package org.life.sl.routefinder;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;

import org.geotools.data.DataUtilities;
//import org.geotools.data.DefaultTransaction;
//import org.geotools.data.Transaction;
//import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.data.simple.SimpleFeatureSource;
//import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
//import org.geotools.geometry.jts.JTSFactoryFinder;
//import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
//import com.vividsolutions.jts.geom.Point;

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
