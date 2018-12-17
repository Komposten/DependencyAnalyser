package komposten.analyser.gui.views.cycles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.mxgraph.layout.mxGraphLayout;

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
		
		for (Cycle cycle : cycles)
		{
			PackageData[] cyclePackages = cycle.getPackages();
			for (int i = 0; i < cyclePackages.length-1; i++)
			{
				DependencyEdge edge = new DependencyEdge(cyclePackages[i], cyclePackages[i+1]);
				graph.addEdge(edge.getSource(), edge.getTarget(), edge);
			}
		}
		
		Object[] cells = jGraph.getEdgeToCellMap().values().toArray();
		jGraph.applyCycleStyle(cells);
	}
	
	
	@Override
	protected void selectionChanged(Object[] newSelection)
	{
		if (newSelection.length > 0)
			refreshGraph(false);
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
