package org.life.sl.tools;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.hibernate.Query;
import org.hibernate.Session;
import org.life.sl.orm.HibernateUtil;
import org.life.sl.orm.ResultRoute;
import org.life.sl.utils.Timer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.LineString;

public class RouteDBExporter {
	private static Logger logger = Logger.getLogger("RouteExport");

	private static String kOutputDir = "results/";

	public static void exportRoutes(ArrayList<ResultRoute> routes, int srID) throws SchemaException, IOException {
		HashMap<Integer, LineString> lsa = new HashMap<Integer, LineString>();
		for (ResultRoute route : routes) {
			logger.info("Exporting route id " + route.getId() + " (sourcerouteID " + route.getSourceRouteID() + ")");
			lsa.put(route.getId(), route.getGeometry());
		}
		if (srID <= 0) srID = routes.get(0).getSourceRouteID();
		String filename = kOutputDir + "route" + srID + ".shp";
		File newFile = new File(filename);
		exportLineStringsToShapeFile(newFile, lsa, srID);
	}
	
	public static void exportLineStringToShapeFile(File newFile, LineString ls, int sourceRouteID) throws SchemaException, IOException {
		HashMap<Integer, LineString> lsa = new HashMap<Integer, LineString>();
		lsa.put(1, ls);
		exportLineStringsToShapeFile(newFile, lsa, sourceRouteID);
	}
	
	public static void exportLineStringsToShapeFile(File newFile, HashMap<Integer, LineString> lsa, int sourceRouteID) throws SchemaException, IOException {
		final SimpleFeatureType TYPE = DataUtilities.createType("route",
				"geom:LineString,srid:0,id:0"
				);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", newFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(TYPE);
		
		ArrayList<SimpleFeature> features = new ArrayList<SimpleFeature>();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		for (Map.Entry<Integer, LineString> e : lsa.entrySet()) {
//			System.out.println(ls);
			Object[] values = { 0, sourceRouteID , e.getKey() };	// e = <id, ls> 
			SimpleFeature feature = featureBuilder.buildFeature(null, values);
			feature.setDefaultGeometry(e.getValue());
			features.add(feature);
		}
		SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);//FeatureCollections.newCollection();

        // You can comment out this line if you are using the createFeatureType method (at end of
        // class file) rather than DataUtilities.createType
        newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
		
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        
        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
            // System.exit(0); // success!
            newDataStore.dispose();
        } else {
            System.out.println(typeName + " does not support read/write access");
            System.exit(1);	// exit program with status 1 (error)
        }
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		// read parameters from console (stdin):
		String s;
		Scanner scn = new Scanner(System.in);
		// choose sourceroutes or routes:
		boolean doSourceRoutes = false;
		System.out.print("Convert routeIDs or sourceRouteIDs? [Rs] ");
		if ((s = scn.nextLine()).length() > 0) {
			doSourceRoutes = (s.toLowerCase().startsWith("s"));
		}
		String idName = (doSourceRoutes ? "source" : "")+"routeID";
		// choose (source)route IDs:
		ArrayList<Integer> routeIDs = new ArrayList<Integer>(10);
		System.out.print("Enter "+idName+"s (single or sep. by ',', empty to start): ");
		while ((s = scn.nextLine()).length() > 0) {
			for (String rs : s.split(",")) routeIDs.add(Integer.parseInt(rs));
		}
		scn.close();
		
		s = routeIDs.toString();
		logger.info("Converting "+(routeIDs.size() == 0 ? "ALL " : "")+idName+"s " + s);
		Query result;
		String query = "from ResultRoute";
		if (routeIDs.size() > 0) {
			s = s.substring(1, s.length()-1);
			query += " WHERE "+(doSourceRoutes ? "sourcerouteID" : "id")+" IN ("+s+")";
		}
		query += " ORDER BY sourcerouteid,id";

		result = session.createQuery(query);
		double nTot = result.list().size();
		logger.info(nTot + " routes to export");
		@SuppressWarnings("unchecked")
		Iterator<ResultRoute> iterator = result.iterate();
		int n = 0;
		Timer timer = new Timer();
		timer.init();	// initialize timer
		int srID = -1;
		ArrayList<ResultRoute> routes = new ArrayList<ResultRoute>();
		while (iterator.hasNext()) {
			n++;
			ResultRoute route = iterator.next();
			if ((srID > 0 && route.getSourceRouteID() != srID) || !iterator.hasNext()) {	// new srID, or end of record set
				try {
					exportRoutes(routes, srID);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			routes.add(route);
			srID = route.getSourceRouteID();
			timer.showProgress((double)n/nTot);
		}
		session.getTransaction().commit();
		logger.info("Finished! (" + timer.getRunTime(true) + "s)");
	}

}
