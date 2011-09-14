package org.life.sl.orm;

import com.vividsolutions.jts.geom.Point;

public class OSMNode {
	private int id;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private Point geometry;
	
	public Point getGeometry() {
		return geometry;
	}
	
	public void setGeometry(Point geometry) {
		this.geometry = geometry;
	}
	
}
    