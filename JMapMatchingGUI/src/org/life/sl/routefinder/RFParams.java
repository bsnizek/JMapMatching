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
import org.life.sl.routefinder.RouteFinder.LabelTraversal;

/**
 * Container for the configuration parameters of the routefinding algorithm;
 * includes methods for reading the parameters from an ini-file. 
 * @author Bernhard Barkow
 * @see org.life.sl.mapmatching.JMMConfig
 * @see <a href="https://github.com/bsnizek/JMapMatching/wiki/JMapMatcherIniFile">JMapMatcherIniFile in the Wiki</a>
 */
public class RFParams {
	public enum Type {
		NetworkBufferSize,		///< size of the buffer around the track, when selecting a network section, in meters(!)
		NetworkBufferSize2,		///< buffer size for optional second run
		DistanceFactor,			///< this is a multiplicative factor to use with the euclidean distance heuristics of the algorithm
		DistanceFactor2,		///< like DistanceFactor, but for the second run and relative to the shortest route
		LabelTraversal,			///< type of label traversal (RouteFinder.LabelTraversal)
		LabelTraversal2,		///< type of label traversal for optional second run (RouteFinder.LabelTraversal)
		MaximumNumberOfRoutes,	///< if this number of routes have been found, the algorithm should terminate
		MaximumNumberOfRoutes2,	///< the same for a second run
		RoutesUsedFromFirstRun,	///< the number of routes to keep from the first run (only relevant if 2 strategies are used)
		ShuffleResetNBack,		///< number of steps to step back in the search tree at a "reset"
		ShuffleResetExtraRoutes,	///< number of extra "BestFirstDR" routes to compute if LabelTraversal==ShuffleReset
		ODDirectionLimit,		///< parameter for weighted direction
		MaxLabels,				///< maximum number of labels to create/evaluate
		MaxRuntime,				///< maximum computation time per run in seconds
		MaxRuntime2,			///< maximum computation time for the second run in seconds
		MaxRuntimeTotal,		///< maximum computation time for multiple runs in seconds
		RejectedLabelsLimit,	///< limit for the number of unsuccessful labels (no routes, only rejected labels)
		NoLabelsResizeNetwork,	///< factor to resize the network if no routes have been found
		NoConnectionResizeNetwork,	///< factor to resize the network if the graph contains no connection from O to D
		NetworkBufferSizeMax,	///< maximum network buffer size
		SwapOD,					///< swap origin and destination
		BothDirections,			///< use both directions to create greater variability
		BothDirections2,		///< use both directions to create greater variability (second run)
		NodeOverlap,			///< maximum number of overlaps in a node for a valid route
		ArticulationPointOverlap,	///< Maximum number of overlaps in an articulation point for a valid route
		EdgeOverlap,			///< maximum number of overlaps in an edge for a valid route
		BridgeOverlap,			///< maximum number of overlaps in a bridge for a valid route
		MinimumLength,			///< minimum length of a valid route; length is defined as the sum of weights over edges in the route 
		MaximumLength,			///< maximum length of a valid route; length is defined as the sum of weights over edges in the route
		MaxPSOverlap,			///< maximum allowed overlap parameter (Path Size Attribute)
		MaxPSOverlap2,			///< maximum allowed overlap parameter (Path Size Attribute), for the second run
		ShowProgressDetail,		///< how much detail regarding the progress is shown ("progress bar"): 0, 1, 2
	}

	// collection of integer constraints, i.e. overlap constraints
	private HashMap<Type, Integer> c_int = null;
	// collection of double constraints, i.e. min/max length constraints
	private HashMap<Type, Double> c_double = null;
	// collection of string parameters
	private HashMap<Type, String> c_str = null;
	// collection of boolean constraints, i.e. yes/no constraints
	private HashMap<Type, Boolean> c_bool = null;

	Map<String, String> iniMap = null;
	
	/** 
	 * Default constructor
	 */
	public RFParams() {
		// allocate HashMaps:
		c_int = new HashMap<Type, Integer>();
		c_double = new HashMap<Type, Double>();
		c_str = new HashMap<Type, String>();
		c_bool = new HashMap<Type, Boolean>();
	}

	/**
	 * Constructor with initialization from integer and double HashMaps
	 * @param ic HashMap containing integer constraints (or null)
	 * @param dc HashMap containing double constraints (or null)
	 */
	public RFParams(HashMap<Type, Integer> ic, HashMap<Type, Double> dc) {
		c_int = ic;
		c_double = dc;
	}
	
	/**
	 * initialize the parameters with some default values
	 */
	public void initDefaults() {
		setInt(Type.MaximumNumberOfRoutes, 1000);	///< maximum number of routes to find (or 0 for infinite)
		setInt(Type.BridgeOverlap, 1);
		setInt(Type.EdgeOverlap, 1);		///< how often each edge may be used
//		constraints.setInt(Constraints.Type.ArticulationPointOverlap, 2);
		setInt(Type.NodeOverlap, 1);		///< how often each single node may be crossed
		setDouble(Type.DistanceFactor, 1.1);		///< how much the route may deviate from the shortest possible
		setDouble(Type.MinimumLength, 0.0);		///< minimum route length
		setDouble(Type.MaximumLength, 1.e20);		///< maximum route length (quasi no limit here)
		setDouble(Type.MaxPSOverlap, .8);			///< maximum allowed overlap between routes
		setDouble(Type.NetworkBufferSize2, 0.);	///< initial buffer size in meters (!)
		setDouble(Type.NetworkBufferSize, 100.);	///< buffer size in meters (!)
		setDouble(Type.ODDirectionLimit, 0.);		///< limit for last edge to fit the OD direction (only for Shuffle(Reset) strategy)
		setInt(Type.RejectedLabelsLimit, 0);		///< limit for unsuccessful labels
		setDouble(Type.NoLabelsResizeNetwork, 0.);	///< factor to resize network buffer if no routes were found
		setDouble(Type.NoConnectionResizeNetwork, 0.);	///< factor to resize network buffer if no routes were found
		set(Type.LabelTraversal, LabelTraversal.BestFirst.toString());		///< way of label traversal
		set(Type.LabelTraversal2, LabelTraversal.ShuffleReset.toString());		///< way of label traversal
		setInt(Type.ShowProgressDetail, 2);		///< how often each edge may be used
	}

	/**
	 * Adds a bunch of constraints from HashMap arguments
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
	 * Sets an integer constraint in the class-wide constraint object
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void setInt(Type type, int value) {
		c_int.put(type, value);
	}

	/**
	 * Sets a double (float) constraint in the class-wide constraint object
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void setDouble(Type type, double value) {
		c_double.put(type, value);
	}

	/**
	 * Sets a constraint value; variant: integer value
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void set(Type type, int value) {
		c_int.put(type, value);
	}

	/**
	 * Sets a constraint value; variant: double value
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void set(Type type, double value) {
		c_double.put(type, value);
	}

	/**
	 * Sets a constraint value; variant: string value
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void set(Type type, String value) {
		c_str.put(type, value);
	}

	/**
	 * Sets a constraint value; variant: boolean value
	 * @param type	the constraint key
	 * @param value	the value for this constraint
	 */
	public void set(Type type, boolean value) {
		c_bool.put(type, value);
	}

	/**
	 * Gets the value of an integer constraint
	 * @param type the constraint key
	 * @return The value of the constraint.
	 */
	public int getInt(Type type) {
		return c_int.get(type);
	}

	/**
	 * Gets the value of a double (float) constraint
	 * @param type the constraint key
	 * @return The value of the constraint.
	 */
	public double getDouble(Type type) {
		Double d = c_double.get(type);
		return d;
	}

	/**
	 * Gets the string value of a constraint as read from the ini file
	 * @param type the constraint key
	 * @return The value of the constraint.
	 */
	public String getString(Type type) {
		return (String) c_str.get(type);
	}

	/**
	 * Gets the string value of a constraint as read from the ini file
	 * @param type the constraint key
	 * @return The value of the constraint.
	 */
	public boolean getBool(Type type) {
		return c_bool.get(type);
	}
	
	/**
	 * Reads parameters from a given ini-File (Win-Ini format)
	 * @param iniFileName name of the configuration file 
	 * @return number of parameters read from the file; -1 if the file could not be opened
	 */
	public int readFromFile(String iniFileName) {
		Logger.getRootLogger().info("Reading ini file " + iniFileName);
		int r = -1;	// default: no file found
		try {
			Ini ini = new Ini(new File(iniFileName));
			iniMap = ini.get("RouteFinder");
			
			r = 0;
			if (map2Int(iniMap, "MaximumNumberOfRoutes", Type.MaximumNumberOfRoutes)) r++;
			if (map2Int(iniMap, "MaximumNumberOfRoutes2", Type.MaximumNumberOfRoutes2)) r++;
			if (map2Int(iniMap, "RoutesUsedFromFirstRun", Type.RoutesUsedFromFirstRun)) r++;
			if (map2Int(iniMap, "NodeOverlap", Type.NodeOverlap)) r++;
			if (map2Int(iniMap, "ArticulationPointOverlap", Type.ArticulationPointOverlap)) r++;
			if (map2Int(iniMap, "EdgeOverlap", Type.EdgeOverlap)) r++;
			if (map2Int(iniMap, "BridgeOverlap", Type.BridgeOverlap)) r++;
			if (map2Double(iniMap, "MinimumLength", Type.MinimumLength)) r++;
			if (map2Double(iniMap, "MaximumLength", Type.MaximumLength)) r++;
			if (map2Double(iniMap, "MaxPSOverlap", Type.MaxPSOverlap)) r++;
			if (map2Double(iniMap, "MaxPSOverlap2", Type.MaxPSOverlap2)) r++;
			if (map2Double(iniMap, "DistanceFactor", Type.DistanceFactor)) r++;
			if (map2Double(iniMap, "DistanceFactor2", Type.DistanceFactor2)) r++;
			if (map2Double(iniMap, "NetworkBufferSize", Type.NetworkBufferSize)) r++;
			if (map2Double(iniMap, "NetworkBufferSize2", Type.NetworkBufferSize2)) r++;
			if (map2Int(iniMap, "RejectedLabelsLimit", Type.RejectedLabelsLimit)) r++;
			if (map2Double(iniMap, "NoLabelsResizeNetwork", Type.NoLabelsResizeNetwork)) r++;
			if (map2Double(iniMap, "NoConnectionResizeNetwork", Type.NoConnectionResizeNetwork)) r++;
			if (map2Int(iniMap, "MaxLabels", Type.MaxLabels)) r++;
			if (map2Double(iniMap, "MaxRuntime", Type.MaxRuntime)) r++;
			if (map2Double(iniMap, "MaxRuntime2", Type.MaxRuntime2)) r++;
			if (map2Double(iniMap, "MaxRuntimeTotal", Type.MaxRuntimeTotal)) r++;
			if (map2Double(iniMap, "NetworkBufferSizeMax", Type.NetworkBufferSizeMax)) r++;
			if (map2String(iniMap, "LabelTraversal", Type.LabelTraversal)) r++;
			if (map2String(iniMap, "LabelTraversal2", Type.LabelTraversal2)) r++;
			if (map2Int(iniMap, "ShuffleResetExtraRoutes", Type.ShuffleResetExtraRoutes)) r++;
			if (map2Int(iniMap, "ShuffleResetNBack", Type.ShuffleResetNBack)) r++;
			if (map2Double(iniMap, "ODDirectionLimit", Type.ODDirectionLimit)) r++;
			if (map2Int(iniMap, "ShowProgressDetail", Type.ShowProgressDetail)) r++;
			if (map2Bool(iniMap, "SwapOD", Type.SwapOD)) r++;
			if (map2Bool(iniMap, "BothDirections", Type.BothDirections)) r++;
			if (map2Bool(iniMap, "BothDirections2", Type.BothDirections2)) r++;
		} catch (InvalidFileFormatException e) {
			Logger.getRootLogger().error("Invalid file format");
		} catch (IOException e) {
			Logger.getRootLogger().error("Error reading ini file - " + e);
		}
		return r;
	}
	
	/**
	 * Reads an integer value from a given Map and stores it in the Integer Hashmap
	 * @param iniMap the map containing keys and values
	 * @param mapKey key to look for in the map
	 * @param parKey parameter key (enum) to store the value
	 * @return true if the map contained the key (and thus the value could be stored)
	 */
	private boolean map2Int(Map<String, String> iniMap, String mapKey, Type parKey) {
		boolean ok = iniMap.containsKey(mapKey); 
		if (ok) {
			Double d = Double.parseDouble(iniMap.get(mapKey));
			setInt(parKey, d.intValue());
		}
		return ok;
	}
	/**
	 * Reads a floating point value from a given Map and stores it in the Double Hashmap
	 * @param iniMap the map containing keys and values
	 * @param mapKey key to look for in the map
	 * @param parKey parameter key (enum) to store the value
	 * @return true if the map contained the key (and thus the value could be stored)
	 * @see #map2Int
	 */
	private boolean map2Double(Map<String, String> iniMap, String mapKey, Type parKey) {
		boolean ok = iniMap.containsKey(mapKey); 
		if (ok) setDouble(parKey, Double.parseDouble(iniMap.get(mapKey)));
		return ok;
	}
	/**
	 * Reads a floating point value from a given Map and stores it in the Double Hashmap
	 * @param iniMap the map containing keys and values
	 * @param mapKey key to look for in the map
	 * @param parKey parameter key (enum) to store the value
	 * @return true if the map contained the key (and thus the value could be stored)
	 * @see #map2Int
	 */
	private boolean map2String(Map<String, String> iniMap, String mapKey, Type parKey) {
		boolean ok = iniMap.containsKey(mapKey); 
		if (ok) set(parKey, iniMap.get(mapKey));
		return ok;
	}
	/**
	 * Reads a boolean value from a given Map and stores it in the Boolean Hashmap
	 * @param iniMap the map containing keys and values
	 * @param mapKey key to look for in the map
	 * @param parKey parameter key (enum) to store the value
	 * @return true if the map contained the key (and thus the value could be stored)
	 */
	private boolean map2Bool(Map<String, String> iniMap, String mapKey, Type parKey) {
		boolean ok = iniMap.containsKey(mapKey); 
		if (ok) {
			Boolean d = Boolean.parseBoolean(iniMap.get(mapKey));
			set(parKey, d.booleanValue());
		}
		return ok;
	}
}
