package komposten.analyser.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.views.packages.PackagePanel;

public class AnalyserPanel extends JPanel
{
	private PackagePanel panelPackages;
	private GraphTabPanel panelTabs;

	public AnalyserPanel(Backend backend)
	{
		setLayout(new BorderLayout());
		
		panelPackages = new PackagePanel(backend);
		
		panelTabs = new GraphTabPanel(backend);
		panelTabs.setMinimumSize(new Dimension(500, panelTabs.getMinimumSize().height));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, panelPackages, panelTabs);
		
		add(splitPane, BorderLayout.CENTER);
	}
	
	
	public void rebuildGraphs()
	{
		panelTabs.rebuildGraphs();
	}
}
