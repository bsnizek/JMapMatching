package org.life.sl.orm;

/**
 * ORM for track metadata (1 record per track)
 * @author Bernhard Barkow
 *
 */
public class ResultMetaData {

	private int id;
	private int sourceRouteID;	///> database ID of source route (GPS Track)
	private int respondentID;	///> ID of the respondent
	private float trackLength;	///> length of GPS track (sum of Euclidian distances between points)
	private float avgDistPt;	///> statistics: average distance between GPS points
	private float minDistPt;	///> minimum distance between GPS points
	private float maxDistPt;	///> maximum distance between GPS points (longest gap)
	private float distPEavg, distPEavg5, distPEavg50, distPEavg95;	///> distance between points and associated edges: average and 5% quantiles
	private int nPoints;		///> number of points in the GPS track
	private long nAlternatives;	///> number of alternatives that were evaluated
	private float maxDistanceFactor;	///< route length <= track length * maxDistanceFactor
	
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
	
	public float getMaxDistanceFactor() {
		return maxDistanceFactor;
	}
	public void setMaxDistanceFactor(float maxDistanceFactor) {
		this.maxDistanceFactor = maxDistanceFactor;
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
	public float getDistPEavg50() {
		return distPEavg50;
	}
	public void setDistPEavg50(float distPEavg50) {
		this.distPEavg50 = distPEavg50;
	}
	public float getDistPEavg95() {
		return distPEavg95;
	}
	public void setDistPEavg95(float distPEavg95) {
		this.distPEavg95 = distPEavg95;
	}

	public int getnPoints() {
		return nPoints;
	}
	public void setnPoints(int nPoints) {
		this.nPoints = nPoints;
	}
}
