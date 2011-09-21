package org.life.sl.utils;

public class Timer {
	private double t_start, t_tot;						///> start and current time, in seconds
	private double[] progressInterval = { 2.5f, 10.f };	///> progress indicator is only updated after this interval, not faster
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
	 * initialize the timer
	 */
	public double init() {
		t_start = getTime();
		return t_start;
	}

	/**
	 * initialize the timer and the progress indicator update intervals
	 * @param int0 the "faster" interval, for the simple indicator
	 * @param int1 the "longer" interval, for the percentage indicator
	 */
	public double init(double int0, double int1) {
		initIntervals(int0, int1);
		return init();
	}
	
	/**
	 * set the progress indicator update intervals
	 * @param int0 the "faster" interval, for the simple indicator
	 * @param int1 the "longer" interval, for the percentage indicator
	 */
	public void initIntervals(double int0, double int1) {
		progressInterval[0] = int0;
		progressInterval[1] = int1;
	}
	
	/**
	 * calculate the time passed since t_start
	 * @param reset if true, t_start is reset to the current time (i.e., a new interval is started)
	 * @return the time interval in seconds passed since t_start
	 */
	public double getRunTime(boolean reset) {
		double t = getTime() - t_start;
		if (reset) init();
		return t;
	}
	
	/**
	 * calculate the time passed since t_start and write out a corresponding message
	 * @param reset if true, t_start is reset to the current time (i.e., a new interval is started)
	 * @param msg an additional message to show on the console
	 * @return the time interval in seconds passed since t_start
	 */
	public double getRunTime(boolean reset, String msg) {
		double t = getRunTime(reset);
		System.out.println(msg + ", t = " + t + "s");
		return t;
	}

	/**
	 * show a progress indicator, eventually with a percentage
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
				System.out.printf(" %6.2f%%\n - ETA %fs", 100.*p, t_tot/p - t_tot);
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
