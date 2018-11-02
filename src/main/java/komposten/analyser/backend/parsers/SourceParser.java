package komposten.analyser.backend.parsers;

import java.io.File;

import komposten.analyser.backend.PackageData;


public interface SourceParser
{
	/**
	 * Prepares the parser for the next file.
	 * 
	 * @param file
	 */
	public void nextFile(File file);


	/**
	 * Causes the parser to parse the specified string.
	 * 
	 * @param line
	 */
	public void parseLine(String line);


	/**
	 * Tells the parser that the end of the current file has been reached.
	 */
	public void postFile();


	/**
	 * Tells the parser to take all data it has collected and store it in the
	 * provided PackageData object.
	 * 
	 * @param packageData
	 */
	public void storeResult(PackageData packageData);
}
