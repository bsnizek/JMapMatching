package org.life.sl.mapmatching;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class JMMConfig {
	public int nRoutesToWrite = 10;
	public boolean bWriteChoices = true;
	
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
		} catch (InvalidFileFormatException e) {
			Logger.getRootLogger().error("Invalid file format");
		} catch (IOException e) {
			Logger.getRootLogger().error("Error reading ini file - " + e);
		}
		return r;
	}
}
