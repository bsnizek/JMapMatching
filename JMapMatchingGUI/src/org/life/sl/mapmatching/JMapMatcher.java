package org.life.sl.mapmatching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
//import java.util.Set;
import java.util.Vector;

import org.geotools.feature.SchemaException;
//import org.geotools.util.logging.Logging;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.readers.shapefile.PointFileReader;
import org.life.sl.routefinder.Label;
import org.life.sl.routefinder.RouteFinder;

import com.vividsolutions.jts.geom.Point;
//import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.Node;

/**
 * The main map matching algorithm
 * @author bsnizek
 */
public class JMapMatcher {

	private static int kMaximumNumberOfRoutes = 10;	///> the result is constrained to this max. number of routes
	private static String kOutputDir = "results/";
	private PathSegmentGraph graph;						///> data basis (graph)
	private ArrayList<Point> gpsPoints;		///> the path to match (GPS points) 

	/**
	 * Initialization (right now, we only store the PathSegmentGraph locally)
	 * @param g the PathSegmentGraph containing the path
	 */
	public JMapMatcher(PathSegmentGraph g) {
		this.graph = g;
	}
	
	/**
	 * load GPS data (points) from a file
	 * @param fileName shapefile containing the GPS data points 
	 * @throws IOException
	 */
	public void loadGPSPoints(String fileName) throws IOException {
		File pointFile = new File(fileName);	// load data
		PointFileReader pfr = new PointFileReader(pointFile);	// initialize data from file
		gpsPoints = pfr.getPoints();	// the collection of GPS data points
	}

	/**
	 * loads GPS data from a file, then invokes the actual matching
	 * @throws IOException
	 */
	public void match(String fileName) throws IOException {
		loadGPSPoints(fileName);
		match();
	}
	
	/**
	 * controls the map matching algorithm (assumes GPS data and graph data have been loaded before)
	 * @throws IOException
	 */
	public void match() {
		Node fromNode = graph.findClosestNode(gpsPoints.get(0).getCoordinate());	// first node (Origin)
		Node toNode   = graph.findClosestNode(gpsPoints.get(gpsPoints.size()-1).getCoordinate());	// last node in GPS route (Destination) 
		// log coordinates:
		System.out.println("Origin:      " + fromNode.getCoordinate());
		System.out.println("Destination: " + toNode.getCoordinate());

		RouteFinder rf = new RouteFinder(graph);	// perform the actual route finding procedure on the PathSegmentGraph

		rf.calculateNearest();	// calculate nearest edges to all data points

		Vector<Label> labels = rf.findRoutes(fromNode, toNode);	///> list containing all routes that were found (still unsorted)

		// Now do the evaluation (assign score to labels):
		EdgeStatistics eStat = new EdgeStatistics(rf, gpsPoints);
		// loop over all result routes, store them together with their score: 
		for (Label l : labels) {
			l.calcScore(eStat);
		}
		Collections.sort(labels);		// sort labels (result routes) by their score
		Collections.reverse(labels);	// reverse the order, so the best (highest score) comes first

		Iterator<Label> it = labels.iterator();
		boolean first = true;
		int nNonChoice = 0, nOut = 0;
		String outFileName = "";
		while (it.hasNext() && nOut++ < kMaximumNumberOfRoutes) {	// use only the kMaximumNumberOfRoutes best routes
			Label element = it.next();
			//System.out.println(element.getScore());
			try {
				if (first) {	// the first route is the "choice" (best score) ...
					first = false;
					outFileName = kOutputDir + "Best.shp";
				} else {	// ... the other routes are "non-choices"
					outFileName = String.format("%s%03d%s", kOutputDir + "NonChoice", nNonChoice, ".shp");
					nNonChoice++;
				}
				element.dumpToShapeFile(outFileName);	// write result route to file
			} catch (SchemaException e1) {
				// TODO Auto-generated catch block
				System.err.println("Error writing file " + outFileName + " (SchemaException)");
				e1.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				System.err.println("Error writing file " + outFileName + " (IOException)");
				e2.printStackTrace();
			}
		}
	}

	/**
	 * main method: loads the data and invokes the matching algorithm
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String... args) throws IOException {
		PathSegmentGraph g = new PathSegmentGraph("testdata/Sparse_bigger0.shp");	// Let us load the graph ...
		new JMapMatcher(g).match("testdata/GPS_Points.shp");	// ... and invoke the matching algorithm
	}

}
