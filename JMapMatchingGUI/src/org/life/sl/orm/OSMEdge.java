package org.life.sl.orm;

import com.vividsolutions.jts.geom.LineString;

public class OSMEdge {
	
	private int id;
	private LineString geometry;
	private int fromnode;
	private int tonode;
	private double length;
	private String roadname;
	private String highway;
	private String cycleway;
	private String foot;
	private String bicycle;
	private String segregated;
	
	
	public String getCycleway() {
		return cycleway;
	}

	public void setCycleway(String cycleway) {
		this.cycleway = cycleway;
	}

	public String getFoot() {
		return foot;
	}

	public void setFoot(String foot) {
		this.foot = foot;
	}

	public String getBicycle() {
		return bicycle;
	}

	public void setBicycle(String bicycle) {
		this.bicycle = bicycle;
	}

	public String getSegregated() {
		return segregated;
	}

	public void setSegregated(String segregated) {
		this.segregated = segregated;
	}

	public String getHighway() {
		return highway;
	}

	public void setHighway(String highway) {
		this.highway = highway;
	}

	public String getRoadname() {
		return roadname;
	}

	public void setRoadname(String roadname) {
		this.roadname = roadname;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
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
