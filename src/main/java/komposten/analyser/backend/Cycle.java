package komposten.analyser.backend;

import java.io.Serializable;

public class Cycle implements Serializable
{
	private PackageData[] packages;
	
	public Cycle(PackageData[] packages)
	{
		this.packages = packages;
	}
	
	
	public PackageData[] getPackages()
	{
		return packages;
	}
	
	
	public boolean contains(PackageData data)
	{
		for (PackageData packageData : packages)
		{
			if (packageData.equals(data))
				return true;
		}
		
		return false;
	}
}
