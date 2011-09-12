package org.life.sl.readers.shapefile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
//import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

//import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class PointFileReader {
	
	private ArrayList<Point> points;
	
	public PointFileReader(File file) throws IOException {
		
		points = new ArrayList<Point>();
		
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
        	if (ln != "the_geom") {
        		fieldnames.add(ln);
        	}
        }
        
        try {
            while (iterator.hasNext()) {
            	Point point = null;
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
    			// Debug output:
                //System.out.println(geometry.getClass());
                
                if(geometry.getClass() != Point.class && geometry.getClass() != Point.class) {
//    				ErrorHandler.getInstance().error("Error, shapefile must contain lines", 2);
                	System.out.println("ERROR: Shapefile must contain points, but does not.");
    				return;
    			}
                
//                if(geometry.getClass() == Point.class) {
//    				// add linestring
//    				point = (Point) geometry;
//    				if(CoordinateArrays.hasRepeatedPoints(point.getCoordinates())) {
//    					// TODO: ErrorHandler.getInstance().error("Found LineString with repeated coordinates (skipping): " + lineString.getCoordinates(), 2);					
//    					continue;
//    				}
//    			}
    			// read data from the row
    			HashMap<String, Object> attributes = new HashMap<String, Object>();
    			for (String fn : fieldnames) {
    				attributes.put(fn, feature.getAttribute(fn));
    				
    			}
    			
    			point = (Point) geometry;
    			
//    			attributes.put("_mester", "Hans");
    			// Debug output:
    			//System.out.println(point);
    			point.setUserData(attributes);
				points.add(point);         
                
            }
        }
        finally {
        	if( iterator != null ){
        		// YOU MUST CLOSE THE ITERATOR!
                iterator.close();
        	}
        }
		
	}

	public ArrayList<Point> getPoints() {
		return points;
	}

}
