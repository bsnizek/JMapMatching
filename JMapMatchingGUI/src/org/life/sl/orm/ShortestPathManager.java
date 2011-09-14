package org.life.sl.orm;

import org.hibernate.Session;

public class ShortestPathManager {
	
	public static void main(String[] args) {
		
		ShortestPathManager mgr = new ShortestPathManager();
		
		if (args[0].equals("store")) {
			mgr.createAndStoreShortestPath(1, 2, 2.3);
		}
		
	}

	private void createAndStoreShortestPath(int i, int j, double d) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		ShortestPathLength sp = new ShortestPathLength();
		sp.setFromnode(i);
		sp.setTonode(j);
		sp.setLength(d);
		
		session.save(sp);
		
		session.getTransaction().commit();
		
	}
	

}
