package komposten.analyser.gui.views;

import komposten.analyser.backend.Edge;
import komposten.analyser.backend.Vertex;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.backend.Backend.PropertyChangeListener;

public abstract class UnrootedGraphPanel<V extends Vertex, E extends Edge> extends GraphPanel<V, E>
{
	protected E baseEdge;
	protected boolean isBidirectional;
	

	public UnrootedGraphPanel(Backend backend, Class<? extends E> edgeClass)
	{
		super(backend, edgeClass);
		
		backend.addPropertyChangeListener(propertyChangeListener,
				Backend.SELECTED_UNIDIRECTIONAL_EDGE,
				Backend.SELECTED_BIDIRECTIONAL_EDGE);
	}
	
	
	protected abstract void addVertices(Edge baseEdge, boolean isBidirectional);
	protected abstract void addEdges(Edge baseEdge, boolean isBidirectional);

	
	public void showGraphForEdge(E edge, boolean isBidirectional)
	{
		if (edge == null || edge != baseEdge)
		{
			clearGraph();
			
			baseEdge = edge;
			this.isBidirectional = isBidirectional;
			
			if (edge != null)
			{
				addVertices(edge, isBidirectional);
				addEdges(edge, isBidirectional);
				
				refreshGraph(true);
			}
		}
	}
	
	
	@Override
	public void rebuildGraph()
	{
		E edge = baseEdge;
		baseEdge = null;
		
		showGraphForEdge(edge, isBidirectional);
	}
	
	
	@Override
	protected void clearGraph()
	{
		baseEdge = null;
		super.clearGraph();
	}
	
	
	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChanged(String key, Object value)
		{
			if (key.equals(Backend.SELECTED_UNIDIRECTIONAL_EDGE))
				showGraphForEdge((E)value, false);
			else if (key.equals(Backend.SELECTED_BIDIRECTIONAL_EDGE))
				showGraphForEdge((E)value, true);
		}
	};
}
