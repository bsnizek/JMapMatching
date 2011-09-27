package org.life.sl.graphs;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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

import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * @author Pimin Konstantin Kefaloukos
 * @author Bernhard Snizek
 */
public class LineStringReader extends AbstractGeometryDataReader {

	private Collection<LineString> lineStrings;



	/**
	 * Make a new LineStringDataReader.
	 */
	public LineStringReader(String shp) {
		super(shp);
		lineStrings = new ArrayList<LineString>();
	}

	public Collection<LineString> getLineStrings() {
		return lineStrings;
	}

	public void read() throws IOException {

		//////////////////////////////////////////////////////
		// READ GRAPH
		//////////////////////////////////////////////////////

		Map<String,Serializable> connectParameters = new HashMap<String,Serializable>();
		File file = new File(this.shapeFile);
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
        
        try {
            while (iterator.hasNext()) {
            	LineString lineString = null;
                SimpleFeature feature = iterator.next();

                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                if(geometry.getClass() != MultiLineString.class && geometry.getClass() != LineString.class) {
    				// TODO: ErrorHandler.getInstance().error("Error, shapefile must contain lines", 2);
    				return;
    			}
    			
    			// handle MultiLineString -> LineString
    			if(geometry.getClass() == MultiLineString.class) {
    				// geometry is MultiLineString
    				// get the first LineString
    				geometry = geometry.getGeometryN(0);
    				if(geometry.getNumGeometries() > 1) {
    					// TODO: ErrorHandler.getInstance().error("Found more than one linestring in MultiLineString", 2);
    				}
    			}
    			if(geometry.getClass() == LineString.class) {
    				// add linestring
    				lineString = (LineString) geometry;
    				if(CoordinateArrays.hasRepeatedPoints(lineString.getCoordinates())) {
    					// TODO: ErrorHandler.getInstance().error("Found LineString with repeated coordinates (skipping): " + lineString.getCoordinates(), 2);					
    					continue;
    				}
    			}
    			// read data from the row
    			HashMap<String, Object> attributes = new HashMap<String, Object>();
    			for (String fn : fieldnames) {
    				attributes.put(fn, feature.getAttribute(fn));
    				
    			}
    			attributes.put("geometry", geometry);
    			lineString.setUserData(attributes);
				lineStrings.add(lineString);                
            }
        }
        finally {
        	if( iterator != null ){
        		// YOU MUST CLOSE THE ITERATOR!
                iterator.close();
        	}
        }
		
	}
	
	public static void main(String... args) {
		LineStringReader reader = new LineStringReader("testdata/Sparse_bigger0.shp");
		try {
			reader.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
