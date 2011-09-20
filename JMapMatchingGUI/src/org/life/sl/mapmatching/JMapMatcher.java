package org.life.sl.mapmatching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.geotools.feature.SchemaException;
import org.hibernate.Query;
import org.hibernate.Session;
//import org.geotools.util.logging.Logging;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.SourcePoint;
import org.life.sl.readers.shapefile.PointFileReader;
import org.life.sl.routefinder.RFParams;
import org.life.sl.routefinder.Label;
import org.life.sl.routefinder.RouteFinder;

import com.vividsolutions.jts.geom.Point;
//import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.Node;

// ${build_files:-Xmx=2048m}

/**
 * The main map matching algorithm
 * @author bsnizek
 */
public class JMapMatcher {
	public enum gpsLoader {
		SHAPEFILE,
		PGSQLDATABASE
	}

	private static int kMaxRoutesOutput = 10;	///> the result is constrained to this max. number of routes
	private static String kOutputDir = "results/";
	// input data:
	//static gpsLoader GpsLoader  = gpsLoader.PGSQLDATABASE;
	private static gpsLoader kGPSLoader    = gpsLoader.SHAPEFILE;
	private static gpsLoader kGraphLoader  = gpsLoader.SHAPEFILE;
	private static boolean kUseMinimalNetwork = true;	///> true: restrict network to an area enveloping the track
	
	private static int kGPSTrackID = 12158;		///> database ID of GPS track to match
	
	// even bigger network and route:
//	private static String kGraphDataFileName = "testdata/OSM_CPH/osm_line_cph_ver4.shp";
//	private static String kGPSPointFileName = "testdata/exmp1/example_gsp_1.shp";
	// bigger network and route:
	private static String kGraphDataFileName = "testdata/SparseNetwork.shp";
	private static String kGPSPointFileName = "testdata/GPS_Points.shp";
	// smaller network and route:
//	private static String kGraphDataFileName = "testdata/Sparse_bigger0.shp";
//	private static String kGPSPointFileName = "testdata/GPS_Points_1.shp";
	
	private PathSegmentGraph graph;			///> data basis (graph)
	private ArrayList<Point> gpsPoints;		///> the path to match (GPS points)
	private RFParams rfParams = null;		///> parameters for the route finding algorithm
	
	private double t_start;					// for timing purposes only

	/**
	 * default constructor: initialization with an empty graph (graph must then be be created later on)
	 */
	public JMapMatcher() {
		this.graph = null;
	}

	/**
	 * Initialization with an existing graph (right now, we only store the PathSegmentGraph locally)
	 * @param g the PathSegmentGraph containing the path (if ==null, it can be created later on)
	 */
	public JMapMatcher(PathSegmentGraph g) {
		this.graph = g;
	}

	/**
	 * loads GPS data from a file, then invokes the actual matching
	 * @throws IOException
	 */
	public void match(String fileName) throws IOException {
		if (kGPSLoader == gpsLoader.PGSQLDATABASE) {
			loadGPSPointsFromDatabase(12158);
		} else {
			loadGPSPoints(fileName);
		}
		
		match();
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
	 * starts the map matching using GPS data from the database
	 * @param source_id source_id of the sourcepoints (GPS route)
	 */
	public void match(int source_id)  {
		loadGPSPointsFromDatabase(source_id);
		match();
	}
	
	/**
	 * loads GPS data from the database, given the source_id of the sourcepoints
	 * @param sourceroute_id source_id of the sourcepoints (GPS route)
	 */
	private void loadGPSPointsFromDatabase(int sourceroute_id) {
		gpsPoints = new ArrayList<Point>();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query result = session.createQuery("from SourcePoint WHERE sourcerouteid=" + sourceroute_id);
		@SuppressWarnings("unchecked")
		Iterator<SourcePoint> iter = result.iterate();
		while (iter.hasNext()) {
			SourcePoint sp = (SourcePoint) iter.next();
			Point o = sp.getGeometry();
			gpsPoints.add(o);
		}
		session.close();		
	}

	/**
	 * controls the map matching algorithm (assumes GPS data and graph data have been loaded before);
	 * if no graph has been loaded yet, a new graph enveloping the GPS track is read from the database
	 * @throws IOException
	 */
	public void match() {
		if (graph == null) {	// create a new graph enveloping the GPS track
			graph = loadGraphFromDB(gpsPoints);
		}
		Node fromNode = graph.findClosestNode(gpsPoints.get(0).getCoordinate());	// first node (Origin)
		Node toNode   = graph.findClosestNode(gpsPoints.get(gpsPoints.size()-1).getCoordinate());	// last node in GPS route (Destination) 
		// log coordinates:
		System.out.println("Origin:      " + fromNode.getCoordinate());
		System.out.println("Destination: " + toNode.getCoordinate());

		initTiming();	// initialize timer
		
		RouteFinder rf = new RouteFinder(graph, initConstraints());	// perform the actual route finding procedure on the PathSegmentGraph
		rf.calculateNearest();	// calculate nearest edges to all data points (needed for edges statistics)
		// Prepare the evaluation (assigning score to labels):
		@SuppressWarnings("unused")
		EdgeStatistics eStat = new EdgeStatistics(rf, gpsPoints);
		double t_2 = timing(true);

		ArrayList<Label> labels = rf.findRoutes(fromNode, toNode, calcGPSPathLength());	///> list containing all routes that were found (still unsorted)

		double t_1 = timing(true, "++ Routefinding finished");
		
		// loop over all result routes, store them together with their score: 
		/*for (Label l : labels) {
			l.calcScore(eStat);
		}*/
		Collections.sort(labels, Collections.reverseOrder());	// sort labels (result routes) by their score in reverse order, so that the best (highest score) comes first

		t_2 += timing(true, "++ Edge statistics created");

		Iterator<Label> it = labels.iterator();
		boolean first = true;
		int nNonChoice = 0, nOut = 0;
		String outFileName = "";
		while (it.hasNext() && nOut++ < kMaxRoutesOutput) {	// use only the kMaximumNumberOfRoutes best routes
			Label element = it.next();
			System.out.println("score: " + element.getScore() 
					+ ", length: " + element.getLength() + " / " + (rf.getGPSPathLength()/element.getLength())
					+ ", a_tot: " + element.getTotalAngle() + ", nLeft: " + element.getLeftTurns() + ", nRight: " + element.getRightTurns());
			try {
				if (first) {	// the first route is the "choice" (best score) ...
					first = false;
					outFileName = kOutputDir + "Best.shp";
				} else {	// ... the other routes are "non-choices"+
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

		double t_3 = timing(true, "++ Files written");
		System.out.println("++ findRoutes: " + t_1 + "s");
		System.out.println("++ writeFiles: " + t_3 + "s");
		System.out.println("++ Total time: " + (t_1 + t_2 + t_3) + "s");
	}
	
	private PathSegmentGraph loadGraphFromDB(ArrayList<Point> track) {
		return new PathSegmentGraph(track);
	}
	/**
	 * small utility to initialize the timing functionality
	 */
	private void initTiming() {
		t_start = ((double)System.nanoTime()) * 1.e-9;
	}
	/**
	 * small timing utility: calculate the time passed since t_start
	 * @param reset if true, t_start is reset to the current time (i.e., a new interval is started)
	 * @return the time interval in seconds passed since t_start
	 */
	private double timing(boolean reset) {
		double t = ((double)System.nanoTime()) * 1.e-9 - t_start;
		if (reset) initTiming();
		return t;
	}
	/**
	 * small timing utility: calculate the time passed since t_start and write out a corresponding message
	 * @param reset if true, t_start is reset to the current time (i.e., a new interval is started)
	 * @param msg an additional message to show on the console
	 * @return the time interval in seconds passed since t_start
	 */
	private double timing(boolean reset, String msg) {
		double t = timing(reset);
		System.out.println(msg + ", t = " + t + "s");
		return t;
	}
	
	/**
	 * calculate the Euclidian path length as sum of the Euclidian distances between subsequent measurement points
	 * @return the path length along the GPS points
	 */
	public double calcGPSPathLength() {
		double l = 0;
		Point p0 = null;
		for (Point p : gpsPoints) {
			if (p0 != null) {
				l += p.distance(p0);
			}
			p0 = p;
		}
		return l;
	}

	private RFParams initConstraints() {
		// initialize constraint fields:
		rfParams = new RFParams();

		rfParams.setInt(RFParams.Type.MaximumNumberOfRoutes, 100);	///> maximum number of routes to find
		rfParams.setInt(RFParams.Type.BridgeOverlap, 1);
		rfParams.setInt(RFParams.Type.EdgeOverlap, 1);		///> how often each edge may be used
//		constraints.setInt(Constraints.Type.ArticulationPointOverlap, 2);
		rfParams.setInt(RFParams.Type.NodeOverlap, 1);		///> how often each single node may be crossed
		rfParams.setDouble(RFParams.Type.DistanceFactor, 1.2);	///> how much the route may deviate from the shortest possible
		rfParams.setDouble(RFParams.Type.MinimumLength, 0.0);		///> minimum route length
		rfParams.setDouble(RFParams.Type.MaximumLength, 1.e20);	///> maximum route length (quasi no limit here)
		
		return rfParams;
	}

	/**
	 * main method: loads the data and invokes the matching algorithm
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String... args) throws IOException {
		PathSegmentGraph g = null;
		// Let us load the graph ...
		if (kGraphLoader == gpsLoader.PGSQLDATABASE) {
			if (!kUseMinimalNetwork) g = new PathSegmentGraph(1);	//... or delay the loading if kUseMinimalNetwork==true
		} else if (kGraphLoader == gpsLoader.SHAPEFILE) {
			g = new PathSegmentGraph(kGraphDataFileName);	// get network from a file
		}
		// ... and invoke the matching algorithm:
		if (kGPSLoader == gpsLoader.PGSQLDATABASE) {
			new JMapMatcher(g).match(kGPSTrackID);			// get track from database; NOTE: g may be null here
		} else if (kGPSLoader == gpsLoader.SHAPEFILE) {
			new JMapMatcher(g).match(kGPSPointFileName);	// get track data from a file
		}
	}
}
