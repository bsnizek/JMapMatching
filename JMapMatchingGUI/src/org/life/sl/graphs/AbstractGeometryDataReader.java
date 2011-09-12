package org.life.sl.graphs;


import java.io.IOException;

/**
 * @author Pimin Konstantin Kefaloukos
 *
 */
public abstract class AbstractGeometryDataReader {

		protected String shapeFile = null;
		protected String dbfFile = null;
		protected String basefile = null;

		/**
		 * Make a new LineStringDataReader.
		 */
		public AbstractGeometryDataReader(String shp) {
			shapeFile = shp;
			dbfFile = shp.substring(0, shp.length()-4) + ".dbf";
			basefile = shp.substring(0, shp.length()-4);
		}
		

		public abstract void read() throws IOException; 

}
