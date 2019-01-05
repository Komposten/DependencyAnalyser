package komposten.analyser.gui;

import java.awt.Dimension;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.files.ClassPanel;
import komposten.analyser.gui.views.statistics.StatisticsPanel;

public class StatisticsTabPanel extends JTabbedPane
{
	private StatisticsPanel globalStatistics;
	private StatisticsPanel packageStatistics;
	
	
	public StatisticsTabPanel(Backend backend, GraphTabPanel graphTabPanel)
	{
		globalStatistics = new StatisticsPanel(backend);
		packageStatistics = new StatisticsPanel(backend);
		
		graphTabPanel.addChangeListener(graphTabChangeListener);

		setMinimumSize(new Dimension(getPreferredSize().width, 200));
		
//		add("Project statistics", globalStatistics);
		add("Package statistics", packageStatistics);
	}
	
	
	private ChangeListener graphTabChangeListener = new ChangeListener()
	{
		@Override
		public void stateChanged(ChangeEvent e)
		{
			GraphTabPanel graphTabPanel = (GraphTabPanel) e.getSource();
			GraphPanel<?, ?> graph = (GraphPanel<?, ?>) graphTabPanel.getSelectedComponent();

			if (graph != null)
			{
				packageStatistics.setDataFrom(graph.getSelectedVertices());
				
				int unitTabIndex = getTabCount()-1;
				if (graph instanceof ClassPanel)
					setTitleAt(unitTabIndex, "Compilation unit statistics");
				else
					setTitleAt(unitTabIndex, "Package statistics");
			}
		}
	};
}
