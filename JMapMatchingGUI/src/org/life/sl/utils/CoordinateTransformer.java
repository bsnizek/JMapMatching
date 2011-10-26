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

import java.io.IOException;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Bernhard Snizek
 */
public class CoordinateTransformer {
	
	CoordinateReferenceSystem sourceCRS;
	CoordinateReferenceSystem targetCRS;
	MathTransform mt;
	
	/**
	 * @param epsg_in	The EPSG from which we convert
	 * @param epsg_out	The EPSG to which we convert
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 */
	public CoordinateTransformer(String epsg_in, String epsg_out) throws NoSuchAuthorityCodeException, FactoryException {
		
		this.sourceCRS = null;
		this.targetCRS = null;
		this.mt = null;
		
		//final CRSAuthorityFactory factory = CRS.getAuthorityFactory(false); 
		
		//this.sourceCRS = factory.createCoordinateReferenceSystem(epsg_in); 
		
		try {
			
			this.sourceCRS = CRS.decode(epsg_in, true);
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			this.targetCRS = CRS.decode(epsg_out, true);
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		try {
			this.mt = CRS.findMathTransform(sourceCRS, targetCRS, false);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public CoordinateTransformer(CoordinateReferenceSystem crs1, String epsg_out) {
		this.sourceCRS = crs1;
		
		try {
			this.targetCRS = CRS.decode(epsg_out, true);
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			this.mt = CRS.findMathTransform(sourceCRS, targetCRS, true);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public CoordinateTransformer(CoordinateReferenceSystem crs1, CoordinateReferenceSystem crs2) {
		this.sourceCRS = crs1;
		this.targetCRS = crs2;
		
		
		try {
			this.mt = CRS.findMathTransform(this.sourceCRS, this.targetCRS, true);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * transforms a coordinate using the transformation generated on instant
	 * @param in the coordinate
	 * @return the new coordinate
	 * @throws MismatchedDimensionException
	 * @throws TransformException
	 */
	public Coordinate transform(Coordinate in) throws TransformException {

		return JTS.transform(in, null, this.mt); 
		
	}
	
	public Geometry transform(Geometry in) throws TransformException {

		return JTS.transform(in, this.mt); 
		
	}
	
	/**
	 * a main which shows the use of the transformer
	 * @param args
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws MismatchedDimensionException
	 * @throws TransformException
	 */
	public static void main(String[] args) throws NoSuchAuthorityCodeException, FactoryException, TransformException {

		CoordinateReferenceSystem crs1;
		ProjectionUtil pu = new ProjectionUtil();
		
		crs1 = null;
		
		try {
			crs1 = pu.getCRS("/Users/bsnizek/Projects/Agents10/kvintus4/data/MaFreiNa/Street_Path.prj");
			// crs1 = pu.getCRS("/Users/bsnizek/Projects/Agents10/kvintus4/data/MaFreiNa/1903plus.prj");
			System.out.println(crs1);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String srs2 = "EPSG:4326";
		//String srs1 = "EPSG:21781"; //22184 //25832 //32632 25832
		//CoordinateTransformer ct = new CoordinateTransformer(srs1, srs2);
		
		CoordinateTransformer ct = new CoordinateTransformer(crs1, srs2);
		
		
		// christian.schmid@nationalpark.ch

		System.out.println(ct.transform(new Coordinate(new Float(816519.54),new Float(170203.37))));
	}
	
}


