package org.life.sl.mapmatching;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.feature.SchemaException;
import org.hibernate.Query;
import org.hibernate.Session;
//import org.geotools.util.logging.Logging;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.OSMEdge;
import org.life.sl.orm.OSMNode;
import org.life.sl.orm.ResultRoute;
import org.life.sl.orm.ResultNodeChoice;
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

	private static String kCfgFileName = "JMM.cfg";
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
	private JMMConfig cfg = new JMMConfig(kCfgFileName);	///> parameters for the MapMatching controller
	
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
	
			int nOK = saveData(labels, rf.getNumLabels(), fromNode, toNode);
			
			t_3 = timer.getRunTime(true, "++ " + nOK + " routes stored");
			logger.info("++ findRoutes: " + t_1 + "s");
			logger.info("++ saveRoutes: " + t_3 + "s");
		}
		logger.info("++ Total time: " + (t_1 + t_2 + t_3) + "s");
	}
	
	private int saveData(ArrayList<Label> labels, long nLabels, Node fromNode, Node toNode) {
		Iterator<Label> it = labels.iterator();
		
		// clear database table:
		if (cfg.bWriteToDatabase) {
			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			session.createQuery("delete from ResultRoute where sourcerouteid=" + sourcerouteID).executeUpdate();
			session.createQuery("delete from ResultNodeChoice where sourcerouteid=" + sourcerouteID).executeUpdate();
			session.getTransaction().commit();
		}	
		
		String outFileName = "";
		int nNonChoice = 0;
		int nOut = 0, nOK = 0;
		boolean first = true;
		while (it.hasNext() && nOut++ < cfg.nRoutesToWrite) {	// use only the kMaximumNumberOfRoutes best routes
			Label curLabel = it.next();
			
			if (cfg.bWriteToDatabase) {
				if (writeLabelToDatabase(curLabel, first, nLabels, fromNode, toNode)) {
					//System.out.println("route stored in database");
					nOK++;
				} else {
					logger.error("ERROR storing route!!");
				}
			}
			
			if (cfg.bWriteToShapefiles) {
				try {
					if (first) {	// the first route is the "choice" (best score) ...
						first = false;
						outFileName = kOutputDir + "Best.shp";
					} else {	// ... the other routes are "non-choices"+
						outFileName = String.format("%s%03d%s", kOutputDir + "NonChoice", nNonChoice, ".shp");
						nNonChoice++;
					}
					curLabel.dumpToShapeFile(outFileName);	// write result route to file
				} catch (SchemaException e1) {
					System.err.println("Error writing file " + outFileName + " (SchemaException)");
				} catch (IOException e2) {
					System.err.println("Error writing file " + outFileName + " (IOException)");
				}
			}
			
			first = false;	// remaining routes are non-choices
		}
		return nOK;
	}
	
	private boolean writeLabelToDatabase(Label label, boolean isChoice, long nLabels, Node fromNode, Node toNode) {
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
		
		// get node list to create the lineString representing the route:
		Coordinate[] coordinates = label.getCoordinates();
		ok = (coordinates.length > 0);	// true if there were any points in the route
		
		if (ok) try {
			LineString lineString = fact.createLineString(coordinates);
			route.setGeometry(lineString);
			route.setnAlternatives(nLabels);
			route.setnPtsOn(label.getScoreCount());
			route.setnPtsOff(gpsPoints.size() - label.getScoreCount());
			route.setnEdges(dEdges.size());
			route.setnEdgesWOPts(label.getnEdgesWOPoints());
			//route.setdistPEavg...
			route.setMatchScore((float)label.getScore());
			route.setMatchFrac((float)(1. - label.getNoMatchLength() / label.getLength()));
			route.setTrackLengthFrac((float)(label.getLength() / calcGPSPathLength()));
			
			route.setnLeftTurns((short)label.getLeftTurns());
			route.setnRightTurns((short)label.getRightTurns());
			route.setnTrafficLights((short)label.getnTrafficLights());
			route.setEnvAttr(label.getEnvAttr());
			route.setCykAttr(label.getCykAttr());
			route.setGroenM((float)label.getGroenM());
			
			session.save(route);
			
			// create output for the choice experiment:
			if (cfg.bWriteChoices && isChoice) {	// (this is required only for the chosen route)
				ResultNodeChoice choice = new ResultNodeChoice(route.getId(), sourcerouteID);
				
				double dist = 0.;
				int i = 0;	// counter
				double[] edgeLengths = label.getEdgeLengths();
				DirectedEdge lastEdge = null;
				List<DirectedEdge> edges = label.getRouteAsEdges();
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
					// match coordinates:
					@SuppressWarnings("unchecked")
					Iterator<OSMNode> it = nodeRes.iterate();
					while (it.hasNext()) {
						OSMNode on = it.next();
						Coordinate onc = on.getGeometry().getCoordinate();
						if (Math.abs(c_n.x - onc.x) < 1.e-6 && Math.abs(c_n.x - onc.x) < 1.e-6) {
							nodeID = on.getId();
							break;
						}								
					}	// now, nodeID is either 0 or the database ID of the corresponding node
					choice.setNodeID(nodeID);
					choice.setI(i);
					choice.setDist((float)(dist / label.getLength()));	// distance along the route as fraction of the whole route
					
					// angle from node to destination:
					Coordinate c_d = toNode.getCoordinate();
					double angle_direct = Math.atan2(c_d.y - c_n.y, c_d.x - c_n.x);

					@SuppressWarnings("unchecked")
					List<DirectedEdge> outEdges = node.getOutEdges().getEdges();
					for (DirectedEdge oe : outEdges) {
						if (lastEdge != null && oe != lastEdge) {	// don't use the backEdge!
							@SuppressWarnings("unchecked")
							HashMap<String, Object> ed2 = (HashMap<String, Object>) oe.getEdge().getData();
							choice.setSelected(oe == e);
							choice.setEdgeID((Integer)ed2.get("id"));
							choice.setEnvType((Short)ed2.get("et"));
							choice.setCykType((Short)ed2.get("ct"));
							choice.setAngleToDest((float)(Math.toDegrees(oe.getAngle() - angle_direct)));	// store value in degrees
	
							session.save(choice.clone());	// save 1 choice/nonchoice for each outEdge!
						}
					}
					dist += edgeLengths[i++];
					lastEdge = e;	// save for next comparison
				}
				
			}
			
			session.getTransaction().commit();
		} catch (Exception e) {
			ok = false;
			System.out.println(e);
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

		// initialize with hardwired default values:
		rfParams.setInt(RFParams.Type.MaximumNumberOfRoutes, 2000);	///> maximum number of routes to find (or 0 for infinite)
		rfParams.setInt(RFParams.Type.BridgeOverlap, 1);
		rfParams.setInt(RFParams.Type.EdgeOverlap, 1);		///> how often each edge may be used
		//rfParams.setInt(RFParams.Type.ArticulationPointOverlap, 2);
		rfParams.setInt(RFParams.Type.NodeOverlap, 1);		///> how often each single node may be crossed
		rfParams.setDouble(RFParams.Type.DistanceFactor, 1.1);		///> how much the route may deviate from the shortest possible
		rfParams.setDouble(RFParams.Type.MinimumLength, 0.0);		///> minimum route length
		rfParams.setDouble(RFParams.Type.MaximumLength, 1.e20);		///> maximum route length (quasi no limit here)
		rfParams.setDouble(RFParams.Type.NetworkBufferSize, 100.);	///> buffer size in meters (!)
		
		// read config file, eventually overwriting existing values:
		int r = rfParams.readFromFile(kCfgFileName);
		if (r < 0) logger.warn("Config file " + kCfgFileName + " not found!");
		else logger.info("Read " + r + " values from config file " + kCfgFileName);
		
		return rfParams;
	}

	/**
	 * main method: loads the data and invokes the matching algorithm
	 * @param args command line arguments: 1: route ID; 2: route ID (end of range)
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
			
			Query result;
			String query = "from SourceRoute";
			if (args.length == 1) query += " WHERE id="+args[0];
			if (args.length == 2) query += " WHERE id>="+args[0]+" AND id<="+args[1];
			System.out.println(query);
			result = session.createQuery(query);
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
