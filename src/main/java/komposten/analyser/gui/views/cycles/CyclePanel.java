package komposten.analyser.gui.views.cycles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxICell;

import komposten.analyser.backend.Cycle;
import komposten.analyser.backend.Dependency;
import komposten.analyser.backend.PackageData;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.views.DependencyEdge;
import komposten.analyser.gui.views.RootedGraphPanel;

public class CyclePanel extends RootedGraphPanel
{

	
	public CyclePanel(Backend backend)
	{
		super(backend);
		
		jGraph.setEdgesSelectable(false);
		
		add(new CycleMenuBar(this), BorderLayout.NORTH);
	}
	
	
	@Override
	protected mxGraphLayout createLayout()
	{
		return new CycleLayout(jGraph, 10d);
	}


	@Override
	protected void addVertices(PackageData rootPackage)
	{
		List<Cycle> cycles = rootPackage.cycles;
		
		for (Cycle cycle : cycles)
		{
			for (PackageData data : cycle.getPackages())
				graph.addVertex(data);
		}
		
		Object[] cells = jGraph.getVertexToCellMap().values().toArray();
		jGraph.applyCycleStyle(cells);
	}


	@Override
	protected void addEdges(PackageData rootPackage)
	{
		List<Cycle> cycles = rootPackage.cycles;
		Set<DependencyEdge> edges = new HashSet<DependencyEdge>();
		
		for (Cycle cycle : cycles)
		{
			PackageData[] cyclePackages = cycle.getPackages();
			for (int i = 0; i < cyclePackages.length-1; i++)
			{
				DependencyEdge edge = new DependencyEdge(cyclePackages[i], cyclePackages[i+1]);
				graph.addEdge(edge.getSource(), edge.getTarget(), edge);
			}
		}
		
//		addVisibleEdges(edges);
		
		mxICell[] cells = jGraph.getEdgeToCellMap().values().toArray(new mxICell[0]);
		jGraph.applyCycleStyle(cells);
	}
	
	
	private void addVisibleEdges(Set<DependencyEdge> edges) //FIXME CyclePanel; Re-factor to improve code quality
	{
		if (visibleEdges != SHOW_ALL_EDGES)
		{
			Object[] selectedCells = jGraph.getSelectionCells();
			
			if (selectedCells.length == 0)
			{
				edges.clear();
			}
			else
			{
				for (Iterator<DependencyEdge> iterator = edges.iterator(); iterator.hasNext();)
				{
					DependencyEdge edge = iterator.next();
					boolean keep = false;
					
					for (Object selectedCell : selectedCells)
					{
						if (((mxICell)selectedCell).isVertex()) //FIXME CyclePanel; Remove all edges from the array prior to looping to optimise?
						{
							PackageData vertex = jGraph.getCellToVertexMap().get(selectedCell);
							
							switch (visibleEdges)
							{
								case SHOW_ALL_EDGES_FOR_SELECTED :
									if (edge.getSource() == vertex || edge.getTarget() == vertex)
										keep = true;
									break;
								case SHOW_ALL_EDGES_FROM_SELECTED :
									if (edge.getSource() == vertex)
										keep = true;
									break;
								case SHOW_ALL_EDGES_TO_SELECTED :
									if (edge.getTarget() == vertex)
										keep = true;
									break;
							}
						}
					}
					
					if (!keep)
						iterator.remove();
				}
			}
		}
		
		for (DependencyEdge edge : edges)
			graph.addEdge(edge.getSource(), edge.getTarget(), edge);
	}


//	@Override
//	public void setVisibleEdges(int mode)
//	{
//		this.visibleEdges = mode;
//		
//		if (rootPackage != null)
//		{
//			clearEdges();
//			addEdges(rootPackage);
//			//FIXME CyclePanel; Rework this so we don't need to do a complete clear of all edges. Instead toggle visibility on the edges. Same goes for selectionChanged().
//		}
//	}
	
	
	@Override
	protected void selectionChanged(Object[] newSelection)
	{
		refreshGraph(false);
//		if (rootPackage != null && visibleEdges != SHOW_ALL_EDGES)
//		{
//			clearEdges();
//			addEdges(rootPackage);
//		}
	}
	
	
	@Override
	protected void vertexDoubleClicked(PackageData packageData)
	{
		StringBuilder message = new StringBuilder();
		
		for (Dependency dependency : packageData.dependencies)
		{
			message.append("Files pointing to \"" + dependency.target.fullName + "\"");
			for (File file : dependency.filesWithDependency)
				message.append("\n" + file.getName());
			
			message.append('\n');
			message.append('\n');
		}

		JTextArea textArea = new JTextArea(message.toString());
		JScrollPane scrollPane = new JScrollPane(textArea);
		
		textArea.setEditable(false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, 400));
		
		JOptionPane.showMessageDialog(this, scrollPane, "Dependencies for " + packageData.fullName, JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	@Override
	protected void edgeDoubleClicked(DependencyEdge edge, boolean isBidirectional)
	{
		backend.setSelectedEdge(edge, isBidirectional);
	}
}
