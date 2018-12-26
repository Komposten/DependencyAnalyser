package komposten.analyser.gui.views.statistics;

import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import komposten.analyser.backend.PackageData;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.backend.Backend.PropertyChangeListener;

public class StatisticsPanel extends JSplitPane
{
	private JScrollPane scrollPane;
	private JTable table;
	private JPanel graphPanel;
	private StatisticsTableModel tableModel;
	
	public StatisticsPanel(Backend backend)
	{
		//CURRENT: Add thresholds to the back-end so "bad" values can be highlighted.
		//			Pass these to the statistic instances? Store in Settings.
		//NEXT_TASK Add a graph to the right panel, which visualises any selected row with a Statistic in.
		//			Use embedded JavaFX chart, or JFreeChart? Advantage with JFreeChart: can be exported.
		//NEXT_TASK Differentiate between "package statistics" and "global statistics".
		//NEXT_TASK Support class/file statistics? (For the class panel.)
		backend.addPropertyChangeListener(propertyListener, Backend.SELECTED_PACKAGE);
		
		tableModel = new StatisticsTableModel();
		table = new JTable(tableModel);
		scrollPane = new JScrollPane(table);
		graphPanel = new JPanel();
		
		prepareTable();
		
		setContinuousLayout(true);
		setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		setLeftComponent(scrollPane);
		setRightComponent(graphPanel);
	}


	private void prepareTable()
	{
		Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
		while (columns.hasMoreElements())
			columns.nextElement().setCellRenderer(new StatisticsCellRenderer());
		
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	
	private PropertyChangeListener propertyListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChanged(String key, Object value)
		{
			PackageData packageData = (PackageData) value;
			
			tableModel.setProperties(packageData.packageProperties);
		}
	};
}
