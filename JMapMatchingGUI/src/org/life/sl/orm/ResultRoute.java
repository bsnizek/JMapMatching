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
 * ORM for result route data (1 record per route)
 * @author Bernhard Barkow
 *
 */
public class ResultRoute {
	
	private int id;
	private LineString geometry;
	private boolean selected;
	private float length;
	private float trackLengthR;
	private int sourceRouteID;
	private int respondentID;
	private long nAlternatives;
	private float pPtsOn;
	private float pPtsOff;
	private int nEdges;
	private int nEdgesWOPts;
	private float matchLengthR;
	private float noMatchLengthR;
	private float matchScore;

	private short nLeftTurns;
	private short nRightTurns;
	private short nFrontTurns;
	private short nBackTurns;
	private float straightness;
	
	private short nTrafficLights;
	//private float[] envAttr;
	//private float[] cykAttr;
	private float envAttr00;
	private float envAttr01;
	private float envAttr02;
	private float envAttr03;
	private float envAttr04;
	private float envAttr05;
	private float envAttr06;
	private float envAttr07;
	private float envAttr08;
	private float cykAttr00;
	private float cykAttr01;
	private float cykAttr02;
	private float cykAttr03;
	private float cykAttr04;

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

	public float getpPtsOn() {
		return pPtsOn;
	}

	public void setpPtsOn(float pPtsOn) {
		this.pPtsOn = pPtsOn;
	}

	public float getpPtsOff() {
		return pPtsOff;
	}

	public void setpPtsOff(float pPtsOff) {
		this.pPtsOff = pPtsOff;
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

	public float getNoMatchLengthR() {
		return noMatchLengthR;
	}

	public void setNoMatchLengthR(double noMatchLengthR) {	// using a double argument!
		this.noMatchLengthR = (float)noMatchLengthR;
	}

	public float getMatchScore() {
		return matchScore;
	}

	public void setMatchScore(double matchScore) {
		this.matchScore = (float)matchScore;
	}

	public float getMatchLengthR() {
		return matchLengthR;
	}

	public void setMatchLengthR(double matchLengthR) {
		this.matchLengthR = (float)matchLengthR;
	}

	public float getTrackLengthR() {
		return trackLengthR;
	}

	public void setTrackLengthR(float trackLengthFracR) {
		this.trackLengthR = trackLengthFracR;
	}

	public void setGroenM(double groenM) {
		this.groenM = (float)groenM;
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

	public short getnFrontTurns() {
		return nFrontTurns;
	}

	public void setnFrontTurns(short nFrontTurns) {
		this.nFrontTurns = nFrontTurns;
	}

	public short getnBackTurns() {
		return nBackTurns;
	}

	public void setnBackTurns(short nBackTurns) {
		this.nBackTurns = nBackTurns;
	}

	public float getStraightness() {
		return straightness;
	}

	public void setStraightness(float straightness) {
		this.straightness = straightness;
	}

	public short getnTrafficLights() {
		return nTrafficLights;
	}

	public void setnTrafficLights(short nTrafficLights) {
		this.nTrafficLights = nTrafficLights;
	}

	/*public float[] getEnvAttr() {
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
	}*/
	
	// this is extremely inelegant, but I don't know a better solution...
	public float getEnvAttr00() { return envAttr00; }
	public void setEnvAttr00(float envAttr) { this.envAttr00 = envAttr; }
	public void setEnvAttr00(double envAttr) { this.envAttr00 = (float)envAttr; }
	public float getEnvAttr01() { return envAttr01; }
	public void setEnvAttr01(float envAttr) { this.envAttr01 = envAttr; }
	public void setEnvAttr01(double envAttr) { this.envAttr01 = (float)envAttr; }
	public float getEnvAttr02() { return envAttr02; }
	public void setEnvAttr02(float envAttr) { this.envAttr02 = envAttr; }
	public void setEnvAttr02(double envAttr) { this.envAttr02 = (float)envAttr; }
	public float getEnvAttr03() { return envAttr03; }
	public void setEnvAttr03(float envAttr) { this.envAttr03 = envAttr; }
	public void setEnvAttr03(double envAttr) { this.envAttr03 = (float)envAttr; }
	public float getEnvAttr04() { return envAttr04; }
	public void setEnvAttr04(float envAttr) { this.envAttr04 = envAttr; }
	public void setEnvAttr04(double envAttr) { this.envAttr04 = (float)envAttr; }
	public float getEnvAttr05() { return envAttr05; }
	public void setEnvAttr05(float envAttr) { this.envAttr05 = envAttr; }
	public void setEnvAttr05(double envAttr) { this.envAttr05 = (float)envAttr; }
	public float getEnvAttr06() { return envAttr06; }
	public void setEnvAttr06(float envAttr) { this.envAttr06 = envAttr; }
	public void setEnvAttr06(double envAttr) { this.envAttr06 = (float)envAttr; }
	public float getEnvAttr07() { return envAttr07; }
	public void setEnvAttr07(float envAttr) { this.envAttr07 = envAttr; }
	public void setEnvAttr07(double envAttr) { this.envAttr07 = (float)envAttr; }
	public float getEnvAttr08() { return envAttr08; }
	public void setEnvAttr08(float envAttr) { this.envAttr08 = envAttr; }
	public void setEnvAttr08(double envAttr) { this.envAttr08 = (float)envAttr; }
	
	public float getCykAttr00() { return cykAttr00; }
	public void setCykAttr00(float cykAttr) { this.cykAttr00 = cykAttr; }
	public void setCykAttr00(double cykAttr) { this.cykAttr00 = (float)cykAttr; }
	public float getCykAttr01() { return cykAttr01; }
	public void setCykAttr01(float cykAttr) { this.cykAttr01 = cykAttr; }
	public void setCykAttr01(double cykAttr) { this.cykAttr01 = (float)cykAttr; }
	public float getCykAttr02() { return cykAttr02; }
	public void setCykAttr02(float cykAttr) { this.cykAttr02 = cykAttr; }
	public void setCykAttr02(double cykAttr) { this.cykAttr02 = (float)cykAttr; }
	public float getCykAttr03() { return cykAttr03; }
	public void setCykAttr03(float cykAttr) { this.cykAttr03 = cykAttr; }
	public void setCykAttr03(double cykAttr) { this.cykAttr03 = (float)cykAttr; }
	public float getCykAttr04() { return cykAttr04; }
	public void setCykAttr04(float cykAttr) { this.cykAttr04 = cykAttr; }
	public void setCykAttr04(double cykAttr) { this.cykAttr04 = (float)cykAttr; }

	public float getGroenM() {
		return groenM;
	}
}
