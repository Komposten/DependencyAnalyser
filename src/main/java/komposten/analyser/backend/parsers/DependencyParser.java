package komposten.analyser.backend.parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import komposten.analyser.backend.Dependency;
import komposten.analyser.backend.PackageData;

public class DependencyParser implements SourceParser
{
	private static final Pattern PATTERN;
	private final Matcher matcher;
	
	private PackageData sourcePackage;
	private List<PackageData> internalPackages;
	private List<PackageData> externalPackages;
	private List<String> dependenciesInFile;
	private File sourceFile;
	
	private Map<PackageData, Dependency> allDependenciesByTarget;
	
	static
	{
		 PATTERN = Pattern.compile("\\b([a-z0-9_]+\\.)+(([A-Z_$][\\w$]*)|\\*)");
	}
	

	public DependencyParser(PackageData sourcePackage, List<PackageData> internalPackages)
	{
		this.sourcePackage = sourcePackage;
		this.internalPackages = internalPackages;
		this.externalPackages = new LinkedList<>();
		this.dependenciesInFile = new LinkedList<>();
		this.allDependenciesByTarget = new HashMap<>();

		this.matcher = PATTERN.matcher("");
	}
	
	
	@Override
	public void nextFile(File file)
	{
		sourceFile = file;
		dependenciesInFile.clear();
	}


	@Override
	public void parseLine(String line)
	{
		List<String> dependenciesOnLine = getReferencesOnLine(line);
		
		for (String dependency : dependenciesOnLine)
		{
			if (!dependenciesInFile.contains(dependency))
				dependenciesInFile.add(dependency);
		}
	}
	
	
	private List<String> getReferencesOnLine(String line)
	{
		List<String> matches = new LinkedList<>();
		matcher.reset(line);
		
		while (matcher.find())
	    matches.add(matcher.group());
	  
	  return matches;
	}
	
	
	@Override
	public void postFile()
	{
		Collections.sort(dependenciesInFile);
		
		Map<String, String[]> referencesByPackage = splitReferencesByPackage();
		Collection<Dependency> dependencies = createDependenciesFromReferences(referencesByPackage);
		
		for (Dependency dependency : dependencies)
		{
			if (!allDependenciesByTarget.containsKey(dependency.target))
				allDependenciesByTarget.put(dependency.target, dependency);
		}
	}


	private Map<String, String[]> splitReferencesByPackage()
	{
		Map<String, String[]> referencesByPackage = new HashMap<>();
		
		ArrayList<String> currentClassReferences = new ArrayList<>();
		for (int i = 0; i < dependenciesInFile.size(); i++)
		{
			boolean isLastElement = (i == dependenciesInFile.size() - 1);
			String classReference = dependenciesInFile.get(i);
			String packageReference = getPackageName(classReference);
			String nextPackageReference = (!isLastElement ? getPackageName(dependenciesInFile.get(i+1)) : null);
			
			currentClassReferences.add(classReference);
			if (isLastElement || !packageReference.equals(nextPackageReference))
			{
				referencesByPackage.put(packageReference, currentClassReferences.toArray(new String[currentClassReferences.size()]));
				currentClassReferences.clear();
			}
		}
		
		return referencesByPackage;
	}


	private Collection<Dependency> createDependenciesFromReferences(Map<String, String[]> referencesByPackage)
	{
		Map<PackageData, Dependency> dependenciesByTarget = new HashMap<>();
		for (Entry<String, String[]> entry : referencesByPackage.entrySet())
		{
			String targetPackageName = entry.getKey();
			String[] targetClassNames = entry.getValue();
			PackageData targetPackage = getPackageDataFromName(targetPackageName);

			// Skip self-references (i.e. loops) since they are misleading.
			// The only occur if a file refers to its own package by its fully qualified name,
			// and it's enough that a single file points to another file for a loop to appear
			// (i.e. p.A points to p.B, but p.B does not point back).
			if (targetPackage.equals(sourcePackage))
				continue;
			
			Dependency dependency = dependenciesByTarget.get(targetPackage);
			if (dependency == null)
			{
				dependency = allDependenciesByTarget.get(targetPackage);
				
				if (dependency == null)
				{
					dependency = sourcePackage.getDependencyForPackage(targetPackage);
					
					if (dependency == null)
					{
						dependency = new Dependency(targetPackage, sourcePackage);
					}
				}
				
				dependenciesByTarget.put(targetPackage, dependency);
			}
			
			dependency.addClass(sourceFile, targetClassNames);
		}
		
		return dependenciesByTarget.values();
	}


	private PackageData getPackageDataFromName(String packageName)
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
		
		return createExternalPackage(packageName);
	}


	private PackageData createExternalPackage(String packageName)
	{
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


	@Override
	public void storeResult(PackageData packageData)
	{
		Dependency[] array = new Dependency[allDependenciesByTarget.size()];
		packageData.dependencies = allDependenciesByTarget.values().toArray(array);
		
		Arrays.sort(packageData.dependencies, new Comparator<Dependency>()
		{
			@Override
			public int compare(Dependency o1, Dependency o2)
			{
				return o1.target.fullName.compareTo(o2.target.fullName);
			}
		});
	}
}
