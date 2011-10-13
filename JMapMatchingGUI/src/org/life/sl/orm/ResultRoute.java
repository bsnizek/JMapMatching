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

public class ResultRoute {
	
	private int id;
	private LineString geometry;
	private boolean selected;
	private float length;
	private int sourcerouteid;
	private long nAlternatives;
	private int nPtsOn;
	private int nPtsOff;
	private int nEdges;
	private int nEdgesWOPts;
	private float distPEavg, distPEavg5, distPEavg95;
	private float matchScore;
	private float matchFrac;
	private float trackLengthFrac;
	private short nLeftTurns;
	private short nRightTurns;
	private short nTrafficLights;
	private float[] envAttr;
	private float[] cykAttr;
	private float groenM;
	
	
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

	public long getnAlternatives() {
		return nAlternatives;
	}

	public void setnAlternatives(long nAlternatives) {
		this.nAlternatives = nAlternatives;
	}

	public int getnPtsOn() {
		return nPtsOn;
	}

	public void setnPtsOn(int nPtsOn) {
		this.nPtsOn = nPtsOn;
	}

	public int getnPtsOff() {
		return nPtsOff;
	}

	public void setnPtsOff(int nPtsOff) {
		this.nPtsOff = nPtsOff;
	}

	public int getnEdges() {
		return nEdges;
	}

	public void setnEdges(int nEdges) {
		this.nEdges = nEdges;
	}

	public int getnEdgesWOPts() {
		return nEdgesWOPts;
	}

	public void setnEdgesWOPts(int nEdgesWOPts) {
		this.nEdgesWOPts = nEdgesWOPts;
	}

	public float getDistPEavg() {
		return distPEavg;
	}

	public void setDistPEavg(float distPEavg) {
		this.distPEavg = distPEavg;
	}

	public float getDistPEavg5() {
		return distPEavg5;
	}

	public void setDistPEavg5(float distPEavg5) {
		this.distPEavg5 = distPEavg5;
	}

	public float getDistPEavg95() {
		return distPEavg95;
	}

	public void setDistPEavg95(float distPEavg95) {
		this.distPEavg95 = distPEavg95;
	}

	public float getMatchScore() {
		return matchScore;
	}

	public void setMatchScore(float matchScore) {
		this.matchScore = matchScore;
	}

	public float getMatchFrac() {
		return matchFrac;
	}

	public void setMatchFrac(float matchFrac) {
		this.matchFrac = matchFrac;
	}

	public float getTrackLengthFrac() {
		return trackLengthFrac;
	}

	public void setTrackLengthFrac(float trackLengthFrac) {
		this.trackLengthFrac = trackLengthFrac;
	}

	public short getnLeftTurns() {
		return nLeftTurns;
	}

	public void setnLeftTurns(short nLeftTurns) {
		this.nLeftTurns = nLeftTurns;
	}

	public short getnRightTurns() {
		return nRightTurns;
	}

	public void setnRightTurns(short nRightTurns) {
		this.nRightTurns = nRightTurns;
	}

	public short getnTrafficLights() {
		return nTrafficLights;
	}

	public void setnTrafficLights(short nTrafficLights) {
		this.nTrafficLights = nTrafficLights;
	}

	public float[] getEnvAttr() {
		return envAttr;
	}

	public void setEnvAttr(float[] envAttr) {
		this.envAttr = envAttr;
	}

	public float[] getCykAttr() {
		return cykAttr;
	}

	public void setCykAttr(float[] cykAttr) {
		this.cykAttr = cykAttr;
	}

	public float getGroenM() {
		return groenM;
	}

	public void setGroenM(float groenM) {
		this.groenM = groenM;
	}
}
