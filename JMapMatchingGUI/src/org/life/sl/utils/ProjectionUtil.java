package org.life.sl.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author Bernhard Snizek, bs@koenigsnizek.org
 * A utility, which returns a CoordinateReferenceSystem given by a .prj file.
 */
public class ProjectionUtil {
	
	public CoordinateReferenceSystem getCRS(String filename) throws IOException, FactoryException {
	
		File f = new File(filename);
		
		//URL u = f.toURI().toURL();
		//ShapefileDataStore sds = new ShapefileDataStore(u);
		//String s = sds.getSchema().getDefaultGeometry().getCoordinateSystem().toString();
		
		StringBuffer sb = new StringBuffer();
	    try {
	        BufferedReader reader = new BufferedReader(new FileReader(f));
	        String s = null;
	        while((s = reader.readLine()) != null) {
	            sb.append(s);
	        }
	       
	    } finally {}
	    return CRS.parseWKT(sb.toString());
	}
}
