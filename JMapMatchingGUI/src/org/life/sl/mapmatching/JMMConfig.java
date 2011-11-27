package org.life.sl.mapmatching;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

/**
 * Container for configuration data for the JMapMatching program, 
 * including functionality to read from ini-file 
 * @author Bernhard Barkow
 * @see org.life.sl.routefinder.RFParams
 * @see <a href="https://github.com/bsnizek/JMapMatching/wiki/JMapMatcherIniFile">JMapMatcherIniFile in the Wiki</a>
 */
public class JMMConfig {
	// configuration parameters and default values:
	public int nRoutesToWrite = 10;
	public boolean bWriteChoices = true;
	public int iFixedNodeChoices = 0;
	public boolean bWriteTrafficLights = true;
	public boolean bWriteToShapefiles = false;
	public boolean bWriteToDatabase = true;
	public boolean bRandomSelect = true;
	public int iWriteNBest = 10;
	public int iWriteNWorst = 1;
	public Level logLevel = Level.INFO;
	public boolean bDumpNetwork = false;
	public String sDumpNetworkDir = "tmp";
	public String sourcerouteIDs = "";				// default: -1 = none
	public double graphSnapDistance = 0.1;
	
	public JMMConfig() {}
	
	/**
	 * constructor initializing the object from a config file
	 * @param iniFileName
	 */
	public JMMConfig(String iniFileName) {
		readFromFile(iniFileName);
	}
	/**
	 * read parameters from a given ini-File (Win-Ini format)
	 * @param iniFileName name of the configuration file 
	 * @return number of parameters read from the file; -1 if the file could not be opened
	 */
	public int readFromFile(String iniFileName) {
		Logger.getRootLogger().info("Reading ini file " + iniFileName);
		int r = -1;	// default: no file found
		try {
			Ini ini = new Ini(new File(iniFileName));
			Map<String, String> iniMap = ini.get("Output");
			
			if (iniMap.containsKey("RoutesToWrite")) nRoutesToWrite = Integer.parseInt(iniMap.get("RoutesToWrite"));
			if (iniMap.containsKey("RandomSelect")) bRandomSelect = Boolean.parseBoolean(iniMap.get("RandomSelect"));
			if (iniMap.containsKey("WriteNBest")) iWriteNBest = Integer.parseInt(iniMap.get("WriteNBest"));
			if (iniMap.containsKey("WriteNWorst")) iWriteNWorst = Integer.parseInt(iniMap.get("WriteNWorst"));
			if (iniMap.containsKey("WriteChoices")) bWriteChoices = Boolean.parseBoolean(iniMap.get("WriteChoices"));
			if (iniMap.containsKey("WriteTrafficLights")) bWriteTrafficLights = Boolean.parseBoolean(iniMap.get("WriteTrafficLights"));
			if (iniMap.containsKey("FixedNodeChoices")) iFixedNodeChoices = Integer.parseInt(iniMap.get("FixedNodeChoices"));
			if (iniMap.containsKey("WriteToShapefiles")) bWriteToShapefiles = Boolean.parseBoolean(iniMap.get("WriteToShapefiles"));
			if (iniMap.containsKey("WriteToDatabase")) bWriteToDatabase = Boolean.parseBoolean(iniMap.get("WriteToDatabase"));
			if (iniMap.containsKey("LogLevel")) logLevel = Level.toLevel(iniMap.get("LogLevel"));
			if (iniMap.containsKey("DumpNetwork")) bDumpNetwork = Boolean.parseBoolean(iniMap.get("DumpNetwork"));
			if (iniMap.containsKey("DumpNetworkDir")) sDumpNetworkDir = iniMap.get("DumpNetworkDir").trim();

			iniMap = ini.get("Input");
			if (iniMap.containsKey("sourcerouteIDs")) sourcerouteIDs = iniMap.get("sourcerouteIDs");
			if (iniMap.containsKey("GraphSnapDistance")) graphSnapDistance = Double.parseDouble(iniMap.get("GraphSnapDistance"));
		} catch (InvalidFileFormatException e) {
			Logger.getRootLogger().error("Invalid file format");
		} catch (IOException e) {
			Logger.getRootLogger().error("Error reading ini file - " + e);
		}
		return r;
	}
}
