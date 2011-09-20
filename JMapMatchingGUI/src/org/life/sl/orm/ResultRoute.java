package org.life.sl.orm;

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
