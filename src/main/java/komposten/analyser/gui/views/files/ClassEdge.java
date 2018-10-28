package komposten.analyser.gui.views.files;

import komposten.analyser.backend.Edge;
import komposten.analyser.backend.Vertex;

public class ClassEdge implements Edge
{
	private Vertex source;
	private Vertex target;
	
	
	public ClassEdge()
	{
		
	}


	public ClassEdge(Vertex source, Vertex target)
	{
		this.source = source;
		this.target = target;
	}
	

	@Override
	public Vertex getSource()
	{
		return source;
	}


	@Override
	public Vertex getTarget()
	{
		return target;
	}
}