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

package org.life.sl.orm;

public class ResultNodeChoice {

	private int id;
	private int routeID;
	private int sourceRouteID;
	private int respondentID;
	private int i;	///> index - sequence in route
	private int edgeID;
	private int nodeID;
	private boolean selected;
	private float dist;
	private float angle;
	private float angleToDest;
	private short envType;
	private short cykType;
	private float groenM;
	private float groenPct;
	private short nPts;
	private short nChoices;

	public ResultNodeChoice() {}

	/**
	 * constructor with route IDs
	 * @param routeID database ID of the corresponding matched route (resultRoute) 
	 * @param sourceRouteID database ID of the corresponding GPS track
	 */
	public ResultNodeChoice(int routeID, int sourceRouteID, int respondentID) {
		this.routeID = routeID;
		this.sourceRouteID = sourceRouteID;
		this.respondentID = respondentID;
	}

	/**
	 * copy constructor
	 * @param rnc the original object to copy from
	 */
	public ResultNodeChoice(ResultNodeChoice rnc) {
		this.id = rnc.id;
		this.routeID = rnc.routeID;
		this.sourceRouteID = rnc.sourceRouteID;
		this.respondentID = rnc.respondentID;
		this.i = rnc.i;
		this.edgeID = rnc.edgeID;
		this.nodeID = rnc.nodeID;
		this.selected = rnc.selected;
		this.dist = rnc.dist;
		this.angle = rnc.angle;
		this.angleToDest = rnc.angleToDest;
		this.envType = rnc.envType;
		this.cykType = rnc.cykType;
		this.groenM = rnc.groenM;
		this.groenPct = rnc.groenPct;
		this.nPts = rnc.nPts;
		this.nChoices = rnc.nChoices;
	}

	public ResultNodeChoice clone() {
		return new ResultNodeChoice(this);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public boolean getSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public float getDist() {
		return dist;
	}
	public void setDist(float dist) {
		this.dist = dist;
	}

	public float getAngle() {
		return angle;
	}
	public void setAngle(float angle) {
		this.angle = angle;
	}

	public int getRouteID() {
		return routeID;
	}
	public void setRouteID(int routeID) {
		this.routeID = routeID;
	}

	public int getSourceRouteID() {
		return sourceRouteID;
	}
	public void setSourceRouteID(int sourceRouteID) {
		this.sourceRouteID = sourceRouteID;
	}

	public int getRespondentID() {
		return respondentID;
	}
	public void setRespondentID(int respondentID) {
		this.respondentID = respondentID;
	}

	public int getEdgeID() {
		return edgeID;
	}
	public void setEdgeID(int edgeID) {
		this.edgeID = edgeID;
	}

	public int getNodeID() {
		return nodeID;
	}
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public float getAngleToDest() {
		return angleToDest;
	}
	public void setAngleToDest(float angleToDest) {
		this.angleToDest = angleToDest;
	}

	public short getEnvType() {
		return envType;
	}
	public void setEnvType(short envType) {
		this.envType = envType;
	}

	public short getCykType() {
		return cykType;
	}
	public void setCykType(short cykType) {
		this.cykType = cykType;
	}

	public float getGroenM() {
		return groenM;
	}
	public void setGroenM(float a) {
		this.groenM = a;
	}
	public float getGroenPct() {
		return groenPct;
	}
	public void setGroenPct(float a) {
		this.groenPct = a;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public short getnPts() {
		return nPts;
	}

	public void setnPts(short nPts) {
		this.nPts = nPts;
	}

	public short getnChoices() {
		return nChoices;
	}

	public void setnChoices(short nChoices) {
		this.nChoices = nChoices;
	}

}
