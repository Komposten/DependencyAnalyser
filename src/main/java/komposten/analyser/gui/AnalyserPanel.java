package komposten.analyser.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.views.packages.PackagePanel;

public class AnalyserPanel extends JPanel
{
	private PackagePanel panelPackages;
	private GraphTabPanel panelGraphs;
	private StatisticsTabPanel panelStats;

	public AnalyserPanel(Backend backend)
	{
		setLayout(new BorderLayout());
		
		panelPackages = new PackagePanel(backend);
		panelGraphs = new GraphTabPanel(backend);
		panelStats = new StatisticsTabPanel(backend);
		
		JSplitPane splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, panelGraphs, panelStats);
		JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, panelPackages, splitPaneV);
		
		splitPaneV.setResizeWeight(1);
		
		add(splitPaneH, BorderLayout.CENTER);
	}
	
	
	public void rebuildGraphs()
	{
		panelGraphs.rebuildGraphs();
	}
}
