package org.life.sl.routefinder;

import java.util.HashMap;

public class Constraints {
	public enum Type {
		NodeOverlap,	///> maximum number of overlaps in a node for a valid route
		ArticulationPointOverlap,	///> Maximum number of overlaps in an articulation point for a valid route
		EdgeOverlap,	///> maximum number of overlaps in an edge for a valid route
		BridgeOverlap,	///> maximum number of overlaps in a bridge for a valid route
		MinimumLength,	///> minimum length of a valid route; length is defined as the sum of weights over edges in the route 
		MaximumLength,	///> maximum length of a valid route; length is defined as the sum of weights over edges in the route 
		MaximumNumberOfRoutes,	///> if this number of routes have been found, the algorithm should terminate
		DistanceFactor,	///> this is a multiplicative factor to use with the euclidean distance heuristics of the algorithm
	}

	// collection of integer constraints, i.e. overlap constraints
	private HashMap<Type, Integer> c_int = null;
	// collection of double constraints, i.e. min/max length constraints
	private HashMap<Type, Double> c_double = null;

	/** 
	 * default constructor
	 */
	public Constraints() {
		// allocate HashMaps:
		c_int = new HashMap<Type, Integer>();
		c_double = new HashMap<Type, Double>();
	}

	/**
	 * constructor with initialization from integer and double HashMaps
	 * @param ic HashMap containing integer constraints (or null)
	 * @param dc HashMap containing double constraints (or null)
	 */
	public Constraints(HashMap<Type, Integer> ic, HashMap<Type, Double> dc) {
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
}
