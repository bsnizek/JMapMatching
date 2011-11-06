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
	private float distPEavg, distPEavg05, distPEavg50, distPEavg95;	///> distance between points and associated edges: average and 5% quantiles
	private int nPoints;		///> number of points in the GPS track
	private long nAlternatives;	///> number of alternatives that were evaluated
	private float maxDistanceFactor;	///< route length <= track length * maxDistanceFactor
	private float matchScore1;
	private float scoreAvg05, scoreAvg10, scoreAvg25, scoreAvg50, scoreAvg100;	///< average match scores of the best 5, 10 etc. routes
	private float matchLength1;
	private float matchLenAvg05, matchLenAvg10, matchLenAvg25, matchLenAvg50, matchLenAvg100;	///< average match lengths of the best 5, 10 etc. routes
	private float noMatchEdges1;
	private float noMatchEdgeAvg05, noMatchEdgeAvg10, noMatchEdgeAvg25, noMatchEdgeAvg50, noMatchEdgeAvg100;	///< average number of matching edges of the best 5, 10 etc. routes
	
	public ResultMetaData(int sourceRouteID, int respondentID) {
		this.sourceRouteID = sourceRouteID;
		this.respondentID = respondentID;
	}
	
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
	public float getDistPEavg05() {
		return distPEavg05;
	}
	public void setDistPEavg05(float distPEavg05) {
		this.distPEavg05 = distPEavg05;
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

	public void setScoreAvgs(double s[]) {
		matchScore1 = (float)s[0];
		if (s.length > 1) scoreAvg05  = (float)s[1];
		if (s.length > 2) scoreAvg10  = (float)s[2];
		if (s.length > 3) scoreAvg25  = (float)s[3];
		if (s.length > 4) scoreAvg50  = (float)s[4];
		if (s.length > 5) scoreAvg100 = (float)s[5];
	}
	public float getMatchScore1() {
		return matchScore1;
	}
	public void setMatchScore1(float matchScore1) {
		this.matchScore1 = matchScore1;
	}
	public float getScoreAvg05() {
		return scoreAvg05;
	}
	public void setScoreAvg05(float s) {
		this.scoreAvg05 = s;
	}
	public float getScoreAvg10() {
		return scoreAvg10;
	}
	public void setScoreAvg10(float s) {
		this.scoreAvg10 = s;
	}
	public float getScoreAvg25() {
		return scoreAvg25;
	}
	public void setScoreAvg25(float s) {
		this.scoreAvg25 = s;
	}
	public float getScoreAvg50() {
		return scoreAvg50;
	}
	public void setScoreAvg50(float s) {
		this.scoreAvg50 = s;
	}
	public float getScoreAvg100() {
		return scoreAvg100;
	}
	public void setScoreAvg100(float s) {
		this.scoreAvg100 = s;
	}

	public void setMatchLengthAvgs(double s[]) {
		matchLength1 = (float)s[0];
		if (s.length > 1) matchLenAvg05  = (float)s[1];
		if (s.length > 2) matchLenAvg10  = (float)s[2];
		if (s.length > 3) matchLenAvg25  = (float)s[3];
		if (s.length > 4) matchLenAvg50  = (float)s[4];
		if (s.length > 5) matchLenAvg100 = (float)s[5];
	}
	public float getMatchLength1() {
		return matchLength1;
	}
	public void setMatchLength1(float s) {
		this.matchLength1 = s;
	}
	public float getMatchLenAvg05() {
		return matchLenAvg05;
	}
	public void setMatchLenAvg05(float s) {
		this.matchLenAvg05 = s;
	}
	public float getMatchLenAvg10() {
		return matchLenAvg10;
	}
	public void setMatchLenAvg10(float s) {
		this.matchLenAvg10 = s;
	}
	public float getMatchLenAvg25() {
		return matchLenAvg25;
	}
	public void setMatchLenAvg25(float s) {
		this.matchLenAvg25 = s;
	}
	public float getMatchLenAvg50() {
		return matchLenAvg50;
	}
	public void setMatchLenAvg50(float s) {
		this.matchLenAvg50 = s;
	}
	public float getMatchLenAvg100() {
		return matchLenAvg100;
	}
	public void setMatchLenAvg100(float s) {
		this.matchLenAvg100 = s;
	}

	public void setNoMatchEdgeAvgs(double s[]) {
		noMatchEdges1 = (float)s[0];
		if (s.length > 1) noMatchEdgeAvg05  = (float)s[1];
		if (s.length > 2) noMatchEdgeAvg10  = (float)s[2];
		if (s.length > 3) noMatchEdgeAvg25  = (float)s[3];
		if (s.length > 4) noMatchEdgeAvg50  = (float)s[4];
		if (s.length > 5) noMatchEdgeAvg100 = (float)s[5];
	}
	public float getNoMatchEdges1() {
		return noMatchEdges1;
	}
	public void setNoMatchEdges1(float s) {
		this.noMatchEdges1 = s;
	}
	public float getNoMatchEdgeAvg05() {
		return noMatchEdgeAvg05;
	}
	public void setNoMatchEdgeAvg05(float s) {
		this.noMatchEdgeAvg05 = s;
	}
	public float getNoMatchEdgeAvg10() {
		return noMatchEdgeAvg10;
	}
	public void setNoMatchEdgeAvg10(float s) {
		this.noMatchEdgeAvg10 = s;
	}
	public float getNoMatchEdgeAvg25() {
		return noMatchEdgeAvg25;
	}
	public void setNoMatchEdgeAvg25(float s) {
		this.noMatchEdgeAvg25 = s;
	}
	public float getNoMatchEdgeAvg50() {
		return noMatchEdgeAvg50;
	}
	public void setNoMatchEdgeAvg50(float s) {
		this.noMatchEdgeAvg50 = s;
	}
	public float getNoMatchEdgeAvg100() {
		return noMatchEdgeAvg100;
	}
	public void setNoMatchEdgeAvg100(float s) {
		this.noMatchEdgeAvg100 = s;
	}
}
