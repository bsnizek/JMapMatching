package org.life.sl.routefinder;

import java.io.File;
import java.io.FileWriter;

/**
 * Container for statistics on the GPS track and the map matching process
 * @author bb
 *
 */
public class MatchStats {
	public enum Status { OK, NOROUTES, MAXLABELS, MAXROUTES, MEMORY, EMPTYTRACK };
	
	public int sourceRouteID = 0;
	
	public int network_edges = 0;
	public int network_nodes = 0;
	public double network_meanDegree = 0;
	public int network_maxDegree = 0;
	public int network_minDegree = 0;
	public double network_maxRoutesEst = 0;
	
	public double trackLength = 0.;
	public double ODDist = 0.;
	public double maxLength = 0.;
	public long nLabels = 0;
	public long nRoutes = 0;
	public long nDeadEnds = 0;
	public long nRejected_length = 0;
	public long nRejected_overlap = 0;

	public double runTime = 0.;
	public Status status = Status.OK;
	
	public MatchStats() {}
	public MatchStats(int sourceRouteID, Status status) {
		this.sourceRouteID = sourceRouteID;
		this.status = status;
	}
	
	public void save(String fn, boolean writeHeader) {
		try {
			File f = new File(fn);
			FileWriter fw = new FileWriter(f, true);	// true = append
			
			if (writeHeader) {
				fw.write("# sourceID\tedges\tnodes\tmeanDegree\tmaxRoutes\ttrackLen\tODDist\tmaxLen\tnLabels\tnRoutes\tnDeadEnds\tnRej_length\tnRej_overlap\trunTime\tstatus\n");
			}
			String s = String.format("%d\t%d\t%d\t%2.3f\t%2.3g\t", sourceRouteID, network_edges, network_nodes, network_meanDegree, network_maxRoutesEst); 
			s += String.format("%2.3f\t%2.3f\t%2.3f\t%d\t%d\t%d\t%d\t%d\t", trackLength, ODDist, maxLength, nLabels, nRoutes, nDeadEnds, nRejected_length, nRejected_overlap); 
			s += String.format("%2.3f\t%d\n", runTime, status.ordinal()); 
			
			fw.write(s);
			fw.close();
		} catch (Exception e) {
			System.err.println("Error saving statistics: " + e);
		}
	}
}
