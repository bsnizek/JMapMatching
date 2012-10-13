/**
 * 
 */
package org.life.sl.mapmatching;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.SourcePoint;
import org.life.sl.orm.SourceRoute;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;

/**
 * @author besn
 *
 */
public class AlternativeMapMatcher {


	private static final float BUFFERDISTANCE = 50;
	private static String kCfgFileName = "JMM.cfg";
	private static JMMConfig cfg = new JMMConfig(kCfgFileName);	
	private static Logger logger = Logger.getLogger("JMapMatcher");
	private GPSTrack gpsPoints;
	private int sourcerouteID = 0;
	//private RFParams rfParams = null;		///< parameters for the route finding algorithm
	private PathSegmentGraph graph;
	Session session = HibernateUtil.getSessionFactory().getCurrentSession();

	public AlternativeMapMatcher(Object object) {
		// TODO Auto-generated constructor stub
	}




	/**
	 * Loads a PathSegmentGraph containing the network to use (track + buffer)
	 * @param track the GPS data
	 * @return
	 */
	private PathSegmentGraph loadGraphFromDB(GPSTrack track) {
		String dumpFile = "";
		if (cfg.bDumpNetwork) {
			File dir = new File(cfg.sDumpNetworkDir);
			if (!dir.exists()) dir.mkdirs();
			dumpFile = String.format("%s/%05d%s", cfg.sDumpNetworkDir, sourcerouteID, "_network.shp");	// path for network buffer dump
		}

		return new PathSegmentGraph(track, 100, dumpFile);
		//return new PathSegmentGraph(1);
	}


	private void match(Integer sourceroute_id) {
		System.out.println("Matching sourceroute (ID=" + sourceroute_id + ")");
		gpsPoints = new GPSTrack(sourceroute_id);
		if (gpsPoints.size() > 0) {	// check if the track contains points
			sourcerouteID = sourceroute_id;				// store in class variable, for later use
			match();								// start the matching process with the loaded track
		} else {
			System.err.println("GPS track " + sourceroute_id + " contains no points!");
		}

	}

	class ValueComparator implements Comparator<Object> {

		Map<DirectedEdge, Integer> base;
		public ValueComparator(Map<DirectedEdge, Integer> base) {
			this.base = base;
		}

		public int compare(Object a, Object b) {

			if(base.get(a) < base.get(b)) {
				return 1;
			} else if(base.get(a) == base.get(b)) {
				return 0;
			} else {
				return -1;
			}
		}
	}


	private void match() {

		org.hibernate.classic.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Node currentNode = null;

		graph = loadGraphFromDB(gpsPoints);
		// graph.splitGraphAtPoint(gpsPoints.getCoordinate(0));
		// graph.splitGraphAtPoint(gpsPoints.getCoordinate(-1));
		Node fromNode = graph.findClosestNode(gpsPoints.getCoordinate(0));	// first node (Origin)
		Node toNode   = graph.findClosestNode(gpsPoints.getCoordinate(-1));	// last node in GPS route (Destination) 
		currentNode = fromNode;

		ArrayList<Integer> visitedEdges = new ArrayList<Integer>();

		while (currentNode.equals(toNode) == false) {
			// 1. Get Star
			DirectedEdgeStar star = currentNode.getOutEdges();
			@SuppressWarnings("unchecked")
			List<Object> outEdges = star.getEdges();
			System.out.println("# out edges: " + outEdges.size());
			HashMap<DirectedEdge, Integer> hm = new HashMap<DirectedEdge, Integer>();
			for (Object ee: outEdges) {
				DirectedEdge e = (DirectedEdge) ee;
				Integer eId = (Integer)((HashMap<?, ?>) e.getEdge().getData()).get("id");
				Geometry l = (LineString)((HashMap<?, ?>) e.getEdge().getData()).get("geom");
				Geometry buffer = l.buffer(BUFFERDISTANCE);
				Criteria testCriteria = session.createCriteria(SourcePoint.class);
				testCriteria.add(SpatialRestrictions.within("geometry", buffer));
				testCriteria.add(SpatialRestrictions.intersects("geometry", buffer));
				@SuppressWarnings("unchecked")
				List<SourcePoint> result = testCriteria.list();
				if (visitedEdges.contains(eId) == false) {
					hm.put(e, result.size());
				}
			}

			// let us find the edge with the most points
			ValueComparator bvc =  new ValueComparator(hm);
			TreeMap<DirectedEdge,Integer> sorted_map = new TreeMap<DirectedEdge, Integer>(bvc);

			sorted_map.putAll(hm);

			if (sorted_map.size()>0) {

				DirectedEdge de = sorted_map.lastKey();
				Integer EdgeId = (Integer) ((HashMap<?, ?>) de.getEdge().getData()).get("id");
				System.out.println("EDGE ID: " + EdgeId);

				Node fn = de.getFromNode();
				Node tn = de.getToNode();
				if (fn.equals(currentNode)) {
					currentNode = tn;
				} else {
					currentNode = fn;
				}

				visitedEdges.add(EdgeId);} else break;
		}

	}



	/**
	 * @param args
	 * Main method
	 */
	public static void main(String[] args) {
		//PathSegmentGraph g = null;
		//g = new PathSegmentGraph(1);

		org.hibernate.classic.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query result;
		String query = "from SourceRoute";
		if (args.length == 0 && !cfg.sourcerouteIDs.trim().isEmpty()) query += " WHERE id IN ("+cfg.sourcerouteIDs+")";
		if (args.length == 1) query += " WHERE id="+args[0];
		if (args.length == 2) query += " WHERE id>="+args[0]+" AND id<="+args[1];
		query += " ORDER BY id";
		result = session.createQuery(query);
		@SuppressWarnings("unchecked")
		Iterator<SourceRoute> iterator = result.iterate();
		logger.info(result.list().size() + " tracks to be matched");
		ArrayList<Integer> sRoutes= new ArrayList<Integer>();
		while (iterator.hasNext()) {
			SourceRoute sR = iterator.next();
			sRoutes.add(sR.getId());
		}

		AlternativeMapMatcher jmm = new AlternativeMapMatcher(null);
		for (int i=0; i<sRoutes.size(); i++) {
			Integer route = sRoutes.get(i);
			jmm.match(route);
		}
	}


}
