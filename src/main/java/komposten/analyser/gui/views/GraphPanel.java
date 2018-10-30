package komposten.analyser.gui.views;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JPanel;

import org.jgrapht.graph.ListenableDirectedGraph;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

import komposten.analyser.backend.Edge;
import komposten.analyser.backend.Vertex;
import komposten.analyser.gui.backend.Backend;

public abstract class GraphPanel<V extends Vertex, E extends Edge> extends JPanel
{
	public static final int SHOW_ALL_EDGES = 0;
	public static final int SHOW_ALL_EDGES_FOR_SELECTED = 1;
	public static final int SHOW_ALL_EDGES_FROM_SELECTED = 2;
	public static final int SHOW_ALL_EDGES_TO_SELECTED = 3;
	
	public static final int SHOW_ALL_VERTICES = 0;
	
	protected Backend backend;
	protected int visibleEdges;
	protected int visibleVertices;
	
	protected ListenableDirectedGraph<V, E> graph;
	protected DependencyGraph<V, E> jGraph;
	protected mxGraphLayout graphLayout;
	protected GraphComponent<V, E> graphPanel;
	

	public GraphPanel(Backend backend, Class<? extends E> edgeClass)
	{
		this.backend = backend;
		
		graph = new ListenableDirectedGraph<>(edgeClass);
		jGraph = new DependencyGraph<>(graph);
		graphLayout = createLayout();
		graphPanel = new GraphComponent<>(jGraph, this);
		
		setLayout(new BorderLayout());
		add(graphPanel, BorderLayout.CENTER);
	}


	protected abstract mxGraphLayout createLayout();
	protected abstract void vertexDoubleClicked(V vertex);
	protected abstract void edgeDoubleClicked(E edge, boolean isBidirectional);
	protected abstract void selectionChanged(Object[] newSelection);
	
	public abstract void rebuildGraph();
	
	public void refreshGraph(boolean performLayout)
	{
		refreshVisibleVertices();
		if (performLayout)
		{
			double scale = graphPanel.getGraph().getView().getScale();
			zoomReset();
			layoutGraph();
			graphPanel.getGraph().getView().setScale(scale);;
		}
		jGraph.refresh();
	}
	

	protected void layoutGraph()
	{
		graphLayout.execute(jGraph.getDefaultParent());
	}


	protected void clearGraph()
	{
		clearVertices();
		clearEdges();
		jGraph.getSelectionModel().clear();
	}


	protected void clearVertices()
	{
		List<V> vertices = new LinkedList<>(graph.vertexSet());
		
		graph.removeAllVertices(vertices);
	}


	protected void clearEdges()
	{
		List<E> edges = new LinkedList<>(graph.edgeSet());
		
		graph.removeAllEdges(edges);
	}
	
	
	public void fitGraphToView()
	{
		mxGraphView view = graphPanel.getGraph().getView();
		int componentWidth = graphPanel.getWidth();
		int componentHeight = graphPanel.getHeight();
		
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
		mxGraphView view = graphPanel.getGraph().getView();
		int componentWidth = graphPanel.getWidth();

		mxRectangle graphBounds = getGraphBounds();
		double viewWidth = graphBounds.getWidth();

		double widthValue = componentWidth/viewWidth;
		view.setScale(widthValue * 0.9);

		moveGraphIntoView(graphBounds);
	}
	
	
	public void fitGraphToHeight()
	{
		mxGraphView view = graphPanel.getGraph().getView();
		int componentHeight = graphPanel.getHeight();

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
		mxGraph graph = graphPanel.getGraph();
		
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
		mxGraph graph = graphPanel.getGraph();
		
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
	
	
	public void zoomIn()
	{
		mxGraphView view = graphPanel.getGraph().getView();
		view.setScale(view.getScale() * 1.25);
	}
	
	
	public void zoomOut()
	{
		mxGraphView view = graphPanel.getGraph().getView();
		view.setScale(view.getScale() / 1.25);
	}
	
	
	public void zoomReset()
	{
		mxGraphView view = graphPanel.getGraph().getView();
		view.setScale(1.25);
	}
	
	
	public void setVisibleEdges(int mode)
	{
		this.visibleEdges = mode;
	}
	
	
	protected void refreshVisibleEdges()
	{
		if (visibleEdges == SHOW_ALL_EDGES)
			showAllEdges();
		else
			updateVisibleEdges(visibleEdges);
	}


	protected void showAllEdges()
	{
		for (Entry<mxICell, E> entry : jGraph.getCellToEdgeMap().entrySet())
		{
			mxICell cell = entry.getKey();
			E edge = entry.getValue();
			
			boolean isSourceVisible = jGraph.getCellForVertex((V)edge.getSource()).isVisible();
			boolean isTargetVisible = jGraph.getCellForVertex((V)edge.getTarget()).isVisible();
			
			cell.setVisible(isSourceVisible && isTargetVisible);
		}
	}


	protected void updateVisibleEdges(int mode)
	{
		boolean showIncoming = true;
		boolean showOutgoing = true;
		switch (mode)
		{
			case SHOW_ALL_EDGES_FROM_SELECTED :
				showIncoming = false;
				break;
			case SHOW_ALL_EDGES_TO_SELECTED :
				showOutgoing = false;
				break;
			case SHOW_ALL_EDGES_FOR_SELECTED :
			default :
				break;
		}
		
		Set<V> selectedVertices = new HashSet<>();
		Object[] selectedCells = jGraph.getSelectionCells();
		
		for (Object object : selectedCells)
		{
			mxICell cell = (mxICell)object;
			
			if (cell.isVertex())
				selectedVertices.add(jGraph.getCellToVertexMap().get(cell));
		}
		
		for (Entry<mxICell, E> entry : jGraph.getCellToEdgeMap().entrySet())
		{
			mxICell cell = entry.getKey();
			E edge = entry.getValue();
			
			boolean isSourceVisible = jGraph.getCellForVertex((V)edge.getSource()).isVisible();
			boolean isTargetVisible = jGraph.getCellForVertex((V)edge.getTarget()).isVisible();
			
			boolean isIncoming = (showIncoming && selectedVertices.contains(edge.getTarget()));
			boolean isOutgoing = (showOutgoing && selectedVertices.contains(edge.getSource()));
			
			if ((isSourceVisible && isTargetVisible) &&
					(isIncoming || isOutgoing))
			{
				cell.setVisible(true);
			}
			else
			{
				cell.setVisible(false);
			}
		}
	}

	
	/**
	 * Sets which vertices should be visible.<br />
	 * This method automatically calls {@link #refreshVisibleEdges()} as well
	 * in order to hide/show edges to hidden/visible vertices.
	 * @param mode
	 */
	public void setVisibleVertices(int mode)
	{
		this.visibleVertices = mode;
	}
	
	
	/**
	 * Refreshes the visibility of the vertices.<br />
	 * This method automatically calls {@link #refreshVisibleEdges()} as well
	 * in order to hide/show edges to hidden/visible vertices.
	 */
	protected void refreshVisibleVertices()
	{
		if (visibleVertices == SHOW_ALL_VERTICES)
			showAllVertices();
		else
			updateVisibleVertices(visibleVertices);
		
		refreshVisibleEdges();
	}


	protected void showAllVertices()
	{
		for (mxICell cell : jGraph.getVertexToCellMap().values())
		{
			cell.setVisible(true);
		}
	}


	protected void updateVisibleVertices(int mode)
	{
		
	}
}
