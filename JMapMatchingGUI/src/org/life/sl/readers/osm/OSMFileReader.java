package org.life.sl.readers.osm;

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.life.sl.graphs.PathSegmentGraph;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.Epsg4326;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Bernhard Snizek <besn@life.ku.dk>
 *
 */
public class OSMFileReader {

	private static PathSegmentGraph psg;

	public OSMFileReader() {
		// initialize the geometry factory
		psg = new PathSegmentGraph();
	}

	/**
	 * loads an OSM File and builds up the road Network (Path Segmented Graph)
	 * 
	 * @param osmFileName : OSM File Name as a String
	 * @throws FileNotFoundException
	 * @throws IllegalDataException
	 * 
	 */
	public void readOSMFile(String osmFileName)  throws FileNotFoundException, IllegalDataException {

		Main.pref = new Preferences();
		Main.proj = new Epsg4326();		
		Main.pref.put("tags.direction", false);

		DataSet dsRestriction = OsmReader.parseDataSet(new FileInputStream(osmFileName), null);

		Collection<Way> ways = dsRestriction.getWays();

		for (Way way : ways) {
			if (way.get("highway") != null) {
				if (way.get("highway").equals("residential")) {

					String roadName = way.getName();
					System.out.println(roadName);
					
					List<Node> nodes = way.getNodes();
					
					Coordinate[] array1 = new Coordinate[nodes.size()];
					
					int counter = 0;
					
					for (Node node : nodes) {
						LatLon ll = node.getCoor();
						Coordinate c = new Coordinate(ll.lat(), ll.lon()); // z = 0, no elevation	
						array1[counter] = c;
						counter = counter +1;
					
					}
					com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
					com.vividsolutions.jts.geom.LineString lineString = fact.createLineString(array1);
					
					HashMap<String, Object> hm = new HashMap<String, Object>();
					
					hm.put("roadname", roadName);
					hm.put("geometry", lineString);
					lineString.setUserData(hm);
					
					psg.addLineString(lineString, (int) way.getId());
				}
			}
		}
	}



	public void readOnline() {

	}

	public PathSegmentGraph asLineMergeGraph() {
		return psg;

	}
	

//	/**
//	 * @param args
//	 * @throws FileNotFoundException
//	 * @throws IllegalDataException
//	 */
//	public static void main(String[] args) throws FileNotFoundException, IllegalDataException {
//		String filename = "testdata/testnet.osm";
//		OSMFileReader osm_reader = new OSMFileReader();
//		osm_reader.readOSMFile(filename);
//		PathSegmentGraph psg = osm_reader.asLineMergeGraph();
//	}

}
