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

	private static final boolean kUseSmoothing = true;	// use smoothing for the track (floating average between node coordinates)
	
	private double trackLength;
	private double avgDist = 0;
	private double minDist = 0;
	private double maxDist = 0;
	private boolean isDirty = true;	///< indicates if data has been modified since last statistics calculation
	private int sourceRouteID;
	
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
		this.sourceRouteID = sourceRouteID;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Query result = session.createQuery("from SourcePoint WHERE sourcerouteid=" + sourceRouteID + " order by t");
		System.out.println(result.getQueryString());
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
			
			// test: create new points by using floating averaging in order to reduce noise:
			if (kUseSmoothing) {
				int kAvgCoords = 4;
				trackLength = 0;
				ArrayList<Coordinate> mc = new ArrayList<Coordinate>(this.size());
				double x, y;
				int i0, n = this.size();
				double n0;
				Coordinate c0;
				for (int i = 0; i < n; i++) {
					x = 0; y = 0;
					i0 = Math.max(0, i-kAvgCoords+1);	// first point to use for current average
					n0 = i - i0 + 1.;					// number of points used (<= kAvgCoords)
					for (int j = i0; j <= i; j++) {		// very inefficient, but at least it works...
						c0 = this.get(j).getCoordinate();
						x += c0.x;
						y += c0.y;
					}
					x /= n0;
					y /= n0;
					mc.add(new Coordinate(x, y));
				}
				c0 = null;
				for (Coordinate c : mc) {
					if (c0 != null) trackLength += c.distance(c0);
					c0 = c;
				}
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

	public int getSourceRouteID() {
		return sourceRouteID;
	}

}
