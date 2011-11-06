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
import org.life.sl.utils.MathUtil;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
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
	
	/**
	 * Comparator comparing only the last edge of two labels
	 * @author bb
	 *
	 */
	public static class LastEdgeComparator implements Comparator<Label> {
		public int compare(Label arg0, Label arg1) { return arg0.compareTo_LE(arg1); }	
	}
	public static class LastEdgeComparatorRev implements Comparator<Label> {
		public int compare(Label arg0, Label arg1) { return -arg0.compareTo_LE(arg1); }	
	}
	
	private Label parent;			///> The parent of the Label
	private Node node;				///> The node associated with this Label
	private DirectedEdge backEdge;	///> The GeoEdge leading back to the node associated with the parent Label
	private double length = 0.;		///> if the label represents a route, this is the length of the route (sum of all backEdges)
	private double lastEdgeLength = 0.;	///> length of last backEdge
	private double score = -1.;		///> the score of the label (evaluated according to edge statistics)
	private short scoreCount = -1;	///> the unweighted score of the label (nearest points-count)
	//private double lastScore = 0.;	///> the same but considering only the last edge	
	private short lastScoreCount = 0;
	
	private List<Node> nodeList = null;
	private List<DirectedEdge> edgeList = null;

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
	}

	/**
	 * Create a new "empty" Label at a node;
	 * parent and backEdge are set to null, length is set to 0.0 and expand is set to true.
	 * @param node The node associated with this Label.
	 */
	public Label(Node node) {
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
	 * Comparison method
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
	 * Comparison method using only the last edge;
	 * in addition to the score, the direction relative to the last edge is evaluated if necessary
	 * @param arg0 the other object to compare this to
	 * @return 1 if this object is larger than the other, -1 if smaller, 0 if equal
	 */
	public int compareTo_LE(Label arg0) {
		int r = 0;
		int ov = arg0.getLastScoreCount();
		if (this.lastScoreCount > ov) r = 1;
		else if (this.lastScoreCount < ov) r = -1;
		else {
			r = compareTo(arg0);	// default fallback: if both are equal, sort them according to their total score
			if (ov==0) {	// both edges have no points associated: compare directions
				if (parent != null && parent.backEdge != null) {
					/* Some notes:
					 * - both edges should have the same parent!
					 * - at the root node (parent=null), this comparison should not be invoked anyway
					 * - at the first node, we don't have a reference direction yet;
					     but if we are at the first node after root and don't have GPS points, we have a problem anyway */
//					double a = parent.backEdge.getSym().getAngle() - Math.PI;
//					if (Math.abs(MathUtil.mapAngle_radians(this.backEdge.getAngle() - a)) 
//							< Math.abs(MathUtil.mapAngle_radians(arg0.backEdge.getAngle() - a))) r = 1;
					if (Math.abs(this.getAngleDiff()) < Math.abs(arg0.getAngleDiff())) r = 1;
					else r = -1;	// (smaller deviation is better)
				}
			}
		}
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
	 * Convenience function: returns distance from the current to another node 
	 * @param node1 the other node
	 * @return Cartesian distance between current node and node1 
	 */
	public double getDistanceTo(Node node1) {
		return node.getCoordinate().distance(node1.getCoordinate());
	}
	
	/**
	 * Computes the angular difference between the two previous edges
	 * @return
	 */
	public double getAngleDiff() {
		if (parent != null && parent.backEdge != null) {
			return MathUtil.mapAngle_radians(backEdge.getAngle() - (parent.backEdge.getSym().getAngle() - Math.PI));
		} else return 0;	// at the first node, we don't have a reference direction yet
	}

	/**
	 * calculate the weighted and unweighted score of this label from the edge statistics;
	 * 	this is a measure of the quality of the route: number of fitting data points, normalized to route length
	 *  (the recursive variant is about 3 times faster than doing it explicitely for all edges)
	 * @param eStat edge statistics
	 */
	public void calcScore(EdgeStatistics eStat) {
		// calculate the score recursively:
		score = 0.;
		scoreCount = 0;
		if (parent != null) {
			lastScoreCount = eStat.getCount(backEdge.getEdge());
			scoreCount = (short) (parent.getScoreCount(eStat) + lastScoreCount);
//			lastScore = (double)lastScoreCount / lastEdgeLength;
			//score = Math.round(parent.getScore(eStat) * parent.getLength()) + eStat.getCount(backEdge.getEdge());	// backEdge should be the last edge in the label
			if (length > 0.) score = scoreCount / length;
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
	public short getScoreCount(EdgeStatistics eStat) {
		if (scoreCount < 0) calcScore(eStat);
		return scoreCount;
	}
	public short getScoreCount() {
		return scoreCount;
	}
//	public double getLastScore() {
//		return lastScore;
//	}
	public short getLastScoreCount() {
		return lastScoreCount;
	}
	
	public double getLastEdgeLength() {
		return this.lastEdgeLength;
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
	 * Create a list of all the directed edges along this route, starting at the origin;
	 * for efficiency, this list is only compiled once and cached in edgeList.
	 * @return the route represented by this label as a list of directed edges
	 */
	public List<DirectedEdge> getRouteAsEdges() {
		if (edgeList == null || edgeList.size() == 0) {
			edgeList = new ArrayList<DirectedEdge>();
			Label label = this;
			while(label.getParent() != null) {
				edgeList.add(label.getBackEdge());
				label = label.getParent();
			}
			Collections.reverse(edgeList);	// now, the topmost label represents the first edge in the list
		}
		return edgeList;
	}

	/**
	 * Create a list of all the Nodes along this route, starting with the origin;
	 * for efficiency, this list is only compiled once and cached in nodeList.
	 * @return a list of all the Nodes of this label starting with the origin 
	 */
	public List<Node> getNodes() {
		if (nodeList == null || nodeList.size() == 0) {
			nodeList = new ArrayList<Node>();
			Label label = this;
			while(label != null) {
				nodeList.add(label.getNode());
				label = label.getParent();
			}
			Collections.reverse(nodeList);	// now, the origin node is first in the list
		}
		return nodeList;
	}

	/**
	 * Create a list of all the Nodes along this route, starting with the origin;
	 * for efficiency, this list is only compiled once and cached in nodeList.
	 * @return a list of all the Nodes of this label starting with the origin 
	 */
	public List<Label> getLabels() {
		ArrayList<Label> lblList = new ArrayList<Label>();
		Label label = this;
		while(label != null) {
			lblList.add(label);
			label = label.getParent();
		}
		Collections.reverse(lblList);	// now, the first label is first in the list
		return lblList;
	}
	
	/**
	 * @return an array of Coordinates of all nodes (sorted from start to end)
	 */
	public Coordinate[] getCoordinates() {
		List<Node> nodes = getNodes();
		Coordinate[] coordinates = new Coordinate[nodes.size()];
		int i = 0;
		for (Node curNode : nodes) {
			coordinates[i++] = curNode.getCoordinate();
		}
		return coordinates;
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
