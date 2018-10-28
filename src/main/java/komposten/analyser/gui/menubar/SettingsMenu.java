package komposten.analyser.gui.menubar;

import javax.swing.JMenu;

import komposten.analyser.gui.backend.Backend;

public class SettingsMenu extends JMenu
{
	private RememberProjectItem itemRememberProject;
	
	public SettingsMenu(Backend backend)
	{
		super("Settings");
		
		setMnemonic('t');
		
		itemRememberProject = new RememberProjectItem(backend);
		
		add(itemRememberProject);
	}
}
