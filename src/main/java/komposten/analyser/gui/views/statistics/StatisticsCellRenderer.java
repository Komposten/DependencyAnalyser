package komposten.analyser.gui.views.statistics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import komposten.analyser.backend.util.Statistic;

public class StatisticsCellRenderer extends DefaultTableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component component = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		
		JLabel label = (JLabel)component;
		
		label.setBorder(new CompoundBorder(label.getBorder(), new EmptyBorder(2, 2, 2, 2)));
		
		if (column == 0 && table.getValueAt(row, 1).equals(""))
		{
			component.setFont(component.getFont().deriveFont(Font.BOLD));
		}

		if (table.getSelectedRow() == row)
		{
			component.setForeground(Color.WHITE);
		}
		else
		{
			component.setForeground(Color.BLACK);
		}
		
		
		if (value instanceof Statistic)
		{
			Statistic stat = (Statistic) value;
			
			if (stat.getValue() >= stat.getThreshold())
			{
				if (table.getSelectedRow() == row)
				{
					component.setForeground(Color.RED.brighter());
				}
				else
				{
					component.setForeground(Color.RED);
				}
			}
		}
		
		return component;
	}
}
