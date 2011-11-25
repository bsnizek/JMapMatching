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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.life.sl.mapmatching.GPSTrack;
import org.life.sl.routefinder.Label;
import org.life.sl.utils.MathUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Node;

/**
 * ORM for result route data (1 record per route)
 * @author Bernhard Barkow
 *
 */
public class ResultRoute {

	private static double kTurnLimit0 = Math.toRadians(45), kTurnLimit1 = Math.toRadians(135);	///< limits determining when a change in angle is counted as a left/right/front/back turn, in radians
	public final double kCoordEps = 1.e0;		///< tolerance for coordinate comparison (if (x1-x2 < kCoordEps) then x1==x2)
	
	private int id;
	private LineString geometry;
	private boolean selected;
	private float length;
	private float trackLength;
	private float lengthR;
	private int sourceRouteID;
	private int nodeID;

	private int respondentID;
	private long nAlternatives;
	private float pPtsOn;
	private float pPtsOff;
	private int nEdges;
	private int nEdgesWOPts;		///< number of edges on this route not containing any points of the GPS track
	private float matchLengthR;
	private float noMatchLengthR;	///< (relative) length of the route not matched to GPS points
	private float matchScore;

	private short nLeftTurns;
	private short nRightTurns;
	private short nFrontTurns;
	private short nBackTurns;
	private float curviness;
	
	private short nTrafficLights;
	private float[] envAttr;
	private float[] cykAttr;
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
	
	private float[] angle_rel;		///< angle relative to the global x-y coordinate system
	private float[] edgeLengths;
	private int[] nodeIDs;			///< list of OSMNode IDs
	private Label label;			///< the corresponding label from the map matching algorithm
	GPSTrack gpsPoints;

	public ResultRoute(int sourceRouteID, int respondentID, boolean isChoice, Label label, GPSTrack gpsPoints) {
		this.sourceRouteID = sourceRouteID;
		this.respondentID = respondentID;
		this.selected = isChoice;
		this.label = label;
		this.gpsPoints = gpsPoints;
		this.trackLength = (float)gpsPoints.getTrackLength();
		calcData();
	}
	
	public void calcData() {
		int i;
		List<Label> labels = label.getLabels();
		int nNodes = labels.size();
		nEdges = nNodes - 1;
		length = (float)label.getLength();
		lengthR = length / trackLength;
		
		// initialize values:
		int scoreCount = 0;
		double noMatchLength = 0.;
		double angle_tot = 0;
		nEdgesWOPts = 0;
		groenM = 0.f;
		// turns, angles:
		double angle = 0, lastAngle = 0;
		angle_rel = new float[nNodes];
		nLeftTurns = 0;
		nRightTurns = 0;
		nFrontTurns = 0;
		nBackTurns = 0;
		edgeLengths = new float[nNodes];
		// initialize attributes arrays:
		envAttr = new float[9];
		for (i = 0; i < envAttr.length; i++) envAttr[i] = 0;
		cykAttr = new float[5];
		for (i = 0; i < cykAttr.length; i++) cykAttr[i] = 0;
		DirectedEdge backEdge;

		i = 0;
		Label lastlbl = null;
		for (Label lbl : labels) {
			int lastScoreCount = lbl.getLastScoreCount();
			scoreCount += lastScoreCount;
			
			double lel = lbl.getLastEdgeLength();
			edgeLengths[i] = (float)lel;
			if (lastScoreCount == 0) {
				nEdgesWOPts++;
				noMatchLength += lel;
			}
			
			backEdge = lbl.getBackEdge();
			
			if (lastlbl != null) {
				angle = MathUtil.mapAngle_radians(backEdge.getAngle());	// absolute angle of backEdge
				angle_rel[i] = (float)MathUtil.mapAngle_radians(angle - lastAngle);
				double aa = Math.abs(angle_rel[i]);
				angle_tot += aa;

				// is it a turn?
				if (aa > kTurnLimit0) {
					if (aa < kTurnLimit1) {	// [kTurnLimit0, kTurnLimit1]: left/right turn
						if (angle_rel[i] > 0) nLeftTurns++;
						if (angle_rel[i] < 0) nRightTurns++;
					} else  {	// [kTurnLimit1, pi]: backward turn (U-turn)
						nBackTurns++;
					}
				} else nFrontTurns++;	// [0, kTurnLimit0]: forward turn (straight on)
	
				@SuppressWarnings("unchecked")
				HashMap<String, Object> userdata = (HashMap<String, Object>) backEdge.getEdge().getData();	// the user data object of the Edge
				envAttr[(Short)userdata.get("et")] += lel;
				cykAttr[(Short)userdata.get("ct")] += lel;
				groenM += ((Double)userdata.get("gm")).floatValue();
			} else {
				angle_rel[i] = 0.f;
				edgeLengths[i] = 0.f;
			}
			
			lastlbl = lbl;
			lastAngle = angle;
			i++;
		}
		matchScore = scoreCount / length;
		noMatchLengthR = (float)(noMatchLength / trackLength);
		matchLengthR = 1.f - noMatchLengthR;
		curviness = (float)(angle_tot / length * 1000.);	// angle / km
		
		// make environmental parameters relative:
		for (i = 0; i < envAttr.length; i++) envAttr[i] /= length;
		for (i = 0; i < cykAttr.length; i++) cykAttr[i] /= length;
		transferEnvParams();
		groenM /= length;

		pPtsOn = (float)( (double)scoreCount / (double)gpsPoints.size() );	// fraction of points on edges 
		pPtsOff = (1.f - pPtsOn);
		getNumberOfTrafficLights();
	}
	
	public void transferEnvParams() {
		setEnvAttr00(envAttr[0]);
		setEnvAttr01(envAttr[1]);
		setEnvAttr02(envAttr[2]);
		setEnvAttr03(envAttr[3]);
		setEnvAttr04(envAttr[4]);
		setEnvAttr05(envAttr[5]);
		setEnvAttr06(envAttr[6]);
		setEnvAttr07(envAttr[7]);
		setEnvAttr08(envAttr[8]);

		setCykAttr00(cykAttr[0]);
		setCykAttr01(cykAttr[1]);
		setCykAttr02(cykAttr[2]);
		setCykAttr03(cykAttr[3]);
		setCykAttr04(cykAttr[4]);
	}

	public int getNumberOfTrafficLights() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<Node> nodes = label.getNodes();
		nodeIDs = new int[nodes.size()];
		List<DirectedEdge> edges = label.getRouteAsEdges();
		int n = 0;
		for (DirectedEdge e : edges) {		// for each node along the route:
			@SuppressWarnings("unchecked")
			HashMap<String, Object> ed = (HashMap<String, Object>) e.getEdge().getData();
			Integer edgeID = (Integer)ed.get("id");

			Node node = e.getFromNode();	// node at beginning of edge
			Coordinate c_n = node.getCoordinate();

			// get node ID from database:
			int nodeID = 0;
			String s = " from OSMEdge where id=" + edgeID;
			//s = "from OSMNode where id in ( (select fromnode"+s+"), (select tonode"+s+") )";	// this sometimes yields only 1 record instead of 2!?!
			s = "from OSMNode where (id = (select fromnode"+s+") or id = (select tonode"+s+"))";
			Query nodeRes = session.createQuery(s);
			// TODO: make this more efficient using a PostGIS spatial query with indexing?
			// match coordinates:
			@SuppressWarnings("unchecked")
			Iterator<OSMNode> it = nodeRes.iterate();
			while (it.hasNext()) {
				OSMNode on = it.next();
				Coordinate onc = on.getGeometry().getCoordinate();
				if (Math.abs(c_n.x - onc.x) < kCoordEps && Math.abs(c_n.y - onc.y) < kCoordEps) {
					nodeID = on.getId();
					break;
				}								
			}	// now, nodeID is either 0 or the database ID of the corresponding node
			nodeIDs[n++] = nodeID;
		}
		
		String s1 = String.valueOf(nodeIDs[0]);
		for (int i = 0; i < n; i++) {
			s1 += "," + nodeIDs[i];
		}
		String s = "select count(\"nodeid\") from trafficlight where \"nodeid\" in ("+s1+")";
		Query res = session.createSQLQuery(s);
		BigInteger ntl = (BigInteger)res.uniqueResult();
		this.nTrafficLights = ntl.shortValue();
		return (ntl == null ? 0 : this.nTrafficLights);
	}

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
	
	public float[] getEdgeLengths() {
		return edgeLengths;
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

	public float getLengthR() {
		return lengthR;
	}

	public void setLengthR(float lengthR) {
		this.lengthR = lengthR;
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

	public float getCurviness() {
		return curviness;
	}

	public void setCurviness(float curviness) {
		this.curviness = curviness;
	}

	public short getnTrafficLights() {
		return nTrafficLights;
	}

	public void setnTrafficLights(short nTrafficLights) {
		this.nTrafficLights = nTrafficLights;
	}
	
	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}
	
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
