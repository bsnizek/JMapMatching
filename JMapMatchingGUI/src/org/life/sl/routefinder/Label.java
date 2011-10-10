package org.life.sl.routefinder;

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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.life.sl.mapmatching.EdgeStatistics;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.Node;
/**
 * This class is used by the RouteGenerator to search for routes, and can also be used
 * to represent a route. Normally you would use a List of edges for that.
 * @author Pimin Konstantin Kefaloukos
 *
 */
public class Label implements Comparable<Label> {

	private static double kTurnLimit = 1.;	///> bottom limit determining when a change in angle is counted as a left/right turn, in radians
	
	/**
	 * Comparator comparing only the last edge of two labels
	 * @author bb
	 *
	 */
	public static class LastEdgeComparator implements Comparator<Label> {
		public int compare(Label arg0, Label arg1) {
			// we could add a test if the labels are in the same tree level?
			return arg0.compareTo_LE(arg1);
		}	
	}
	
	private Label parent;			///> The parent of the Label
	private Node node;				///> The node associated with this Label
	private DirectedEdge backEdge;	///> The GeoEdge leading back to the node associated with the parent Label
	private int treeLevel = 0;		///> counter for the Label's level (depth) in the search tree
	private double score = -1.;		///> the score of the label (evaluated according to edge statistics)
	private int scoreCount = -1;	///> the unweighted score of the label (nearest points-count)
	private double lastScore = 0.;	///> the same but considering only the last edge	
	private int lastScoreCount = 0;
	private int nEdgesWOPoints = 0;	///> number of edges on this route not containing any points of the GPS track
	private double noMatchLength = 0;	///> (absolute) length of the route not matched to GPS points
	
	private double length = 0.;		///> if the label represents a route, this is the length of the route (sum of all backEdges)
	private double lastEdgeLength = 0.;	///> length of last backEdge
	private double angle = 0.;		///> angle relative to the global x-y coordinate system
	private double angle_rel = 0.;	///> angle change relative to previous edge
	private double angle_tot = 0.;	///> sum of all angle changes
	private int nLeftTurns = 0, nRightTurns = 0;
	
	/**
	 * Create a new Label as descendant of a parent label
	 * @param parent The Label that this Label was expanded from
	 * @param node The node associated with this Label.
	 * @param backEdge The edge leading back to the parent of the Label.
	 * @param length The new accumulative length of the route represented by this Label.
	 */
	public Label(Label parent, Node node, DirectedEdge backEdge, double length, double lastEdgeLength) {
		//		System.out.println("Label(+) " + node.getCoordinate());
		this.parent = parent;
		this.node = node;
		this.backEdge = backEdge;
		this.lastEdgeLength = lastEdgeLength;
		this.length = length;
		this.treeLevel = parent.getTreeLevel() + 1;
		
		if (parent != null) {
			angle = backEdge.getAngle();	// absolute angle of backEdge
			angle_rel = angle - parent.getAngle();
			angle_tot = parent.getTotalAngle() + Math.abs(angle_rel);
			// is it a turn?
			boolean bTurn = (Math.abs(angle_rel) > kTurnLimit); 
			nLeftTurns = parent.getLeftTurns() + (bTurn && angle_rel > 0 ? 1 : 0);
			nRightTurns = parent.getLeftTurns() + (bTurn && angle_rel < 0 ? 1 : 0);
		}
	}

	/**
	 * Create a new "empty" Label at a node;
	 * parent and backEdge are set to null, length is set to 0.0 and expand is set to true.
	 * @param node The node associated with this Label.
	 */
	public Label(Node node) {
		//		System.out.println("Label(-) " + node.getCoordinate());
		this.parent = null;
		this.node = node;
		this.backEdge = null;
		this.length = 0.;
		this.lastEdgeLength = 0.;
	}

	/**
	 * string representation
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return node.toString();
	}

	/**
	 * comparison method
	 * @param arg0 the other object to compare this to
	 * @return 1 if this object is larger than the other, -1 if smaller, 0 if equal
	 */
	public int compareTo(Label arg0) {
		int r = 0;
		double ov = arg0.getScore();
		if (this.score > ov) r = 1;
		else if (this.score < ov) r = -1;
//		int ov = arg0.getScoreCount();
//		if (this.scoreCount > ov) r = 1;
//		else if (this.scoreCount < ov) r = -1;
		return r;
	}
	/**
	 * comparison method using only the last edge
	 * @param arg0 the other object to compare this to
	 * @return 1 if this object is larger than the other, -1 if smaller, 0 if equal
	 */
	public int compareTo_LE(Label arg0) {
		int r = 0;
		double ov = arg0.getLastScore();
		if (this.lastScore > ov) r = 1;
		else if (this.lastScore < ov) r = -1;
		else r = compareTo(arg0);	// if both are equal, sort them according to their total score
		return r;
	}

	// The following are getter methods:
	
	/**
	 * @return Returns the parent of the Label
	 */
	public Label getParent() {
		return this.parent;
	}

	/**
	 * @return Returns the GeoNode associated with this label. GeoNodes can be associated with any number
	 * of Labels, but Labels can only be associated with one GeoNode.
	 */
	public Node getNode() {
		return this.node;
	}

	/**
	 * @return Returns the edge leading back to the parent of this Label.
	 */
	public DirectedEdge getBackEdge() {
		return this.backEdge;
	}

	/**
	 * @return Returns the length of the route represented by this Label.
	 */
	public double getLength() {
		return this.length;
	}
	
	/**
	 * @return the Label's level (depth) in the search tree
	 */
	public int getTreeLevel() { return treeLevel; }
	
	/**
	 * @return the angle change relative to the previous edge
	 */
	public double getAngle() { return angle; }
	/**
	 * @return the total angle changes at all nodes along the route
	 */
	public double getTotalAngle() { return angle_tot; }
	/**
	 * @return total angle divided by length, should be a measurement of straightness
	 */
	public double straightness() {
		return angle_tot / length;
	}

	/**
	 * @return number of left turns performed along the route so far
	 */
	public int getLeftTurns() { return nLeftTurns; }
	/**
	 * @return number of right turns performed along the route so far
	 */
	public int getRightTurns() { return nRightTurns; }

	/**
	 * calculate the weighted and unweighted score of this label from the edge statistics;
	 * 	this is a measure of the quality of the route: number of fitting data points, normalized to route length
	 *  (the recursive variant is about 3 times faster than doing it explicitely for all edges)
	 * @param eStat edge statistics
	 */
	public void calcScore(EdgeStatistics eStat) {
// the old version, calculating the score explicitely
//		double s = 0;
//		List<DirectedEdge> edges = this.getRouteAsEdges();	
//		for (DirectedEdge e : edges) {
//			s += eStat.getCount(e.getEdge());
//		}
//		score = s/this.getLength();
// the new version, doing it recursively:
		score = 0.;
		scoreCount = 0;
		if (parent != null) {
			//System.out.println((int)(parent.getScore(eStat) * parent.getLength()) + " - " + eStat.getCount(backEdge.getEdge()));
			lastScoreCount = eStat.getCount(backEdge.getEdge());
			lastScore = lastScoreCount / lastEdgeLength;
			scoreCount = parent.getScoreCount(eStat) + lastScoreCount;
			//score = Math.round(parent.getScore(eStat) * parent.getLength()) + eStat.getCount(backEdge.getEdge());	// backEdge should be the last edge in the label
			if (length > 0.) score = scoreCount / length;
			
			nEdgesWOPoints = parent.getnEdgesWOPoints();
			noMatchLength = parent.getNoMatchLength();
			if (lastScoreCount == 0) {
				nEdgesWOPoints++;
				noMatchLength += lastEdgeLength;
			}
		}
	}
	
	/**
	 * @return the score of this label, freshly calculated from the edge statistics, if necessary
	 * @param eStat edge statistics
	 */
	public double getScore(EdgeStatistics eStat) {
		if (score < 0.) calcScore(eStat);
		return score;
	}
	/**
	 * @return the score of this label (getter method)
	 */
	public double getScore() {
		return score;
	}
	/**
	 * @return the unweighted score of this label (nearest points-count), freshly calculated from the edge statistics, if necessary
	 * @param eStat edge statistics
	 */
	public int getScoreCount(EdgeStatistics eStat) {
		if (scoreCount < 0) calcScore(eStat);
		return scoreCount;
	}
	public int getScoreCount() {
		return scoreCount;
	}
	public double getLastScore() {
		return lastScore;
	}
	public int getLastScoreCount() {
		return lastScoreCount;
	}

	public int getnEdgesWOPoints() {
		return nEdgesWOPoints;
	}

	public double getNoMatchLength() {
		return noMatchLength;
	}

	/**
	 * Given a directed edge, this method calculates how many times the undirected parent edge has been visited by the route
	 * represented by this Label.
	 * @param dirEdge The directed edge we are querying about.
	 * @return An int value indicating the degree of overlap for the edge. Counting occurrences in both directions.
	 */
	public int getOccurrencesOfEdge(DirectedEdge dirEdge) {
		Label label = this;
		int n = 0;
		while (label.getBackEdge() != null) {
			Edge undirectedBackEdge = label.getBackEdge().getEdge();
			if (dirEdge.getEdge() == undirectedBackEdge) n++;	// important: compare the undirected edges!
			label = label.getParent();
		}
		return n;
	}

	/**
	 * Given a node, this method calculates how many times that node has been visited by the route
	 * represented by this Label.
	 * @param node The node we are querying about.
	 * @return An int value indicating the degree of overlap for the node.
	 */
	public int getOccurrencesOfNode(Node node) {
		Label label = this;
		int n = 0;
		while (label != null) {
			if (node == label.getNode()) n++;
			label = label.getParent();
		}
		return n;
	}

	/**
	 * @return Get the route represented by this label as a list of directed edges.
	 */
	public List<DirectedEdge> getRouteAsEdges() {
		ArrayList<DirectedEdge> results = new ArrayList<DirectedEdge>();
		Label label = this;
		while(label.getParent() != null) {
			results.add(label.getBackEdge());
			label = label.getParent();
		}
		Collections.reverse(results);	// now, the topmost label represents the first edge in the list
		return results;
	}

	/**
	 * @return a list of all the Nodes of this label starting with the origin 
	 */
	public List<Node> getNodes() {
		ArrayList<Node> results = new ArrayList<Node>();
		Label label = this;
		while(label.getParent() != null) {
			results.add(label.getNode());
			label = label.getParent();
		}
		Collections.reverse(results);	// now, the origin node is first in the list
		return results;
	}

	/**
	 * export the label data to a shape file
	 * @param filename the name of the shape file
	 * @throws SchemaException
	 * @throws IOException
	 */
	public void dumpToShapeFile(String filename) throws SchemaException, IOException {

		final SimpleFeatureType TYPE = DataUtilities.createType("route",
				"location:LineString:srid=4326," + // <- the geometry attribute: Polyline type
						"name:String," + // <- a String attribute
						"number:Integer" // a number attribute
				);

		// 1. build a feature
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		SimpleFeatureCollection collection = FeatureCollections.newCollection();
		for (DirectedEdge l : this.getRouteAsEdges()) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> data = (HashMap<String, Object>) l.getEdge().getData();
			LineString ls = (LineString) data.get("geometry");
			SimpleFeature feature = featureBuilder.buildFeature(null);	
			feature.setDefaultGeometry(ls);
			collection.add(feature);
		}
		
		// 2. write to a shapefile
        System.out.println("Writing to shapefile " + filename);
		File newFile = new File(filename);
		// File newFile = getNewShapeFile(file);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(TYPE);

        // You can comment out this line if you are using the createFeatureType method (at end of
        // class file) rather than DataUtilities.createType
        newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
		
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        
        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
            // System.exit(0); // success!
        } else {
            System.out.println(typeName + " does not support read/write access");
            System.exit(1);	// exit program with status 1 (error)
        }
    }
		
	/**
	 * print the edge data of the current route to the console (mostly  for debugging purposes)
	 */
	public void printRoute() {
		List<DirectedEdge> edges = this.getRouteAsEdges();
		String resultString = "** ";
		for (DirectedEdge e : edges) {
			resultString += e.getData() + " - ";
		}
		System.out.println(resultString);
	}
}
