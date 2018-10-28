package komposten.analyser.gui.views;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;

import komposten.analyser.backend.Edge;
import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.Vertex;

public class GraphComponent<V extends Vertex, E extends Edge> extends mxGraphComponent
{
	private GraphPanel<V, E> parent;
	private DependencyGraph<V, E> graph;
	
	@SuppressWarnings("unused")
	private mxRubberband rubberband;

	public GraphComponent(DependencyGraph<V, E> graph, GraphPanel<V, E> parent)
	{
		super(graph);
		
		this.parent = parent;
		this.graph = graph;

		setConnectable(false);
		setPanning(true);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		zoom(1.25d);
		getVerticalScrollBar().setUnitIncrement(16);
		getGraphControl().addMouseListener(mouseListener);
		graph.getSelectionModel().addListener(mxEvent.CHANGE, selectionChangedListener);
		
		rubberband = new mxRubberband(this);
	}
	
	
	private MouseListener mouseListener = new MouseAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent event)
		{
			if (event.getClickCount() % 2 == 0)
			{
				mxICell cell = (mxICell)getCellAt(event.getX(), event.getY());
				
				if (cell != null)
				{
					if (cell.getValue() instanceof PackageData)
					{
						parent.vertexDoubleClicked((V)cell.getValue());
					}
					else if (cell.getValue() instanceof DependencyEdge)
					{
						DependencyEdge edge = (DependencyEdge)cell.getValue();
						
						boolean isBidirectional = graph.hasEdgeBetweenVertices((V)edge.getTarget(), (V)edge.getSource()); //FIXME GraphComponent; Fix unchecked cast!
						parent.edgeDoubleClicked((E)edge, isBidirectional);
					}
				}
			}
		}
	};
	
	
	private mxIEventListener selectionChangedListener = new mxIEventListener()
	{
		@Override
		public void invoke(Object sender, mxEventObject evt)
		{
			parent.selectionChanged(graph.getSelectionCells());
		}
	};
}
