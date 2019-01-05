package komposten.analyser.gui.views.dependencies;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.SwingConstants;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxICell;

import komposten.analyser.backend.Dependency;
import komposten.analyser.backend.PackageData;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.views.DependencyEdge;
import komposten.analyser.gui.views.RootedGraphPanel;

public class DependencyPanel extends RootedGraphPanel
{
	public static final int SHOW_INTERNAL_DEPENDENCIES = 10;
	public static final int SHOW_EXTERNAL_DEPENDENCIES = 11;
	
	public DependencyPanel(Backend backend)
	{
		super(backend);
		
		jGraph.setEdgesSelectable(true);
		
		add(new DependencyMenuBar(this), BorderLayout.NORTH);
	}
	
	
	@Override
	protected mxGraphLayout createLayout()
	{
		return new mxHierarchicalLayout(jGraph, SwingConstants.WEST);
	}


	@Override
	protected void addVertices(PackageData rootPackage)
	{
		graph.addVertex(rootPackage);
		
		List<Object> verticesInCycles = new ArrayList<>();
		List<Object> verticesExternal = new ArrayList<>();
		List<Object> verticesDefault = new ArrayList<>();
		for (Dependency dependency : rootPackage.dependencies)
		{
			boolean isExternal = dependency.target.isExternal;
			graph.addVertex(dependency.target);

			Object vertexCell = jGraph.getCellForVertex(dependency.target);
			if (dependency.target.isInCycle)
				verticesInCycles.add(vertexCell);
			else if (isExternal)
				verticesExternal.add(vertexCell);
			else
				verticesDefault.add(vertexCell);
		}

		if (rootPackage.isInCycle)
			verticesInCycles.add(jGraph.getCellForVertex(rootPackage));
		
		jGraph.applyDefaultStyle(verticesDefault.toArray());
		jGraph.applyCycleStyle(verticesInCycles.toArray());
		jGraph.applyExternalStyle(verticesExternal.toArray());
	}


	@Override
	protected void addEdges(PackageData rootPackage)
	{
		List<Object> edgesInCycles = new ArrayList<>();
		List<Object> edgesToExternal = new ArrayList<>();
		List<Object> edgesDefault = new ArrayList<>();

		for (Dependency dependency : rootPackage.dependencies)
		{
			boolean isExternal = dependency.target.isExternal;
			DependencyEdge edge = new DependencyEdge(rootPackage, dependency.target);
			graph.addEdge(edge.getSource(), edge.getTarget(), edge);

			Object edgeCell = jGraph.getCellForEdge(edge);
			if (rootPackage.sharesCycleWith(dependency.target))
				edgesInCycles.add(edgeCell);
			else if (isExternal)
				edgesToExternal.add(edgeCell);
			else
				edgesDefault.add(edgeCell);
		}

		jGraph.applyDefaultStyle(edgesDefault.toArray());
		jGraph.applyCycleStyle(edgesInCycles.toArray());
		jGraph.applyExternalStyle(edgesToExternal.toArray());
	}
	
	
	@Override
	protected void updateVisibleVertices(int mode)
	{
		boolean showInternal = true;
		boolean showExternal = true;
		switch (mode)
		{
			case SHOW_INTERNAL_DEPENDENCIES :
				showExternal = false;
				break;
			case SHOW_EXTERNAL_DEPENDENCIES :
				showInternal = false;
				break;
			case SHOW_ALL_EDGES_FOR_SELECTED :
			default :
				break;
		}
		
		for (Entry<mxICell, PackageData> entry : jGraph.getCellToVertexMap().entrySet())
		{
			if (entry.getValue() == rootPackage)
				continue;
			
			boolean isExternal = entry.getValue().isExternal;
			
			if ((isExternal && showExternal) ||
					(!isExternal && showInternal))
			{
				entry.getKey().setVisible(true);
			}
			else
			{
				entry.getKey().setVisible(false);
			}
		}
	}
	
	
	@Override
	protected void selectionChanged(Object[] newSelection)
	{
		if (newSelection.length > 0)
		{
			setActiveCells(newSelection);
			refreshGraph(false);
		}
		
		if (isVisible())
			backend.setSelectedPackages((PackageData[]) getSelectedVertices());
	}
	
	
	@Override
	public Object getSelectedVertices()
	{
		return getPackagesFromSelection(activeCells);
	}
	
	
	@Override
	protected void vertexDoubleClicked(PackageData packageData)
	{
		backend.setActivePackage(packageData);
	}
	
	
	@Override
	protected void edgeDoubleClicked(DependencyEdge edge, boolean isBidirectional)
	{
		backend.setSelectedEdge(edge, false);
	}
}
