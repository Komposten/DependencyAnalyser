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

	private PackageData lastActivePackage;
	
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
	
	
	public void setDataFrom(Object object)
	{
		if (object instanceof PackageData)
			newActivePackage((PackageData) object);
		else if (object instanceof PackageData[])
			newPackageSelection((PackageData[]) object);
		else if (object instanceof Object[][])
			newCompilationUnitSelection((Object[][]) object);
	}
	
	
	private void setDataFrom(String eventKey, Object value)
	{
		if (eventKey.equals(Backend.NEW_ACTIVE_PACKAGE))
			newActivePackage((PackageData) value);
		else if (eventKey.equals(Backend.SELECTED_PACKAGES))
			newPackageSelection((PackageData[]) value);
		else if (eventKey.equals(Backend.SELECTED_COMPILATION_UNITS))
			newCompilationUnitSelection((Object[][]) value);
	}


	private void newActivePackage(PackageData value)
	{
		lastActivePackage = value;
		
		setTableData(lastActivePackage.packageProperties);
		tableHeader.setText(lastActivePackage.fullName);
	}


	private void newPackageSelection(PackageData[] selectedPackages)
	{
		if (selectedPackages.length == 0)
		{
			if (lastActivePackage != null)
				setTableData(lastActivePackage.packageProperties);
		}
		else if (selectedPackages.length == 1)
		{
			if (!selectedPackages[0].isExternal)
			{
				setTableData(selectedPackages[0].packageProperties);
				tableHeader.setText(selectedPackages[0].fullName);
			}
			else
			{
				setTableDataMissing("External resource");
				tableHeader.setText(selectedPackages[0].fullName);
			}
		}
		else
		{
			setTableDataMissing("Multiple selected");
			tableHeader.setText(selectedPackages.length + " packages");
		}
	}


	private void newCompilationUnitSelection(Object[][] selectedUnits)
	{
		if (selectedUnits.length == 0)
		{
			setTableData(new PackageProperties());
			tableHeader.setText("No selection");
		}
		else if (selectedUnits.length == 1)
		{
			String unitName = (String) selectedUnits[0][0];
			PackageData unitPackage = (PackageData) selectedUnits[0][1];

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
		else if (selectedUnits.length >= 2)
		{
			setTableDataMissing("Multiple selected");
			tableHeader.setText(selectedUnits.length + " units");
		}
	}


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
	
	
	private PropertyChangeListener propertyListener = new PropertyChangeListener()
	{
		
		
		@Override
		public void propertyChanged(String key, Object value)
		{
			setDataFrom(key, value);
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
