package org.life.sl.mapmatching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
//import org.geotools.util.logging.Logging;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.ResultRoute;
import org.life.sl.orm.SourcePoint;
import org.life.sl.orm.SourceRoute;
import org.life.sl.readers.shapefile.PointFileReader;
import org.life.sl.routefinder.RFParams;
import org.life.sl.routefinder.Label;
import org.life.sl.routefinder.RouteFinder;
import org.life.sl.utils.Timer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
//import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Node;

// JVM argument for max. heap size (required for large networks): -Xmx2048m

/**
 * The main map matching algorithm
 * @author bsnizek
 */
public class JMapMatcher {
	public enum gpsLoader {
		SHAPEFILE,
		PGSQLDATABASE,
		BULK_PGSQLDATABASE
	}

	private static int kMaxRoutesOutput = 10;	///> the result is constrained to this max. number of routes
	private static String kOutputDir = "results/";
	// input data:
	//static gpsLoader GpsLoader  = gpsLoader.PGSQLDATABASE;
	private static gpsLoader kGPSLoader    = gpsLoader.BULK_PGSQLDATABASE;
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
	
	private PathSegmentGraph graph = null;	///> data basis (graph)
	private ArrayList<Point> gpsPoints;		///> the path to match (GPS points)
	private RFParams rfParams = null;		///> parameters for the route finding algorithm
	
	private int sourcerouteID = 0;
	
	private com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();

	static Logger logger = Logger.getLogger("JMapMatcher");

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
	 * delete the currently loaded graph (by setting it to null)
	 */
	public void clearGraph() {
		this. graph = null;
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
		if (loadGPSPointsFromDatabase(source_id)) {	// check if the track contains points
			sourcerouteID = source_id;				// store in class variable, for later use
			match();								// start the matching process with the loaded track
		} else {
			System.err.println("GPS track " + source_id + " contains no points!");
		}
	}
	
	/**
	 * loads GPS data from the database, given the source_id of the sourcepoints
	 * @param sourceroute_id source_id of the sourcepoints (GPS route)
	 * @return true if the track contains >0 points, false if it is empty
	 */
	private boolean loadGPSPointsFromDatabase(int sourceroute_id) {
		gpsPoints = new ArrayList<Point>();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query result = session.createQuery("from SourcePoint WHERE sourcerouteid=" + sourceroute_id);
		@SuppressWarnings("unchecked")
		Iterator<SourcePoint> iter = result.iterate();
		while (iter.hasNext()) {	// create list from query results
			SourcePoint sp = (SourcePoint) iter.next();
			Point o = sp.getGeometry();
			gpsPoints.add(o);
		}
		session.close();
		return (gpsPoints.size() > 0);
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
		logger.info("Origin:      " + fromNode.getCoordinate());
		logger.info("Destination: " + toNode.getCoordinate());

		Timer timer = new Timer();
		timer.init();	// initialize timer
		
		RouteFinder rf = new RouteFinder(graph, initConstraints());	// perform the actual route finding procedure on the PathSegmentGraph
		rf.calculateNearest();	// calculate nearest edges to all data points (needed for edges statistics)
		// Prepare the evaluation (assigning score to labels):
		@SuppressWarnings("unused")
		EdgeStatistics eStat = new EdgeStatistics(rf, gpsPoints);
		double t_2 = timer.getRunTime(true);

		ArrayList<Label> labels = rf.findRoutes(fromNode, toNode, calcGPSPathLength());	///> list containing all routes that were found (still unsorted)

		double t_1 = timer.getRunTime(true, "++ Routefinding finished");
		double t_3 = 0;
		
		if (!labels.isEmpty()) {
			// loop over all result routes, store them together with their score: 
			/*for (Label l : labels) {
				l.calcScore(eStat);
			}*/
			t_2 += timer.getRunTime(true, "++ Edge statistics created");
			Collections.sort(labels, Collections.reverseOrder());	// sort labels (result routes) by their score in reverse order, so that the best (highest score) comes first
	
			int nOK = saveData(labels);
			
			t_3 = timer.getRunTime(true, "++ " + nOK + " routes stored");
			logger.info("++ findRoutes: " + t_1 + "s");
			logger.info("++ saveRoutes: " + t_3 + "s");
		}
		logger.info("++ Total time: " + (t_1 + t_2 + t_3) + "s");
	}
	
	private int saveData(ArrayList<Label> labels) {
		Iterator<Label> it = labels.iterator();
		// clear database table:
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.createQuery("delete from ResultRoute where sourcerouteid=" + sourcerouteID).executeUpdate();
		session.getTransaction().commit();
		
//		String outFileName = "";
//		int nNonChoice = 0;
		int nOut = 0, nOK = 0;
		boolean first = true;
		while (it.hasNext() && nOut++ < kMaxRoutesOutput) {	// use only the kMaximumNumberOfRoutes best routes
			Label curLabel = it.next();
			/*System.out.println("score: " + curLabel.getScore() 
					+ ", length: " + curLabel.getLength() + " / " + (rf.getGPSPathLength()/curLabel.getLength())
					+ ", a_tot: " + curLabel.getTotalAngle() + ", nLeft: " + curLabel.getLeftTurns() + ", nRight: " + curLabel.getRightTurns());*/
			
			if (writeLabelToDatabase(curLabel, first)) {
				//System.out.println("route stored in database");
				nOK++;
			} else {
				logger.error("ERROR storing route!!");
			}
			
			first = false;	// remaining routes are non-choice
			
//			try {
//				if (first) {	// the first route is the "choice" (best score) ...
//					first = false;
//					outFileName = kOutputDir + "Best.shp";
//				} else {	// ... the other routes are "non-choices"+
//					outFileName = String.format("%s%03d%s", kOutputDir + "NonChoice", nNonChoice, ".shp");
//					nNonChoice++;
//				}
//				curLabel.dumpToShapeFile(outFileName);	// write result route to file
//			} catch (SchemaException e1) {
//				// TODO Auto-generated catch block
//				System.err.println("Error writing file " + outFileName + " (SchemaException)");
//				e1.printStackTrace();
//			} catch (IOException e2) {
//				// TODO Auto-generated catch block
//				System.err.println("Error writing file " + outFileName + " (IOException)");
//				e2.printStackTrace();
//			}
		}
		return nOK;
	}
	
	private boolean writeLabelToDatabase(Label label, boolean isChoice) {
		boolean ok = false;
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		List<DirectedEdge> dEdges = label.getRouteAsEdges();
		// set up entry for route table:
		ResultRoute route = new ResultRoute();
		// set route parameters:
		
		route.setLength((float)label.getLength());
		route.setSelected(isChoice);
		route.setSourcerouteid(sourcerouteID);
		// entry for node table:
		// ResultNode node = new ResultNode();	// TODO: create ORM connector
		
		// get node list to create the lineString representing the route:
		List<Node> nodes = label.getNodes();
		Coordinate[] coordinates = new Coordinate[nodes.size()];
		int i = 0;
		for (Node curNode : nodes) {
			coordinates[i++] = curNode.getCoordinate();
		} 
		ok = (i > 0);	// true if there were any points in the route
		
		// HashMap<String, Object> hm;
//		Coordinate[] coordinates = new Coordinate[dEdges.size() +1];
//		for (DirectedEdge de : dEdges) {
//			if (i==0) {
//				Node node1 = de.getFromNode();
//				coordinates[0] = node1.getCoordinate();
//			}
//				Node node2 = de.getToNode();
//				coordinates[i+1] = node2.getCoordinate();
//			
//			// Integer eID = (Integer) de.getData().get("id");
//			//OSMEdge osme = new OSMEdge();
//			// TODO: How to go on from here:
//			// 1. get OSMEdge from database
//			// 2. get OSMNode-IDs from OSMEdge
//			//    problem: direction of edge / sequnce of nodes??
//			// 3. store OSMNode-IDs in array
//			// 4. store this array in the table ResultNodes
//			i++;
//		} 

		if (ok) try {
			LineString lineString = fact.createLineString(coordinates);
			route.setGeometry(lineString);
			session.save(route);
			session.getTransaction().commit();
		} catch (Exception e) {
			ok = false;
		}

		return ok;
	}
	
	private PathSegmentGraph loadGraphFromDB(ArrayList<Point> track) {
		return new PathSegmentGraph(track, (float)initConstraints().getDouble(RFParams.Type.NetworkBufferSize));
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

		rfParams.setInt(RFParams.Type.MaximumNumberOfRoutes, 2000);	///> maximum number of routes to find
		rfParams.setInt(RFParams.Type.BridgeOverlap, 1);
		rfParams.setInt(RFParams.Type.EdgeOverlap, 1);		///> how often each edge may be used
		//rfParams.setInt(RFParams.Type.ArticulationPointOverlap, 2);
		rfParams.setInt(RFParams.Type.NodeOverlap, 1);		///> how often each single node may be crossed
		rfParams.setDouble(RFParams.Type.DistanceFactor, 1.1);	///> how much the route may deviate from the shortest possible
		rfParams.setDouble(RFParams.Type.MinimumLength, 0.0);		///> minimum route length
		rfParams.setDouble(RFParams.Type.MaximumLength, 1.e20);	///> maximum route length (quasi no limit here)
		rfParams.setDouble(RFParams.Type.NetworkBufferSize, 100.);	///> buffer size in meters (!)
		
		return rfParams;
	}

	/**
	 * main method: loads the data and invokes the matching algorithm
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String... args) throws IOException {
		//Logger logger = Logger.getRootLogger();
		//BasicConfigurator.configure();	// set up logging
		logger.setLevel(Level.INFO);
		
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
		} 
		
		if (kGPSLoader == gpsLoader.SHAPEFILE) {
			new JMapMatcher(g).match(kGPSPointFileName);	// get track data from a file
		}
		
		// ... a sweet bulk loader
		if (kGPSLoader == gpsLoader.BULK_PGSQLDATABASE) {
			// 1. get a list over sourceroute ids
			org.hibernate.classic.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			
			Query result = session.createQuery("from SourceRoute");
			@SuppressWarnings("unchecked")
			Iterator<SourceRoute> iterator = result.iterate();
			logger.info(result.list().size() + " tracks to be matched");
			ArrayList<Integer> sRoutes= new ArrayList<Integer>();
			while (iterator.hasNext()) {
				SourceRoute sR = iterator.next();
				sRoutes.add(sR.getId());
				//
			}
			JMapMatcher jmm = new JMapMatcher(null);
			for (int i=0; i<sRoutes.size(); i++) {
				logger.info("--- Matching track " + sRoutes.get(i) + "...");
				jmm.clearGraph();	// clear the graph, so that a new one enveloping the current track is loaded
				jmm.match(sRoutes.get(i));
				logger.info("--- Track " + sRoutes.get(i) + " matched.");
			}
			
		}
	}
}
