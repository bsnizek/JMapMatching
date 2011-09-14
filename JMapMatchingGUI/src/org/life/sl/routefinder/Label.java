package org.life.sl.routefinder;

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
	
	/**
	 * As Labels can represent routes, this is the length of that route. It is defined
	 * as the sum of all backedges, summing up over parent Labels until a Label has no parent.
	 */
	private double length = 0.;
	private double lastEdgeLength = 0.;

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
	
	public int getTreeLevel() {
		return treeLevel;
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

	/**
	 * calculate the weighted and unweighted score of this label from the edge statistics;
	 * 	this is a measure of the quality of the route: number of fitting data points, normalized to route length
	 *  (the recursive variant is about 3 times faster than doing it explicitely for all edges)
	 * @param eStat edge statistics
	 */
	public void calcScore(EdgeStatistics eStat) {
		if (false) {
			double s = 0;
			List<DirectedEdge> edges = this.getRouteAsEdges();	
			for (DirectedEdge e : edges) {
				s += eStat.getCount(e.getEdge());
			}
			score = s/this.getLength();
		} else {
			score = 0.;
			scoreCount = 0;
			if (parent != null) {
				//System.out.println((int)(parent.getScore(eStat) * parent.getLength()) + " - " + eStat.getCount(backEdge.getEdge()));
				lastScoreCount = eStat.getCount(backEdge.getEdge());
				lastScore = lastScoreCount / lastEdgeLength;
				scoreCount = parent.getScoreCount(eStat) + lastScoreCount;
				//score = Math.round(parent.getScore(eStat) * parent.getLength()) + eStat.getCount(backEdge.getEdge());	// backEdge should be the last edge in the label
				if (length > 0.) score = scoreCount / length;
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
}
