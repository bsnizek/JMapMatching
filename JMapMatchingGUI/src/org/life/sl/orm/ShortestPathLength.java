package org.life.sl.orm;

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

public class ShortestPathLength {
	private int id;
	private int fromnode;
	private int tonode;
	private double length;
	
	public ShortestPathLength() {
		this(0, 0, 0.);
	}

	public ShortestPathLength(int from, int to, double len) {
		// TODO Auto-generated constructor stub
		fromnode = from;
		tonode = to;
		length = len;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double d) {
		this.length = d;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFromnode() {
		return fromnode;
	}

	public void setFromnode(int i) {
		this.fromnode = i;
	}

	public int getTonode() {
		return tonode;
	}

	public void setTonode(int j) {
		this.tonode = j;
	}

}
