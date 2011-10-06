package org.life.sl.utils;

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
