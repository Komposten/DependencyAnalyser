package komposten.analyser.gui;

import java.awt.Dimension;

import javax.swing.JTabbedPane;

import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.views.statistics.StatisticsPanel;

public class StatisticsTabPanel extends JTabbedPane
{
	private StatisticsPanel globalStatistics;
	private StatisticsPanel packageStatistics;
	
	
	public StatisticsTabPanel(Backend backend)
	{
		globalStatistics = new StatisticsPanel(backend);
		packageStatistics = new StatisticsPanel(backend);

		setMinimumSize(new Dimension(getPreferredSize().width, 200));
		
//		add("Project statistics", globalStatistics);
		add("Package statistics", packageStatistics);
	}
}
