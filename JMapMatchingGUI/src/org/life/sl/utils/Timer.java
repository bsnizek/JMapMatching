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

import org.apache.log4j.Logger;

/**
 * Utility class for timing purposes.
 * Use {@link #Timer(double int0, double int1)} or {@link #init()} to start the timer, 
 * {@link #getRunTime(boolean)} to return the consumed time,
 * and {@link #showProgress(double)} to show a progress indicator.
 * The progress indicator works at 2 levels specified by {@link #progressInterval}
 * @author Bernhard Barkow
 *
 */
public class Timer {
	private double t_start, t_tot;						///< start and current time, in seconds
	private double[] progressInterval = { 2.5f, 10.f };	///< progress indicator is only updated after this interval, not faster
	private double[] t_tot_last = { 0f, 0f };
	
	public Timer() {
		t_start = 0;
		t_tot = 0;
	}
	
	/**
	 * Constructor initializing the progress display update intervals
	 * @param int0
	 * @param int1
	 */
	public Timer(double int0, double int1) {
		this();
		initIntervals(int0, int1);
	}
	
	/**
	 * Initializes the timer
	 */
	public double init() {
		t_start = getTime();
		return t_start;
	}

	/**
	 * Initializes the timer and the progress indicator update intervals
	 * @param int0 the "faster" interval, for the simple indicator
	 * @param int1 the "longer" interval, for the percentage indicator
	 */
	public double init(double int0, double int1) {
		initIntervals(int0, int1);
		return init();
	}
	
	/**
	 * Sets the progress indicator update intervals
	 * @param int0 the "faster" interval, for the simple indicator
	 * @param int1 the "longer" interval, for the percentage indicator
	 */
	public void initIntervals(double int0, double int1) {
		progressInterval[0] = int0;
		progressInterval[1] = int1;
	}
	
	/**
	 * Calculates the time passed since t_start
	 * @param reset if true, t_start is reset to the current time (i.e., a new interval is started)
	 * @return the time interval in seconds passed since t_start
	 */
	public double getRunTime(boolean reset) {
		double t = getTime() - t_start;
		if (reset) init();
		return t;
	}
	
	/**
	 * Calculates the time passed since t_start and write out a corresponding message
	 * @param reset if true, t_start is reset to the current time (i.e., a new interval is started)
	 * @param msg an additional message to show on the console
	 * @return the time interval in seconds passed since t_start
	 */
	public double getRunTime(boolean reset, String msg) {
		double t = getRunTime(reset);
		//System.out.println(msg + ", t = " + t + "s");
		Logger.getRootLogger().info(msg + ", t = " + t + "s");
		return t;
	}

	/**
	 * Shows a progress indicator, eventually with a percentage
	 * @param p if > 0, the progress is written as percentage
	 */
	public void showProgress(double p) {
		t_tot = getTime() - t_start;
		if (t_tot - t_tot_last[0] > progressInterval[0]) {	// show indicator every x seconds
			System.out.print(".");
			t_tot_last[0] = t_tot;
		}
		if (p > 0f) {	// percentage indicator
			if (t_tot - t_tot_last[1] > progressInterval[1]) {	// show indicator every x seconds
				System.out.printf(" %6.2f%% - ETA %fs\n", 100.*p, t_tot/p - t_tot);
				t_tot_last[1] = t_tot;
			}
		}
	}
	
	/**
	 * @return the system time in seconds, as a floating point number
	 */
	public double getTime() {
		return System.nanoTime() * 1.e-9;
	}
}
