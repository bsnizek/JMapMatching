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

import java.util.Date;

import com.vividsolutions.jts.geom.Point;

public class SourcePoint {
	
	private int id;
	private Point geometry;
	private int sourcerouteid;
	private Date t;
	/*private int ts; // timestamp ? 
	
	public int getTs() {
		return ts;
	}

	public void setTs(int ts) {
		this.ts = ts;
	}*/

	public Date getT() {
		return t;
	}

	public void setT(Date t) {
		this.t = t;
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

	
	public Point getGeometry() {
		return geometry;
	}
	
	public void setGeometry(Point geometry) {
		this.geometry = geometry;
	}
	
}
    