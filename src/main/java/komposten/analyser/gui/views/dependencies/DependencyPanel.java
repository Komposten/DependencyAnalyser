package komposten.analyser.gui.views.dependencies;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.SwingConstants;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxICell;

import komposten.analyser.backend.Cycle;
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
		
		List<Object> verticesInCycles = new ArrayList<Object>();
		List<Object> verticesExternal = new ArrayList<Object>();
		for (Dependency dependency : rootPackage.dependencies)
		{
			boolean isExternal = dependency.target.isExternal;
			boolean shouldAdd = (visibleEdges == SHOW_ALL_EDGES ||
					(isExternal && visibleEdges == SHOW_EXTERNAL_DEPENDENCIES) ||
					(!isExternal && visibleEdges == SHOW_INTERNAL_DEPENDENCIES));
			
			if (shouldAdd)
			{
				graph.addVertex(dependency.target);
				
				if (backend.getAnalyser().isContainedInCycle(dependency.target))
					verticesInCycles.add(jGraph.getCellForVertex(dependency.target));
				if (isExternal)
					verticesExternal.add(jGraph.getCellForVertex(dependency.target));
			}
		}
		
		if (backend.getAnalyser().isContainedInCycle(rootPackage))
			verticesInCycles.add(jGraph.getCellForVertex(rootPackage));
		
		jGraph.applyCycleStyle(verticesInCycles.toArray());
		jGraph.applyExternalStyle(verticesExternal.toArray());
	}


	@Override
	protected void addEdges(PackageData rootPackage)
	{
		boolean isRootInCycle = backend.getAnalyser().isContainedInCycle(rootPackage);
		List<Object> edgesInCycles = new ArrayList<Object>();
		List<Object> edgesToExternal = new ArrayList<>();

		for (Dependency dependency : rootPackage.dependencies)
		{
			boolean isExternal = dependency.target.isExternal;
//			boolean shouldAdd = (visibleEdges == SHOW_ALL_EDGES ||
//					(isExternal && visibleEdges == SHOW_EXTERNAL_DEPENDENCIES) ||
//					(!isExternal && visibleEdges == SHOW_INTERNAL_DEPENDENCIES));
			
//			if (shouldAdd)
//			{
				DependencyEdge edge = new DependencyEdge(rootPackage, dependency.target);
				graph.addEdge(edge.getSource(), edge.getTarget(), edge);

				if (isRootInCycle)
				{
					List<Cycle> cycles = rootPackage.cycles;

					for (Cycle cycle : cycles)
					{
						if (cycle.contains(dependency.target))
						{
							edgesInCycles.add(jGraph.getCellForEdge(edge));
							break;
						}
					}
				}
				
				if (isExternal)
					edgesToExternal.add(jGraph.getCellForEdge(edge));
//			}
		}
		
		jGraph.applyCycleStyle(edgesInCycles.toArray());
		jGraph.applyExternalStyle(edgesToExternal.toArray());
	}
	
	
//	@Override
//	public void setVisibleEdges(int mode)
//	{
//		this.visibleEdges = mode;
//		
//		if (rootPackage != null)
//		{
//			PackageData temp = rootPackage;
//			clearGraph();
//			showGraphForPackage(temp);
//		}
//	}
	
	
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
	protected void selectionChanged(Object[] newSelection) { }
	
	
	@Override
	protected void vertexDoubleClicked(PackageData packageData)
	{
		backend.setSelectedPackage(packageData);
	}
	
	
	@Override
	protected void edgeDoubleClicked(DependencyEdge edge, boolean isBidirectional)
	{
		backend.setSelectedEdge(edge, false);
	}
}
