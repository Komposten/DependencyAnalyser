package komposten.analyser.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import komposten.analyser.backend.parsers.DependencyParser;
import komposten.analyser.backend.parsers.SourceParser;
import komposten.analyser.backend.parsers.UnitParser;
import komposten.analyser.backend.util.Constants;
import komposten.analyser.backend.util.SourceUtil;
import komposten.analyser.tools.Settings;
import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;

public class PackageAnalyser
{
	private Settings settings;
	
	private PackageData currentPackage;
	private List<PackageData> internalPackages;
	
	private List<SourceParser> parsers;
	
	
	public PackageAnalyser(Settings settings)
	{
		this.settings = settings;
		this.parsers = new ArrayList<>();
	}
	
	
	/**
	 * Analyses the specified package. All source files in the PackageData object
	 * will be analysed and the data that is retrieved will be stored in the
	 * PackageData object.
	 * 
	 * @param packageData A PackageData object representing a package to analyse.
	 * @param internalPackages A list of all packages in the source set that is
	 *          analysed. This is used to match references against to see if they
	 *          refer to external or internal classes.
	 */
	public void analysePackage(PackageData packageData, List<PackageData> internalPackages)
	{
		this.currentPackage = packageData;
		this.internalPackages = internalPackages;
		
		createParsers();
		
		for (File sourceFile : packageData.getCompilationUnits())
		{
			analyseFile(sourceFile);
		}
		
		for (SourceParser parser : parsers)
		{
			parser.storeResult(packageData);
		}
	}
	

	private void createParsers()
	{
		parsers.clear();
		parsers.add(new DependencyParser(currentPackage, internalPackages));
		parsers.add(new UnitParser(settings));
	}
	

	private void analyseFile(File file)
	{
		for (SourceParser parser : parsers)
		{
			parser.nextFile(file);
		}
		
		int lineNo = 0;
		boolean analyseStrings = settings.getBoolean(Constants.SettingKeys.ANALYSE_STRINGS);
		boolean analyseComments = settings.getBoolean(Constants.SettingKeys.ANALYSE_COMMENTS);
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			boolean lastEndedInComment = false;
			String line = "";
			String strippedLine = "";
			
			while ((line = reader.readLine()) != null)
			{
				lineNo++;
				line = line.trim();

				if (!line.isEmpty())
				{
					StringBuilder builder = new StringBuilder(line);
					boolean lineEndsInComment = SourceUtil.removeComments(builder, lastEndedInComment, analyseStrings, analyseComments);

					if (!analyseComments && !analyseStrings)
					{
						strippedLine = builder.toString();
					}
					else
					{
						StringBuilder builder2 = new StringBuilder(line);
						SourceUtil.removeComments(builder2, lastEndedInComment);
						strippedLine = builder2.toString();
					}

					line = builder.toString();
					lastEndedInComment = lineEndsInComment;
				}
				else
				{
					strippedLine = line;
				}

				for (SourceParser parser : parsers)
				{
					parser.parseLine(line, strippedLine);
				}
			}
			
			for (SourceParser parser : parsers)
			{
				parser.postFile();
			}
		}
		catch (IOException e)
		{
			String msg = "En unexpected exception occured when reading the file \"" + file + "\"!";
			LogUtils.log(Level.ERROR, PackageAnalyser.class.getSimpleName(), msg, e, false);
		}
		catch (IllegalArgumentException e)
		{
			String msg = file + ":" + lineNo + " contains an un-closed string, so it could not be fully analysed.";
			LogUtils.log(Level.WARNING, msg);
		}
	}
}
