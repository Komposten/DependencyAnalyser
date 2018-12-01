package komposten.analyser.gui;


import javax.swing.JTabbedPane;

import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.backend.Backend.PropertyChangeListener;
import komposten.analyser.gui.views.cycles.CyclePanel;
import komposten.analyser.gui.views.dependencies.DependencyPanel;
import komposten.analyser.gui.views.files.ClassPanel;

public class GraphTabPanel extends JTabbedPane
{
	private DependencyPanel panelDependencies;
	private CyclePanel panelCycles;
	private ClassPanel panelClasses;

	public GraphTabPanel(Backend backend)
	{
		backend.addPropertyChangeListener(propertyListener, Backend.SELECTED_EDGE);
		
		panelCycles = new CyclePanel(backend);
		panelDependencies = new DependencyPanel(backend);
		panelClasses = new ClassPanel(backend);

		add("Dependencies", panelDependencies);
		add("Cycles", panelCycles);
		add("Classes", panelClasses);
		
	}
	
	
	void rebuildGraphs()
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
				setSelectedComponent(panelClasses);
		}
	};
}
