package komposten.analyser.gui.views.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import komposten.analyser.backend.PackageProperties;

public class StatisticsTableModel extends AbstractTableModel
{
	private List<Row> rows;
	
	
	public StatisticsTableModel()
	{
		setProperties(new PackageProperties());
	}
	
	
	public void setProperties(PackageProperties properties)
	{
		this.rows = new ArrayList<>(properties.count());
		
		addRows(properties, 0);
		
		fireTableDataChanged();
	}
	

	private void addRows(PackageProperties properties, int indent)
	{
		for (Entry<String, Object> entry : properties.getValues().entrySet())
		{
			String key = entry.getKey();
			Object value = entry.getValue();
			
			if (value instanceof PackageProperties)
			{
				rows.add(new Row(key, "", indent));
				addRows((PackageProperties)value, indent+1);
			}
			else
			{
				rows.add(new Row(key, value, indent));
			}
		}
	}


	@Override
	public int getRowCount()
	{
		return rows.size();
	}


	@Override
	public int getColumnCount()
	{
		return 2;
	}


	@Override
	public String getColumnName(int columnIndex)
	{
		switch (columnIndex)
		{
			case 0 :
				return "Property";
			case 1 :
				return "Value";
			default :
				return null;
		}
	}


	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex)
		{
			case 0 :
				return String.class;
			case 1 :
				return String.class;
			default :
				return null;
		}
	}


	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}


	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch (columnIndex)
		{
			case 0 :
				return rows.get(rowIndex).getIndentedKey();
			case 1 :
				return rows.get(rowIndex).getValueAsString();
			default :
				return null;
		}
	}
	
	
	private static class Row
	{
		String key;
		Object value;
		int indent;
		
		public Row(String key, Object value, int indent)
		{
			this.key = key;
			this.value = value;
			this.indent = indent;
		}
		
		
		String getIndentedKey()
		{
			char[] indentChars = new char[indent*4];
			Arrays.fill(indentChars, ' ');
			String indentString = new String(indentChars);
			
			return indentString + key;
		}
		
		
		String getValueAsString()
		{
			
			if (value instanceof Float || value instanceof Double)
			{
				return String.format("%.02f", value);
			}
			else
			{
				return String.format("%s", value.toString());
			}
		}
	}
}
