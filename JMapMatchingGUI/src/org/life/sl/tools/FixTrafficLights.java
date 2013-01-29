package org.life.sl.tools;

import java.math.BigInteger;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.ResultRoute;

public class FixTrafficLights {

	private static final int batchSize = 50;
	private static Logger logger = Logger.getLogger("RouteFix");

	public static void fixRoute(ResultRoute route) {
		String se = route.getEdgeIDs();	// the list of edge IDs
		if (se.endsWith(",")) se = se.substring(0, se.length()-1);
//		String[] sa = se.split(",");
		// get the node IDs for the edges:
//		int[] nodeIDs = new int[sa.length+1];
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		//session.beginTransaction();

		String s1 = "select distinct(fromNode) as nodeID from osmedge where id in ("+se+")"
			+ " union " +
			"select distinct(fromNode) as nodeID from osmedge where id in ("+se+")"
			+ "group by nodeID";
		//Query nodeRes = session.createQuery(s1);
		//System.out.println(s1);
		
		String s = "select count(distinct \"nodeid\") from trafficlight where \"nodeid\" in ("+s1+")";	// important: only count distinct trafficlights, there may be >1 at one node!
		Query res = session.createSQLQuery(s);
		BigInteger ntl = (BigInteger)res.uniqueResult();
		short nTrafficLights = (ntl == null ? 0 : ntl.shortValue());
		logger.info("Route " + route.getId() + ": " + nTrafficLights + " traffic lights (was " + route.getnTrafficLights() + ")");
		route.setnTrafficLights(nTrafficLights);
		session.update(route);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query result;
		String query = "from ResultRoute";
		query += " ORDER BY sourcerouteid";
		result = session.createQuery(query);
		int nTot = result.list().size();
		logger.info(nTot + " routes to fix");
		@SuppressWarnings("unchecked")
		Iterator<ResultRoute> iterator = result.iterate();
		int n = 0;
		while (iterator.hasNext()) {
			n++;
			ResultRoute route = iterator.next();
			fixRoute(route);
			if (n % batchSize == 0) {
				session.flush();
				session.clear();
				System.out.print(100.*(double)n/(double)nTot + "% ...\n");
			}
		}
		session.getTransaction().commit();
		logger.info("Finished!");
	}

}
