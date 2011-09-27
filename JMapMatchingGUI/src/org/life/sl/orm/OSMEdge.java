package org.life.sl.orm;

import com.vividsolutions.jts.geom.LineString;

public class OSMEdge {
	
	private int id;
	private LineString geometry;
	private int fromnode;
	private int tonode;
	private double length;
	private String roadname;
	private Short highwaytype;
	private Short cyclewaytype;
	private Short foottype;
	private Short bicycletype;
	private Short segregatedtype;
	
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFromnode() {
		return fromnode;
	}

	public void setFromnode(int fromnode) {
		this.fromnode = fromnode;
	}

	public int getTonode() {
		return tonode;
	}

	public void setTonode(int tonode) {
		this.tonode = tonode;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public String getRoadname() {
		return roadname;
	}

	public void setRoadname(String roadname) {
		this.roadname = roadname;
	}

	public Short getHighwaytype() {
		return highwaytype;
	}

	public void setHighwaytype(Short highwaytype) {
		this.highwaytype = highwaytype;
	}

	public Short getCyclewaytype() {
		return cyclewaytype;
	}

	public void setCyclewaytype(Short cyclewaytype) {
		this.cyclewaytype = cyclewaytype;
	}

	public Short getFoottype() {
		return foottype;
	}

	public void setFoottype(Short foottype) {
		this.foottype = foottype;
	}

	public Short getBicycletype() {
		return bicycletype;
	}

	public void setBicycletype(Short bicycletype) {
		this.bicycletype = bicycletype;
	}

	public Short getSegregatedtype() {
		return segregatedtype;
	}

	public void setSegregatedtype(Short segregatedtype) {
		this.segregatedtype = segregatedtype;
	}

	public LineString getGeometry() {
		return geometry;
	}
	
	public void setGeometry(LineString geometry) {
		this.geometry = geometry;
	}
}
