package org.life.sl.tools;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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

	public static void exportRoute(ResultRoute route) throws SchemaException, IOException {
		logger.info("Exporting route id " + route.getId() + " (sourcerouteID " + route.getSourceRouteID() + ")");
		
		LineString ls = route.getGeometry();
		String filename = kOutputDir + "route" + route.getId() + ".shp";
		File newFile = new File(filename);
		exportLineStringToShapeFile(newFile, ls);
	}
	
	public static void exportLineStringToShapeFile(File newFile, LineString ls) throws SchemaException, IOException {
		final SimpleFeatureType TYPE = DataUtilities.createType("route",
				"geom:LineString,srid="+ls.getSRID()
				);
		
		ArrayList<SimpleFeature> features = new ArrayList<SimpleFeature>();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		SimpleFeature feature = featureBuilder.buildFeature(null);	
		feature.setDefaultGeometry(ls);
		features.add(feature);
		SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);//FeatureCollections.newCollection();

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", newFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(TYPE);
 
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
		while (iterator.hasNext()) {
			n++;
			ResultRoute route = iterator.next();
			try {
				exportRoute(route);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			timer.showProgress((double)n/nTot);
		}
		session.getTransaction().commit();
		logger.info("Finished! (" + timer.getRunTime(true) + "s)");
	}

}
