package komposten.analyser.gui.views;

import java.io.Serializable;
import java.util.Objects;

import komposten.analyser.backend.Edge;
import komposten.analyser.backend.PackageData;

public class DependencyEdge implements Edge, Serializable
{
	private PackageData source;
	private PackageData target;
	
	
	public DependencyEdge()
	{
	}
	
	
	public DependencyEdge(PackageData source, PackageData target)
	{
		this.source = source;
		this.target = target;
	}
	
	
	@Override
	public PackageData getSource()
	{
		return source;
	}
	
	
	@Override
	public PackageData getTarget()
	{
		return target;
	}
	
	
	@Override
	public int hashCode()
	{
		return Objects.hash(source, target);
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
		
		DependencyEdge object = (DependencyEdge) obj;
		
		if (source == null)
		{
			if (object.source != null)
				return false;
		}
		else
		{
			if (!source.equals(object.source))
				return false;
		}
		
		if (target == null)
		{
			if (object.target != null)
				return false;
		}
		else
		{
			if (!target.equals(object.target))
				return false;
		}
		
		return true;
	}
	
	
	@Override
	public String toString()
	{
		return "[" + source.fullName + "-->" + target.fullName + "]";
	}
}
