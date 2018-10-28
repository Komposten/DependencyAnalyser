package komposten.analyser.gui.views.files;

import java.io.File;

import komposten.analyser.backend.Vertex;

public class ClassVertex implements Vertex
{
	String name;
	File file;
	
	public ClassVertex(String name, File file)
	{
		this.name = name;
		this.file = file;
	}
	
	
	@Override
	public String toString()
	{
		return name;
	}
}