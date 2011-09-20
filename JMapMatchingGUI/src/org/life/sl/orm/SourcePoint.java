package org.life.sl.orm;

import com.vividsolutions.jts.geom.Point;

public class SourcePoint {
	
	private int id;
	private Point geometry;
	private int sourcerouteid;
	/*private int ts; // timestamp ? 
	
	public int getTs() {
		return ts;
	}

	public void setTs(int ts) {
		this.ts = ts;
	}*/

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
    