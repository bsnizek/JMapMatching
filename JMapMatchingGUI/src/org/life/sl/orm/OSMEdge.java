package org.life.sl.orm;

import com.vividsolutions.jts.geom.LineString;

public class OSMEdge {

	private int id;
	private LineString geometry;
	private int fromnode;
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
	public Short getEnvtype() {
		return envtype;
	}
	public void setEnvtype(Short envtype) {
		this.envtype = envtype;
	}
	public Short getCyktype() {
		return cyktype;
	}
	public void setCyktype(Short cyktype) {
		this.cyktype = cyktype;
	}
	public double getGroenpct() {
		return groenpct;
	}
	public void setGroenpct(double groenpct) {
		this.groenpct = groenpct;
	}
	public double getGroenm() {
		return groenm;
	}
	public void setGroenm(double groenm) {
		this.groenm = groenm;
	}
	private int tonode;
	private double length;
	private String roadname;
	private Short highwaytype;
	private Short cyclewaytype;
	private Short foottype;
	private Short bicycletype;
	private Short segregatedtype;

	// HSP specific attributes

	private Short envtype;
	private Short cyktype;
	private double groenpct;
	private double groenm;


}
