package org.life.sl.orm;

import com.vividsolutions.jts.geom.LineString;

public class OSMEdge {
	
	private int id;
	private LineString geometry;
	private int fromnode;
	private int tonode;
	private double length;
	private String roadname;
	private int highwaytype;
	private int cyclewaytype;
	private int foottype;
	private int bicycletype;
	private int segregatedtype;
	
	

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

	public int getHighwaytype() {
		return highwaytype;
	}

	public void setHighwaytype(int highwaytype) {
		this.highwaytype = highwaytype;
	}

	public int getCyclewaytype() {
		return cyclewaytype;
	}

	public void setCyclewaytype(int cyclewaytype) {
		this.cyclewaytype = cyclewaytype;
	}

	public int getFoottype() {
		return foottype;
	}

	public void setFoottype(int foottype) {
		this.foottype = foottype;
	}

	public int getBicycletype() {
		return bicycletype;
	}

	public void setBicycletype(int bicycletype) {
		this.bicycletype = bicycletype;
	}

	public int getSegregatedtype() {
		return segregatedtype;
	}

	public void setSegregatedtype(int segregatedtype) {
		this.segregatedtype = segregatedtype;
	}

	public LineString getGeometry() {
		return geometry;
	}
	
	public void setGeometry(LineString geometry) {
		this.geometry = geometry;
	}
}
