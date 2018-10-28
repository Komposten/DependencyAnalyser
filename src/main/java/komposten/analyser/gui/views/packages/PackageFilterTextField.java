package komposten.analyser.gui.views.packages;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

public class PackageFilterTextField extends JFormattedTextField
{
	private static final String MESSAGE = "Filter...";
	private PackageList packageList;
	private DefaultFormatter formatter;
	
	private boolean showingDefaultText;
	private Color foreground;
	private Color promptColor;


	public PackageFilterTextField(PackageList list)
	{
		this.packageList = list;
		
		formatter = new DefaultFormatter();
		formatter.setCommitsOnValidEdit(true);
		
		setFormatter(formatter);
		setFormatterFactory(new DefaultFormatterFactory(formatter));
		
		addPropertyChangeListener("value", listener);
		addFocusListener(focusListener);
		
		foreground = getForeground();
		promptColor = new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 100);
		showDefaultText(true);
	}
	
	
	private void showDefaultText(boolean show)
	{
		if (show)
		{
		  showingDefaultText = show;
			setText(MESSAGE);
			setForeground(promptColor);
		}
		else
		{
			if (getText().matches(MESSAGE))
				setText("");
			setForeground(foreground);
			showingDefaultText = show;
		}

	}
	
	
	private PropertyChangeListener listener = new PropertyChangeListener()
	{
		@Override
		public void propertyChange(PropertyChangeEvent event)
		{
			if (!showingDefaultText)
				packageList.setFilter(getText());
			else if (!getText().equals(MESSAGE))
			{
				showDefaultText(false);
				packageList.setFilter(getText());
			}
		}
	};
	
	
	private FocusListener focusListener = new FocusListener()
	{
		@Override
		public void focusGained(FocusEvent e)
		{
			showDefaultText(false);
		}
		
		@Override
		public void focusLost(FocusEvent e)
		{
			if (getText().isEmpty())
			{
				showDefaultText(true);
			}
		}
	};
}
