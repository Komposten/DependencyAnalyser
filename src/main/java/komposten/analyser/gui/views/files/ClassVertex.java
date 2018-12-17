package komposten.analyser.gui.views.files;

import java.io.File;

import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.Vertex;

public class ClassVertex implements Vertex
{
	final PackageData packageData;
	final String name;
	final File file;
	final boolean isInCycle;
	final boolean isExternal;
	
	public ClassVertex(PackageData packageData, String name, File file)
	{
		this.packageData = packageData;
		this.name = name;
		this.file = file;
		this.isInCycle = packageData.isInCycle;
		this.isExternal = packageData.isExternal;
	}
	
	
	@Override
	public String toString()
	{
		return name;
	}
}