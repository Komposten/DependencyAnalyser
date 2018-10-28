package komposten.analyser.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import komposten.analyser.gui.backend.Backend;

public abstract class SettingsItem extends JCheckBoxMenuItem
{
	protected Backend backend;

	public SettingsItem(String text, Backend backend)
	{
		super(text);
		
		this.backend = backend;
		
		addActionListener(actionListener);
	}


	protected abstract void onStateChanged();
	
	
	private ActionListener actionListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			onStateChanged();
		}
	};
}
