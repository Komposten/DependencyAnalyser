package komposten.analyser.gui.views;

import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;

import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;

public class DependencyGraph<V, E> extends JGraphXAdapter<V, E>
{
	private ListenableGraph<V, E> graph;
	private boolean edgesSelectable;
	
	public DependencyGraph(ListenableGraph<V, E> graph)
	{
		super(graph);
		
		this.graph = graph;

		setCellsEditable(false);
		setCellsResizable(false);
		setCellsDisconnectable(false);
		setAllowDanglingEdges(false);
		setConnectableEdges(false);
		setSplitEnabled(false);
		setGridEnabled(false);
		getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_NOLABEL, "1");
		getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_MOVABLE, "0");
		getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_SPACING, "1");
		getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_SPACING_TOP, "4");
	}
	
	
	public void setEdgesSelectable(boolean selectable)
	{
		edgesSelectable = selectable;
	}


	public void applyCycleStyle(Object[] cells)
	{
		setCellStyles(mxConstants.STYLE_FILLCOLOR, "#FF6B6B", cells);
		setCellStyles(mxConstants.STYLE_STROKECOLOR, "#B73737", cells);
		setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1", cells);
		setCellStyles(mxConstants.STYLE_FONTCOLOR, "black", cells);
	}
	
	
	public void applyExternalStyle(Object[] cells)
	{
		setCellStyles(mxConstants.STYLE_FILLCOLOR, "#79B378", cells);
		setCellStyles(mxConstants.STYLE_STROKECOLOR, "#4B7E4A", cells);
		setCellStyles(mxConstants.STYLE_STROKEWIDTH, "1", cells);
		setCellStyles(mxConstants.STYLE_FONTCOLOR, "#3F3F3F", cells);
	}
	
	
	public mxICell getCellForVertex(V vertex)
	{
		return getVertexToCellMap().get(vertex);
	}
	
	
	public mxICell getCellForEdge(E edge)
	{
		return getEdgeToCellMap().get(edge);
	}
	
	
	public boolean hasEdgeBetweenVertices(V vertex1, V vertex2)
	{
		return graph.containsEdge(vertex1, vertex2);
	}
	
	
	@Override
	public boolean isCellSelectable(Object cell)
	{
		if (((mxICell)cell).isEdge())
			return edgesSelectable;
		return super.isCellSelectable(cell);
	}
}
