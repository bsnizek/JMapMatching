package org.life.sl.utils;

import java.util.ArrayList;

/**
 * Computes a set of average values for the N best etc. 
 * @author bb
 *
 */
public class BestNAverageStat {

	private ArrayList<Double> data;
	double sum = 0;
	private double[] averages;
	private int[] nBest;
	
	/**
	 * @param nBest an array of values determining at which positions the averages shall be computed (e.g. [5,10,25,50,100])
	 */
	public BestNAverageStat(int[] nBest) {
		this.nBest = nBest;
		averages = new double[nBest.length];
		data = new ArrayList<Double>(nBest[nBest.length-1]);
	}
	
	/**
	 * Adds a value to the statistics; the averages are computed on the fly.
	 * @param d a data value
	 */
	public void add(double d) {
		data.add(d);
		sum += d;
		for (int i = 0; i < nBest.length; i++) {
			if (data.size() == nBest[i]) averages[i] = sum / (double)nBest[i];
		}
	}
	
	public double[] getAverages() {
		return averages;
	}
	
	/**
	 * Computes the current average of all values
	 * @return
	 */
	public double getAverage() {
		return sum / (double)data.size();
	}
}
