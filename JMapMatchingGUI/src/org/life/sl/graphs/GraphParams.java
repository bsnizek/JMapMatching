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

public class GraphParams {
	
	private static GraphParams instance = null;
	
	public static boolean SNAP = false;

	public static double GLOBAL_SNAP_DIST = 0.1;
	
	public boolean getSnap() { return SNAP; }
	public void setSnap(boolean b) { SNAP = b; }
	public double getSnapDistance() { return GLOBAL_SNAP_DIST; }
	public void setSnapDistance(double d) { GLOBAL_SNAP_DIST = d; }

	public static GraphParams getInstance() {
		if(instance == null) instance = new GraphParams();
		return instance;
	}
}
