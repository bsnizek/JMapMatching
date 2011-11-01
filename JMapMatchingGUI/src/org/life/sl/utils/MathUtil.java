package org.life.sl.utils;

/**
 * Some mathematical utility functions
 * @author bb
 *
 */
public class MathUtil {
	
	/**
	 * Maps an angle to the interval [-pi,pi]
	 * @param a the angle in radians
	 * @return angle mapped to [-pi,pi]
	 */
	public static double mapAngle_radians(double a) {
		while (a >  Math.PI) a -= 2*Math.PI;
		while (a < -Math.PI) a += 2*Math.PI;
		return a;
	}
	
	/**
	 * Maps an angle to the interval [-180,180]
	 * @param a the angle in degrees
	 * @return angle mapped to [-180,180]
	 */
	public static double mapAngle_degrees(double a) {
		while (a > 180) a -= 360;
		while (a < -180) a += 360;
		return a;
	}

}
