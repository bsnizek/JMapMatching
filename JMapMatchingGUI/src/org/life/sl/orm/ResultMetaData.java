package org.life.sl.orm;

public class ResultMetaData {

	private int id;
	private int sourceRouteID;	///> database ID of source route (GPS Track)
	private int respondentID;	///> ID of the respondent
	private float trackLength;	///> length of GPS track (sum of Euclidian distances between points)
	private float avgDistPt;	///> statistics: average distance between GPS points
	private float minDistPt;	///> minimum distance between GPS points
	private float maxDistPt;	///> maximum distance between GPS points (longest gap)
	private int nPoints;		///> number of points in the GPS track
	private long nAlternatives;	///> number of alternatives that were evaluated
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public float getTrackLength() {
		return trackLength;
	}
	public void setTrackLength(float trackLength) {
		this.trackLength = trackLength;
	}
	public long getnAlternatives() {
		return nAlternatives;
	}
	public void setnAlternatives(long nAlternatives) {
		this.nAlternatives = nAlternatives;
	}
	public float getAvgDistPt() {
		return avgDistPt;
	}
	public void setAvgDistPt(float avgDistPt) {
		this.avgDistPt = avgDistPt;
	}
	public float getMinDistPt() {
		return minDistPt;
	}
	public void setMinDistPt(float minDistPt) {
		this.minDistPt = minDistPt;
	}
	public float getMaxDistPt() {
		return maxDistPt;
	}
	public void setMaxDistPt(float maxDistPt) {
		this.maxDistPt = maxDistPt;
	}
	public int getnPoints() {
		return nPoints;
	}
	public void setnPoints(int nPoints) {
		this.nPoints = nPoints;
	}
}
