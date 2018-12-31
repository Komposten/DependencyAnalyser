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
	public Map<String, File> classToFileMap;
	public Map<String, String[]> classDependencies;
	

	public Dependency(PackageData target, PackageData source)
	{
		this.target = target;
		this.source = source;
		this.classToFileMap = new HashMap<>();
		this.classDependencies = new HashMap<>();
	}
	
	
	public void addClass(File classFile, String[] classesReferenced)
	{
		String className = source.fullName + "." + FileOperations.getNameWithoutExtension(classFile);
		classDependencies.put(className, classesReferenced);
		classToFileMap.put(className, classFile);
	}
	
	
	/**
	 * Returns a string representation of this Dependency in the following format:
	 * <pre>target[dependingFiles]</pre>.
	 */
	@Override
	public String toString()
	{
		return toString(false, true, true);
	}
	
	
	/**
	 * Returns a string representation of this Dependency in the following format:
	 * <pre>source-->target[dependingFiles]</pre>
	 * @param includeSource If the source should be included in the string.
	 * @param includeTarget If the target should be included in the string.
	 * @param includeDependingFiles If the files in source that depend on target should be included.
	 * @return
	 */
	public String toString(boolean includeSource, boolean includeTarget, boolean includeDependingFiles)
	{
		StringBuilder builder = new StringBuilder();
		
		if (includeSource)
			builder.append(source);
			
		if (includeSource && includeTarget)
			builder.append("-->");
		
		if (includeTarget)
			builder.append(target);
		
		if (includeDependingFiles)
		{
			String[] array = new String[classToFileMap.size()];
			int i = 0;
			for (Map.Entry<String, File> entry : classToFileMap.entrySet())
			{
				array[i] = entry.getValue().getName();
			}
			
			builder.append(Arrays.toString(array));
		}
		
		return builder.toString();
	}
}
