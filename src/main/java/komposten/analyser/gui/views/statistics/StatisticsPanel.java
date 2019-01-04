package komposten.analyser.gui.views.statistics;

import java.awt.Dimension;
import java.io.File;
import java.util.Enumeration;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.PackageProperties;
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
		//CURRENT 2: Add a header that clarifies what the statistics belong to (e.g. a package/class name, or "4 packages").
		//NEXT_TASK Differentiate between "package statistics" and "global statistics".
		backend.addPropertyChangeListener(propertyListener, Backend.NEW_ACTIVE_PACKAGE, Backend.SELECTED_PACKAGES, Backend.SELECTED_COMPILATION_UNITS);
		
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
		private PackageData lastActivePackage;
		
		private void setTableData(PackageProperties properties)
		{
			int selectionIndex = table.getSelectionModel().getMinSelectionIndex();
			tableModel.setProperties(properties);
			if (selectionIndex != -1 && selectionIndex < table.getRowCount())
				table.getSelectionModel().setSelectionInterval(selectionIndex, selectionIndex);
		}
		
		
		@Override
		public void propertyChanged(String key, Object value)
		{
			if (key.equals(Backend.NEW_ACTIVE_PACKAGE))
			{
				lastActivePackage = (PackageData) value;
				
				setTableData(lastActivePackage.packageProperties);
			}
			else if (key.equals(Backend.SELECTED_PACKAGES))
			{
				PackageData[] array = (PackageData[]) value;
				
				if (array.length == 0)
				{
					setTableData(lastActivePackage.packageProperties);
				}
				else if (array.length == 1)
				{
					setTableData(array[0].packageProperties);
				}
				else
				{
					PackageProperties properties = new PackageProperties();
					properties.set("Multiple packages selected", array.length);
					setTableData(properties);
				}
			}
			else if (key.equals(Backend.SELECTED_COMPILATION_UNITS))
			{
				Object[][] array = (Object[][]) value;
				
				if (array.length == 1)
				{
					String unitName = (String) array[0][0];
					PackageData unitPackage = (PackageData) array[0][1];
					File unitFile = unitPackage.getCompilationUnitByName(unitName);
					PackageProperties unitProperties = unitPackage.fileProperties.get(unitFile);
					
					if (unitProperties != null)
					{
						setTableData(unitProperties);
					}
					else
					{
						PackageProperties properties = new PackageProperties();
						properties.set("No data for unit", array[0][0]);
						setTableData(properties);
					}
				}
				else if (array.length >= 2)
				{
					PackageProperties properties = new PackageProperties();
					properties.set("Multiple units selected", array.length);
					setTableData(properties);
				}
			}
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
