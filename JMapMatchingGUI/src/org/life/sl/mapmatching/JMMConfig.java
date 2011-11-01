package org.life.sl.mapmatching;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
	public int nRoutesToWrite = 10;
	public boolean bWriteChoices = true;
	public boolean bWriteToShapefiles = false;
	public boolean bWriteToDatabase = true;
	public boolean bSelectRandomRoutes = true;
	
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
			if (iniMap.containsKey("WriteChoices")) bWriteChoices = Boolean.parseBoolean(iniMap.get("WriteChoices"));
			if (iniMap.containsKey("WriteToShapefiles")) bWriteToShapefiles = Boolean.parseBoolean(iniMap.get("WriteToShapefiles"));
			if (iniMap.containsKey("WriteToDatabase")) bWriteToDatabase = Boolean.parseBoolean(iniMap.get("WriteToDatabase"));
			if (iniMap.containsKey("SelectRandomRoutes")) bSelectRandomRoutes = Boolean.parseBoolean(iniMap.get("SelectRandomRoutes"));
		} catch (InvalidFileFormatException e) {
			Logger.getRootLogger().error("Invalid file format");
		} catch (IOException e) {
			Logger.getRootLogger().error("Error reading ini file - " + e);
		}
		return r;
	}
}
