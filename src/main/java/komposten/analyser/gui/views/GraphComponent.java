package komposten.analyser.gui.views;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

import komposten.analyser.backend.Edge;
import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.Vertex;
import komposten.analyser.gui.backend.ZoomAction;
import komposten.analyser.gui.backend.Zoomable;

public class GraphComponent<V extends Vertex, E extends Edge> extends mxGraphComponent implements Zoomable
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
		getGraphControl().addMouseWheelListener(mouseListener);
		graph.getSelectionModel().addListener(mxEvent.CHANGE, selectionChangedListener);
		
		rubberband = new mxRubberband(this);
		
		createKeyboardShortcuts();
	}


	private void createKeyboardShortcuts()
	{
		InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();
		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK, true), "zoomin");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK, true), "zoomout");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK, true), "zoomreset");
		actionMap.put("zoomin", new ZoomAction(ZoomAction.ZOOM_IN, this));
		actionMap.put("zoomout", new ZoomAction(ZoomAction.ZOOM_OUT, this));
		actionMap.put("zoomreset", new ZoomAction(ZoomAction.ZOOM_RESET, this));
	}
	
	
	public void fitGraphToView()
	{
		mxGraphView view = getGraph().getView();
		int componentWidth = getWidth();
		int componentHeight = getHeight();
		
		mxRectangle graphBounds = getGraphBounds();
		double viewWidth = graphBounds.getWidth();
		double viewHeight = graphBounds.getHeight();

		double widthValue = componentWidth/viewWidth;
		double heightValue = componentHeight/viewHeight;
		view.setScale(Math.min(widthValue, heightValue) * 0.9);
		
		moveGraphIntoView(graphBounds);
	}
	
	
	public void fitGraphToWidth()
	{
		mxGraphView view = getGraph().getView();
		int componentWidth = getWidth();

		mxRectangle graphBounds = getGraphBounds();
		double viewWidth = graphBounds.getWidth();

		double widthValue = componentWidth/viewWidth;
		view.setScale(widthValue * 0.9);

		moveGraphIntoView(graphBounds);
	}
	
	
	public void fitGraphToHeight()
	{
		mxGraphView view = getGraph().getView();
		int componentHeight = getHeight();

		mxRectangle graphBounds = getGraphBounds();
		double viewHeight = graphBounds.getHeight();

		double heightValue = componentHeight/viewHeight;
		view.setScale(heightValue * 0.9);

		moveGraphIntoView(graphBounds);
	}
	
	
	public void moveGraphIntoView()
	{
		moveGraphIntoView(getGraphBounds());
	}
	
	
	public void moveGraphIntoView(mxRectangle graphBounds)
	{
		mxGraph graph = getGraph();
		
		Object[] oldSelection = graph.getSelectionCells();
		
		graph.selectAll();
		graph.moveCells(graph.getSelectionCells(), -graphBounds.getX(), -graphBounds.getY());
		graph.setSelectionCells(oldSelection);
	}
	
	
	/**
	 * @return The bounds of graph independent of the current scale.
	 */
	private mxRectangle getGraphBounds()
	{
		mxGraph graph = getGraph();
		
		Object[] oldSelection = graph.getSelectionCells();
		
		graph.selectAll();
		
		mxCell cell = (mxCell) graph.getSelectionCells()[0];
		double minX = cell.getGeometry().getX();
		double minY = cell.getGeometry().getY();
		double maxX = cell.getGeometry().getX() + cell.getGeometry().getWidth();
		double maxY = cell.getGeometry().getY() + cell.getGeometry().getHeight();
		
		for (Object object : graph.getSelectionCells())
		{
			cell = (mxCell)object;
			
			if (cell.getGeometry().getX() < minX)
				minX = cell.getGeometry().getX();
			if (cell.getGeometry().getX() + cell.getGeometry().getWidth() > maxX)
				maxX = cell.getGeometry().getX() + cell.getGeometry().getWidth();
			
			if (cell.getGeometry().getY() < minY)
				minY = cell.getGeometry().getY();
			if (cell.getGeometry().getY() + cell.getGeometry().getHeight() > maxY)
				maxY = cell.getGeometry().getY() + cell.getGeometry().getHeight();
		}
		
		graph.setSelectionCells(oldSelection);
		
		return new mxRectangle(minX, minY, maxX - minX, maxY - minY);
	}
	
	
	@Override
	public void zoomIn()
	{
		mxGraphView view = getGraph().getView();
		view.setScale(view.getScale() * 1.25);
	}
	
	
	@Override
	public void zoomOut()
	{
		mxGraphView view = getGraph().getView();
		view.setScale(view.getScale() / 1.25);
	}
	
	
	@Override
	public void zoomReset()
	{
		mxGraphView view = getGraph().getView();
		view.setScale(1.25);
	}
	
	
	private MouseAdapter mouseListener = new MouseAdapter()
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
		
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			if (e.isControlDown())
			{
				if (e.getWheelRotation() < 0)
					zoomIn();
				else
					zoomOut();
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
