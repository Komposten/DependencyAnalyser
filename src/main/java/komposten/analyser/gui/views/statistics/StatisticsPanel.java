package komposten.analyser.gui.views.statistics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
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
	private JLabel tableHeader;
	private StatisticsChartPanel graphPanel;
	private StatisticsTableModel tableModel;
	
	public StatisticsPanel(Backend backend)
	{
		backend.addPropertyChangeListener(propertyListener, Backend.NEW_ACTIVE_PACKAGE, Backend.SELECTED_PACKAGES, Backend.SELECTED_COMPILATION_UNITS);
		
		tableModel = new StatisticsTableModel();
		table = new JTable(tableModel);
		scrollPane = new JScrollPane(table);
		graphPanel = new StatisticsChartPanel();
		
		table.getSelectionModel().addListSelectionListener(selectionListener);
		table.setTableHeader(null);
		scrollPane.setMinimumSize(new Dimension(285, table.getMinimumSize().height));
		
		tableHeader = new JLabel(" ");
		tableHeader.setBorder(new EmptyBorder(5, 5, 5, 5));
		tableHeader.setFont(tableHeader.getFont().deriveFont(Font.BOLD));

		JPanel panelLeft = new JPanel(new BorderLayout());
		panelLeft.add(tableHeader, BorderLayout.NORTH);
		panelLeft.add(scrollPane, BorderLayout.CENTER);
		
		prepareTable();
		
		setContinuousLayout(true);
		setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		setLeftComponent(panelLeft);
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


		private void setTableDataMissing(String reason)
		{
			PackageProperties reasonProperties = new PackageProperties();
			reasonProperties.set("Reason", reason);
			
			PackageProperties properties = new PackageProperties();
			properties.set("No data", reasonProperties);
			setTableData(properties);
		}
		
		
		@Override
		public void propertyChanged(String key, Object value)
		{
			if (key.equals(Backend.NEW_ACTIVE_PACKAGE))
			{
				lastActivePackage = (PackageData) value;
				
				setTableData(lastActivePackage.packageProperties);
				tableHeader.setText(lastActivePackage.fullName);
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
					if (!array[0].isExternal)
					{
						setTableData(array[0].packageProperties);
						tableHeader.setText(array[0].fullName);
					}
					else
					{
						setTableDataMissing("External resource");
						tableHeader.setText(array[0].fullName);
					}
				}
				else
				{
					setTableDataMissing("Multiple selected");
					tableHeader.setText(array.length + " packages");
				}
			}
			else if (key.equals(Backend.SELECTED_COMPILATION_UNITS))
			{
				Object[][] array = (Object[][]) value;
				
				if (array.length == 1)
				{
					String unitName = (String) array[0][0];
					PackageData unitPackage = (PackageData) array[0][1];

					String labelText = String.format("<html><span style=\"font-size: 80%%\">%s</span>.%s</html>", unitPackage.fullName, unitName);
					tableHeader.setText(labelText);
					
					if (!unitPackage.isExternal)
					{
						File unitFile = unitPackage.getCompilationUnitByName(unitName);
						PackageProperties unitProperties = unitPackage.fileProperties.get(unitFile);
						
						
						if (unitProperties != null)
						{
							setTableData(unitProperties);
						}
						else
						{
							setTableDataMissing("Missing file");
						}
					}
					else
					{
						setTableDataMissing("External resource");
					}
				}
				else if (array.length >= 2)
				{
					setTableDataMissing("Multiple selected");
					tableHeader.setText(array.length + " units");
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
