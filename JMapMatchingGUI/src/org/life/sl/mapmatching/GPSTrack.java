package org.life.sl.mapmatching;

import java.util.ArrayList;
import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.Session;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.SourcePoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * Class storing the GPS data of a measured track.
 * Subclass of an ArrayList with additional functionality regarding the track.
 * @author bb
 *
 */
public class GPSTrack extends ArrayList<Point> {

	private static final long serialVersionUID = 7848737559208241849L;	///< serial version ID required due to descendance from ArrayList (?)

	private double trackLength;
	private double avgDist = 0;
	private double minDist = 0;
	private double maxDist = 0;
	private boolean isDirty = true;	///< indicates if data has been modified since last statistics calculation
	
	/**
	 * Create a GPSTrack from an ArrayList of Points (e.g., when reading from a shapefile)
	 * @param pointList list of GPS points
	 */
	public GPSTrack(ArrayList<Point> pointList) {
		super(pointList);
		markDirty();
	}
	
	/**
	 * Create a GPSTrack from source route data in the database
	 * @param sourceRouteID database ID of source route
	 */
	public GPSTrack(int sourceRouteID) {
		super();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query result = session.createQuery("from SourcePoint WHERE sourcerouteid=" + sourceRouteID + " order by id");
		@SuppressWarnings("unchecked")
		Iterator<SourcePoint> iter = result.iterate();
		while (iter.hasNext()) {	// create list from query results
			SourcePoint sp = (SourcePoint) iter.next();
			Point o = sp.getGeometry();
			add(o);
		}
		session.close();
		markDirty();
	}
	
	private void markDirty() {
		isDirty = true;
	}
	
	public boolean add(Point o) {
		boolean b = super.add(o);
		if (b) markDirty();
		return b;
	}
	
	/**
	 * convenience method to get the coordinates of a specified list item
	 * @param i index of the Point; i < 0 startst from the end of the list (-1 is the last item)
	 * @return Coordinate of the point
	 */
	public Coordinate getCoordinate(int i) {
		if (i < 0) i = size() + i;	// -1 maps to the last item
		return get(i).getCoordinate();
	}
	
	/**
	 * calculate some statistics on the track:
	 * Euclidian path length, minimum and maximum distance between subsequent points
	 */
	public void calcStatistics() {
		if (isDirty) {
			double d = 0;
			trackLength = 0;
			avgDist = 0;
			minDist = Double.MAX_VALUE;
			maxDist = 0;
			Point p0 = null;
			for (Point p : this) {
				if (p0 != null) {
					d = p.distance(p0);
					trackLength += d;
					minDist = Math.min(minDist, d);
					maxDist = Math.max(maxDist, d);
				}
				p0 = p;
			}
			avgDist = trackLength / size();
			isDirty = false;
		}
	}

	/**
	 * calculate the Euclidian path length as sum of the Euclidian distances between subsequent measurement points
	 * @return the path length along the GPS points
	 */
	public double getTrackLength() {
		calcStatistics();
		return trackLength;
	}

	public double getAvgDist() {
		calcStatistics();
		return avgDist;
	}

	public double getMinDist() {
		calcStatistics();
		return minDist;
	}

	public double getMaxDist() {
		calcStatistics();
		return maxDist;
	}
}
