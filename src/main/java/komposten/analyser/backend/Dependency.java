package komposten.analyser.backend;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	public Map<String, String[]> byCompilationUnit;
	

	public Dependency(PackageData target, PackageData source)
	{
		this.target = target;
		this.source = source;
		this.byCompilationUnit = new HashMap<>();
	}
	
	
	public void addReferences(String sourceUnit, String[] targetUnits)
	{
		byCompilationUnit.put(sourceUnit, targetUnits);
	}
	
	
	/**
	 * Returns a string representation of this Dependency in the following format:
	 * <pre>target[depending compilation units]</pre>.
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
			builder.append("[");
			List<String> units = new LinkedList<>(byCompilationUnit.keySet());
			for (int i = 0; i < units.size(); i++)
			{
				builder.append(units.get(i));
				
				if (i < units.size()-1)
					builder.append(", ");
			}
			builder.append("]");
		}
		
		return builder.toString();
	}
}
