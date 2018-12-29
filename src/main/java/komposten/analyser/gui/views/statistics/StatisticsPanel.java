package komposten.analyser.gui.views.statistics;

import java.awt.Dimension;
import java.util.Enumeration;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.statistics.Statistic;
import komposten.analyser.backend.statistics.StatisticLink;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.backend.Backend.PropertyChangeListener;

public class StatisticsPanel extends JSplitPane
{
	private JScrollPane scrollPane;
	private JTable table;
	private StatisticsChartPanel graphPanel;
	private StatisticsTableModel tableModel;
	
	public StatisticsPanel(Backend backend)
	{
		//NEXT_TASK Differentiate between "package statistics" and "global statistics".
		//NEXT_TASK Support class/file statistics? (For the class panel.)
		backend.addPropertyChangeListener(propertyListener, Backend.SELECTED_PACKAGE);
		
		tableModel = new StatisticsTableModel();
		table = new JTable(tableModel);
		scrollPane = new JScrollPane(table);
		graphPanel = new StatisticsChartPanel();
		
		table.getSelectionModel().addListSelectionListener(selectionListener);
		scrollPane.setMinimumSize(new Dimension(200, table.getMinimumSize().height));
		
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
			
			int selectionIndex = table.getSelectionModel().getMinSelectionIndex();
			tableModel.setProperties(packageData.packageProperties);
			if (selectionIndex != -1)
				table.getSelectionModel().setSelectionInterval(selectionIndex, selectionIndex);
		}
	};
	
	
	private ListSelectionListener selectionListener = new ListSelectionListener()
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			if (e.getValueIsAdjusting() || table.getSelectedRow() < 0)
				return;
			
			Object value = table.getValueAt(table.getSelectedRow(), 1);
			
			if (value instanceof Statistic)
				graphPanel.display((Statistic)value);
			else if (value instanceof StatisticLink<?>)
				graphPanel.display(((StatisticLink<?>)value).getLinkTarget());
			else
				graphPanel.clear();
		}
	};
}
