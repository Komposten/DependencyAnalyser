package komposten.analyser.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import komposten.analyser.backend.util.SourceUtil;
import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;
import komposten.utilities.tools.Regex;

public class PackageAnalyser
{
	private List<PackageData> externalPackages;
	
	public PackageAnalyser()
	{
		externalPackages = new ArrayList<>();
	}
	
	/*
	 * NEXT_TASK ?: Re-factor PackageAnalyser:
	 * 
	 * 1) Add analysePackage(PackageData). It should e.g. get the dependencies and assign them to packageData.dependencies.
	 * 2) Add a new method that loops through all lines in all files in packageData.sourceFiles.
	 * 	a) For every line in a file, first clean it (i.e. remove comments and strings).
	 * 	b) Then pass the line to information parsers. E.g. for references, class or method starts, etc.
	 * 	c) When a file end is reached, collect the information from each parser and store it in the PackageData object.
	 * 
	 * E.g.:
	 * For every file X:
	 * 	1) Inititalise the parsers (by creating new or resetting).
	 * 	2) For every line in file X:
	 * 			- Run DependencyParser.parse(line)
	 *			- Run ClassParser.parse(line) //Which class we are in, and how long it is.
	 *			- Run MethodParser.parse(line) //Which method we are in, and how long it is.
	 *			- Run ComplexityParser.parse(line) //Needs to know what class and method we are in. Can I get this info from the previous two parsers somehow?
	 *			- Run AbstractionParser.parse(line) //Abstract vs. concrete classes.
	 *			- Run AccessibilityParser.parse(line) //Private vs. protected vs. package vs. public.
	 */
	

	public Dependency[] getPackageDependencies(PackageData data, List<PackageData> internalPackages)
	{
		Map<PackageData, Dependency> dependencies = new HashMap<>();
		
		for (File sourceFile : data.sourceFiles)
		{
			List<String> classReferences = findDependenciesInFile(sourceFile);
			//removeExternalDependencies(references, internalPackages);

			Collections.sort(classReferences);
			Map<String, String[]> referencesByPackage = new HashMap<>();
			ArrayList<String> currentClassReferences = new ArrayList<>();
			
			//Split the references by package.
			for (int i = 0; i < classReferences.size(); i++)
			{
				boolean isLastElement = (i == classReferences.size() - 1);
				String classReference = classReferences.get(i);
				String packageReference = getPackageName(classReference);
				String nextPackageReference = (!isLastElement ? getPackageName(classReferences.get(i+1)) : null);
				
				currentClassReferences.add(classReference);
				if (isLastElement || !packageReference.equals(nextPackageReference))
				{
					referencesByPackage.put(packageReference, currentClassReferences.toArray(new String[currentClassReferences.size()]));
					currentClassReferences.clear();
				}
			}
			
			//Go through all referenced packages and create Dependencies to them.
			for (Entry<String, String[]> entry : referencesByPackage.entrySet())
			{
				String packageName = entry.getKey();
				String[] classNames = entry.getValue();
				PackageData packageData = getPackageDataFromName(packageName, internalPackages);

				// Skip self-references (i.e. loops) since they are misleading.
				// The only occur if a file refers to its own package by its fully qualified name,
				// and it's enough that a single file points to another file for a loop to appear
				// (i.e. p.A points to p.B, but p.B does not point back).
				if (packageData.equals(data))
					continue;
				
				Dependency dependency = dependencies.get(packageData);
				
				if (dependency == null)
				{
					dependency = new Dependency(packageData, data);
					dependencies.put(packageData, dependency);
				}
				
				dependency.addClass(sourceFile, classNames);
			}
		}
		
		Dependency[] dependencyArray = dependencies.values().toArray(new Dependency[dependencies.size()]);
		Arrays.sort(dependencyArray, new Comparator<Dependency>()
		{
			@Override
			public int compare(Dependency o1, Dependency o2)
			{
				return o1.target.fullName.compareTo(o2.target.fullName);
			}
		});
		
		return dependencyArray;
	}


	private List<String> findDependenciesInFile(File sourceFile)
	{
		ArrayList<String> dependencies = new ArrayList<String>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile)))
		{
			boolean lastEndedInComment = false;
			String line = "";
			
			int lineNo = 0;
			while ((line = reader.readLine()) != null)
			{
				lineNo++;
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				StringBuilder builder = new StringBuilder(line);
				try
				{
					lastEndedInComment = SourceUtil.removeComments(builder, lastEndedInComment, false, false);
				}
				catch (IllegalArgumentException e)
				{
					String msg = sourceFile + ":" + lineNo + " contains an un-closed string, so it could not be fully analysed.";
					if (LogUtils.hasInitialised())
						LogUtils.log(Level.WARNING, msg);
					else
						System.err.println(msg);
					break;
				}
				
				line = builder.toString();
				String[] dependenciesOnLine = Regex.getMatches("\\b([a-z0-9_]+\\.)+(([A-Z_$][\\w$]*)|\\*)", line);
				
				for (String dependency : dependenciesOnLine)
				{
					if (!dependencies.contains(dependency))
						dependencies.add(dependency);
				}
			}
		}
		catch (IOException e)
		{
			String msg = "En unexpected exception occured when reading the file \"" + sourceFile + "\"!";
			if (LogUtils.hasInitialised())
				LogUtils.log(Level.ERROR, PackageAnalyser.class.getSimpleName(), msg, e, false);
			else
				System.err.println(msg);
		}
		
		return dependencies;
	}


	private PackageData getPackageDataFromName(String packageName, List<PackageData> internalPackages)
	{
		for (PackageData packageData : internalPackages)
		{
			if (packageData.fullName.equals(packageName))
				return packageData;
		}
		
		for (PackageData packageData : externalPackages)
		{
			if (packageData.fullName.equals(packageName))
				return packageData;
		}
		
		PackageData packageData = new PackageData(packageName);
		packageData.isExternal = true;
		
		externalPackages.add(packageData);
		
		return packageData;
	}


	private String getPackageName(String dependency)
	{
		String packageName = dependency.substring(0, dependency.lastIndexOf('.'));
		return packageName;
	}
}
