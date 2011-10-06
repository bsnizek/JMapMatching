package org.life.sl.routefinder;

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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class RFParams {
	public enum Type {
		NodeOverlap,	///> maximum number of overlaps in a node for a valid route
		ArticulationPointOverlap,	///> Maximum number of overlaps in an articulation point for a valid route
		EdgeOverlap,	///> maximum number of overlaps in an edge for a valid route
		BridgeOverlap,	///> maximum number of overlaps in a bridge for a valid route
		MinimumLength,	///> minimum length of a valid route; length is defined as the sum of weights over edges in the route 
		MaximumLength,	///> maximum length of a valid route; length is defined as the sum of weights over edges in the route 
		MaximumNumberOfRoutes,	///> if this number of routes have been found, the algorithm should terminate
		DistanceFactor,	///> this is a multiplicative factor to use with the euclidean distance heuristics of the algorithm
		NetworkBufferSize,	///> size of the buffer around the track, when selecting a network section, in meters(!)
	}

	// collection of integer constraints, i.e. overlap constraints
	private HashMap<Type, Integer> c_int = null;
	// collection of double constraints, i.e. min/max length constraints
	private HashMap<Type, Double> c_double = null;

	/** 
	 * default constructor
	 */
	public RFParams() {
		// allocate HashMaps:
		c_int = new HashMap<Type, Integer>();
		c_double = new HashMap<Type, Double>();
	}

	/**
	 * constructor with initialization from integer and double HashMaps
	 * @param ic HashMap containing integer constraints (or null)
	 * @param dc HashMap containing double constraints (or null)
	 */
	public RFParams(HashMap<Type, Integer> ic, HashMap<Type, Double> dc) {
		c_int = ic;
		c_double = dc;
	}

	/**
	 * add a bunch of constraints from HashMap arguments
	 * @param ic HashMap containing integer constraints (or null)
	 * @param dc HashMap containing double constraints (or null)
	 */
	public void setConstraints(HashMap<Type, Integer> ic, HashMap<Type, Double> dc) {
		if (ic != null) {
			for (Type key : ic.keySet()) setInt(key, ic.get(key));
		}
		if (dc != null) {
			for (Type key : dc.keySet()) setDouble(key, dc.get(key));
		}
	}

	/**
	 * Set an integer constraint in the class-wide constraint object
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void setInt(Type type, int value) {
		c_int.put(type, value);
	}

	/**
	 * Set a double (float) constraint in the class-wide constraint object
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void setDouble(Type type, double value) {
		c_double.put(type, value);
	}

	/**
	 * Set a constraint value; variant: integer value
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void set(Type type, int value) {
		c_int.put(type, value);
	}

	/**
	 * Set a constraint value; variant: double value
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void set(Type type, double value) {
		c_double.put(type, value);
	}

	/**
	 * Get the value of an integer constraint
	 * @param type the constraint key
	 * @return The value of the constraint.
	 */
	public int getInt(Type type) {
		return c_int.get(type);
	}

	/**
	 * Get the value of a double (float) constraint
	 * @param type the constraint key
	 * @return The value of the constraint.
	 */
	public double getDouble(Type type) {
		Double d = c_double.get(type);
		return d;
	}
	
	/**
	 * read parameters from a given ini-File (Win-Ini format)
	 * @param iniFileName name of the configuration file 
	 * @return number of parameters read from the file; -1 if the file could not be opened
	 */
	public int readFromFile(String iniFileName) {
		Logger.getRootLogger().info("Reading ini file " + iniFileName);
		int r = -1;	// default: no file found
		try {
			Ini ini = new Ini(new File(iniFileName));
			Map<String, String> iniMap = ini.get("RouteFinder");
			
			r = 0;
			if (map2Int(iniMap, "MaximumNumberOfRoutes", Type.MaximumNumberOfRoutes)) r++;
			if (map2Int(iniMap, "NodeOverlap", Type.NodeOverlap)) r++;
			if (map2Int(iniMap, "ArticulationPointOverlap", Type.ArticulationPointOverlap)) r++;
			if (map2Int(iniMap, "EdgeOverlap", Type.EdgeOverlap)) r++;
			if (map2Int(iniMap, "BridgeOverlap", Type.BridgeOverlap)) r++;
			if (map2Double(iniMap, "MinimumLength", Type.MinimumLength)) r++;
			if (map2Double(iniMap, "MaximumLength", Type.MaximumLength)) r++;
			if (map2Double(iniMap, "DistanceFactor", Type.DistanceFactor)) r++;
			if (map2Double(iniMap, "NetworkBufferSize", Type.NetworkBufferSize)) r++;
		} catch (InvalidFileFormatException e) {
			Logger.getRootLogger().error("Invalid file format");
		} catch (IOException e) {
			Logger.getRootLogger().error("Error reading ini file - " + e);
		}
		return r;
	}
	
	/**
	 * from a given Map, read an integer value and store it in the Integer Hashmap
	 * @param iniMap the map containing keys and values
	 * @param mapKey key to look for in the map
	 * @param parKey parameter key (enum) to store the value
	 * @return
	 */
	private boolean map2Int(Map<String, String> iniMap, String mapKey, Type parKey) {
		boolean ok = iniMap.containsKey(mapKey); 
		if (ok) setInt(parKey, Integer.parseInt(iniMap.get("mapKey")));
		return ok;
	}
	/**
	 * from a given Map, read a floating point value and store it in the Double Hashmap
	 * @see map2Int
	 * @param iniMap the map containing keys and values
	 * @param mapKey key to look for in the map
	 * @param parKey parameter key (enum) to store the value
	 * @return
	 */
	private boolean map2Double(Map<String, String> iniMap, String mapKey, Type parKey) {
		boolean ok = iniMap.containsKey(mapKey); 
		if (ok) setDouble(parKey, Double.parseDouble(iniMap.get("mapKey")));
		return ok;
	}
}
