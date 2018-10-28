package komposten.analyser.backend;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import komposten.utilities.tools.FileOperations;

/**
 * A class that describes a directed dependency from one package ({@link #source}) to another ({@link #target}).
 * {@link #filesWithDependency} contains references to all files within <code>owner</code> that references <code>dependency</code>.  
 * @author Jakob Hjelm
 *
 */
public class Dependency implements Serializable
{
	public PackageData target;
	public PackageData source;
	public File[] filesWithDependency;
	public Map<String, String[]> classDependencies;
	

//	public Dependency(PackageData dependency, PackageData owner, File[] filesWithDependency)
//	{
//		this.target = dependency;
//		this.source = owner;
//		this.filesWithDependency = filesWithDependency;
//		this.classDependencies = new HashMap<>();
//	}
	

	public Dependency(PackageData dependency, PackageData owner)
	{
		this.target = dependency;
		this.source = owner;
		this.filesWithDependency = new File[0];
		this.classDependencies = new HashMap<>();
	}
	
	
	void addClass(File classFile, String[] classesReferenced)
	{
		int newLength = filesWithDependency.length + 1;
		
		File[] newArray = Arrays.copyOf(filesWithDependency, newLength);
		newArray[newLength-1] = classFile;
		
		filesWithDependency = newArray;
		
		String className = source.fullName + "." + FileOperations.getNameWithoutExtension(classFile);
		classDependencies.put(className, classesReferenced);
	}
	
	
	@Override
	public String toString()
	{
		return toString(false, true, true);
	}
	
	
	public String toString(boolean includeSource, boolean includeTarget, boolean includeDependendingFiles)
	{
		StringBuilder builder = new StringBuilder();
		
		if (includeSource)
			builder.append(source);
			
		if (includeSource && includeTarget)
			builder.append("-->");
		
		if (includeTarget)
			builder.append(target);
		
		if (includeDependendingFiles)
		{
			builder.append("[");
			for (int i = 0; i < filesWithDependency.length; i++)
			{
				File file = filesWithDependency[i];
				builder.append(file.getName());
				
				if (i < filesWithDependency.length-1)
					builder.append(", ");
			}
			builder.append("]");
		}
		
		return builder.toString();
	}
}
