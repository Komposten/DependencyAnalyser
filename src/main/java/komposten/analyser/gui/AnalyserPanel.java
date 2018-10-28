package komposten.analyser.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.backend.Backend.PropertyChangeListener;
import komposten.analyser.gui.views.cycles.CyclePanel;
import komposten.analyser.gui.views.dependencies.DependencyPanel;
import komposten.analyser.gui.views.files.ClassPanel;
import komposten.analyser.gui.views.packages.PackagePanel;

public class AnalyserPanel extends JPanel
{
	private PackagePanel panelPackages;
	private DependencyPanel panelDependencies;
	private CyclePanel panelCycles;
	private ClassPanel panelClasses;
	private JTabbedPane panelTabs;

	public AnalyserPanel(Backend backend)
	{
		setLayout(new BorderLayout());
		
		backend.addPropertyChangeListener(propertyListener, Backend.SELECTED_EDGE);
		
		panelPackages = new PackagePanel(backend);
		panelCycles = new CyclePanel(backend);
		panelDependencies = new DependencyPanel(backend);
		panelClasses = new ClassPanel(backend);
		
		panelTabs = new JTabbedPane();
		panelTabs.add("Dependencies", panelDependencies);
		panelTabs.add("Cycles", panelCycles);
		panelTabs.add("Classes", panelClasses);
		panelTabs.setMinimumSize(new Dimension(500, panelTabs.getMinimumSize().height));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, panelPackages, panelTabs);
		
		add(splitPane, BorderLayout.CENTER);
	}
	
	
	public void rebuildGraphs()
	{
		panelDependencies.rebuildGraph();
		panelCycles.rebuildGraph();
		panelClasses.rebuildGraph();
	}
	
	
	private PropertyChangeListener propertyListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChanged(String key, Object value)
		{
			if (key.equals(Backend.SELECTED_EDGE))
				panelTabs.setSelectedComponent(panelClasses);
		}
	};
}
