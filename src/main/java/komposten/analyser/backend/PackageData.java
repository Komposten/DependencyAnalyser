package komposten.analyser.backend;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import komposten.analyser.backend.GraphCycleFinder.GraphNode;

/**
 * A class used by {@link Analyser} to store data about a package and its dependencies. 
 * @author Jakob Hjelm
 *
 */
public class PackageData implements GraphCycleFinder.GraphNode, Serializable, Vertex
{
	public final File folder;
	public final String fullName;
	
	public File[] sourceFiles;
	public Dependency[] dependencies;
	public boolean isExternal;
	public boolean isInCycle;
	public List<Cycle> cycles;
	
	
	{
		this.dependencies = new Dependency[0];
		this.cycles = new ArrayList<>();
	}
	
	
	public PackageData(String fullName)
	{
		this.fullName = fullName;
		this.folder = null;
	}
	
	
	public PackageData(String fullName, File folder, File[] sourceFiles)
	{
		this.folder = folder;
		this.sourceFiles = sourceFiles;
		this.fullName = fullName;
	}
	
	
//	public PackageData(File rootFolder, File folder, File[] sourceFiles)
//	{
//		this.folder = folder;
//		this.sourceFiles = sourceFiles;
//		this.fullName = getFullName(rootFolder, folder);
//	}
	
	
//	private String getFullName(File rootFolder, File folder)
//	{
//		String folderPath = folder.getAbsolutePath();
//		String rootPath = rootFolder.getAbsolutePath();
//		
//		String result = folderPath.replace(rootPath, ""); //Make the path relative to the root folder.
//		result = result.replaceAll("(\\\\|/)", "."); //Replace all '/' or '\' with '.'.
//		result = result.replaceAll("^\\.|\\.$", ""); //Trim away start/end periods.
//		return result;
//	}
	
	
	@Override
	public GraphNode[] getSuccessorNodes()
	{
		GraphNode[] nodes = new GraphNode[dependencies.length];
		for (int i = 0; i < dependencies.length; i++)
			nodes[i] = dependencies[i].target;
		
		return nodes;
	}
	
	
	public Dependency getDependencyForPackage(PackageData thePackage)
	{
		for (Dependency dependency : dependencies)
		{
			if (dependency.target.equals(thePackage))
				return dependency;
		}
		
		return null;
	}
	
	
	@Override
	public int hashCode()
	{
//		Object[] values = new Object[3 + sourceFiles.length + dependencies.length];
//		values[0] = folder;
//		values[1] = fullName;
//		values[2] = isInCycle;
//		System.arraycopy(sourceFiles, 0, values, 3, sourceFiles.length);
//		System.arraycopy(dependencies, 0, values, 3+sourceFiles.length, dependencies.length);
//		return Objects.hash(values);
		return fullName.hashCode();
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		
		if (obj == null)
			return false;
		
		if (obj.getClass() != getClass())
			return false;

		PackageData object = (PackageData) obj;

		if (fullName == null)
		{
			if (object.fullName != null)
				return false;
		}
		else
		{
			if (!fullName.equals(object.fullName))
				return false;
		}
		
//		if (folder == null)
//		{
//			if (object.folder != null)
//				return false;
//		}
//		else
//		{
//			if (!folder.equals(object.folder))
//				return false;
//		}
//
//		if (sourceFiles == null)
//		{
//			if (object.sourceFiles != null)
//				return false;
//		}
//		else
//		{
//			if (!Arrays.equals(sourceFiles, object.sourceFiles))
//				return false;
//		}
//
//		if (dependencies == null)
//		{
//			if (object.dependencies != null)
//				return false;
//		}
//		else
//		{
//			if (!Arrays.equals(dependencies, object.dependencies))
//				return false;
//		}
//		
//		if (isInCycle != object.isInCycle)
//			return false;
		
		return true;
	}
	
	
	@Override
	public String toString()
	{
		return fullName;
	}
}
