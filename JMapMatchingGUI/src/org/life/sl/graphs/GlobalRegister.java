package org.life.sl.graphs;

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
