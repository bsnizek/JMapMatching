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
/**
 * ORM class for Edges created from OpenStreetMap data
 * @see https://github.com/bsnizek/JMapMatching/wiki/LookupTableCodes
 */
public class OSMEdge {

	private int id;
	private LineString geometry;	///< edge geometry data (line string)
	private int fromnode;			///< start node ID (start node of this edge)
	private int tonode;				///< end node ID
	private float length;			///< edge length [m]
	private String roadname;		///< road name associated with this edge 

	// edge type attributes:
	private Short highwaytype;		///< highway type parameter - @see highwaytype table
	private Short cyclewaytype;		///< cycleway type parameter - @see cyclewaytype table
	private Short foottype;			///< footpath type parameter - @see highwaytype table
	private Short bicycletype;		///< bicycle type parameter - @see bicycletype table
	private Short segregatedtype;	///< if road is segregated or not - @see segregatedtype table

	// HSP specific attributes:
	private Short envtype;	///< type of environment surrounding the edge - @see envType table
	private Short cyktype;	///< type of bicycle facility - @see cykType table
	private float groenpct;	///< percentage of the edge running through a green environment; GroenM = shape_length * GroenPct
	private float groenm;	///< length [m] of the edge running thorough a green environment; GroenM = shape_length * GroenPct

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
	public float getLength() {
		return length;
	}
	public void setLength(float length) {
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
	public float getGroenpct() {
		return groenpct;
	}
	public void setGroenpct(float groenpct) {
		this.groenpct = groenpct;
	}
	public float getGroenm() {
		return groenm;
	}
	public void setGroenm(float groenm) {
		this.groenm = groenm;
	}
}
