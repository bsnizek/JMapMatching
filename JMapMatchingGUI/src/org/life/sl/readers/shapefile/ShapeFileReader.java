package org.life.sl.readers.shapefile;

import java.io.IOException;

import org.life.sl.graphs.PathSegmentGraph;

//import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;

/**
 * @author besn
 * the ShapeFileReader reads a Shapefile into a PathSegmentGraph
 *
 */
public class ShapeFileReader {
	
	private PathSegmentGraph psg;

	public static void main(String[] args) {

	}

	public void read(String string) throws IOException {
		psg = new PathSegmentGraph();
		
	}

	public PathSegmentGraph asLineMergeGraph() {
		// TODO Auto-generated method stub
		return psg;
		
	}

}
