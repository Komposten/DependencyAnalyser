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
	
	
	public boolean sharesCycleWith(PackageData otherPackage)
	{
		if (!isInCycle || !otherPackage.isInCycle)
			return false;
		
		for (Cycle cycle : cycles)
		{
			if (cycle.contains(otherPackage))
				return true;
		}
		
		return false;
	}
	
	
	@Override
	public int hashCode()
	{
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
		
		return true;
	}
	
	
	@Override
	public String toString()
	{
		return fullName;
	}
}
