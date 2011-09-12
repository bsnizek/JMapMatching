package org.life.sl.graphs;

public class GlobalRegister {
	
	
	private static GlobalRegister instance = null;
	
	public final static boolean SNAP = false;

	public final static double GLOBAL_SNAP = 0.01;
	
	public double getSnap() {
		return GLOBAL_SNAP;
	}

	public static GlobalRegister getInstance() {
		if(instance == null) {
			instance = new GlobalRegister();
		}
		return instance;
	}



	
	
	
}
