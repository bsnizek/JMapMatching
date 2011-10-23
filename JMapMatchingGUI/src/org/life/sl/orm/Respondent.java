package org.life.sl.orm;

import org.apache.log4j.Logger;
import org.hibernate.Session;

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

public class Respondent {
	
	/**
	 * create a respondent associated with a source route
	 * @param sourcerouteID database ID of the source route
	 * @return the respondent for the source route
	 */
	static public Respondent getForSourceRouteID(int sourcerouteID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Respondent resp = null;
		try {
			SourceRoute sr = (SourceRoute) session.load(SourceRoute.class, sourcerouteID);
			resp = (Respondent) session.load(Respondent.class, sr.getRespondentid());
		} catch(Exception e) {
			Logger.getRootLogger().error("Could not find respondent for sourcerouteID " + sourcerouteID + " - " + e.toString());
		}
		session.getTransaction().commit();
		//session.close();
		return resp;
	}

	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
    