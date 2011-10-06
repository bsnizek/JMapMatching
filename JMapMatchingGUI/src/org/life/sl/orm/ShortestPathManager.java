package org.life.sl.orm;

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
