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

import com.vividsolutions.jts.geom.LineString;

public class ResultRoute {
	
	private int id;
	private LineString geometry;
	private boolean selected;
	private float length;
	private int sourcerouteid;
	
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public int getSourcerouteid() {
		return sourcerouteid;
	}

	public void setSourcerouteid(int sourcerouteid) {
		this.sourcerouteid = sourcerouteid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
	public LineString getGeometry() {
		return geometry;
	}
	
	public void setGeometry(LineString geometry) {
		this.geometry = geometry;
	}
}
