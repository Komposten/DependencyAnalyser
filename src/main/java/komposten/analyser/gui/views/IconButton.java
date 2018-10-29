package komposten.analyser.gui.views;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public abstract class IconButton extends JButton
{

	public IconButton(String iconPath)
	{
		this (iconPath, null);
	}
	

	public IconButton(String iconPath, String toolTip)
	{
		super(new ImageIcon(IconButton.class.getResource(iconPath)));
		
		if (toolTip != null)
			setToolTipText(toolTip);
		
		addActionListener(actionListener);
		setFocusPainted(false);
		setMargin(new Insets(2, 2, 2, 2));
	}
	
	
	protected abstract void onClick();
	
	
	private ActionListener actionListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			onClick();
		}
	};
}
