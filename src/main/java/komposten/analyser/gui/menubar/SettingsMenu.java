package komposten.analyser.gui.menubar;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import komposten.analyser.gui.backend.Backend;

public class SettingsMenu extends JMenu
{
	private JMenuItem itemRememberProject;
	private JMenuItem itemAnalyseComments;
	private JMenuItem itemAnalyseStrings;
	
	public SettingsMenu(Backend backend)
	{
		super("Settings");
		
		setMnemonic('t');
		
		itemRememberProject = new RememberProjectItem(backend);
		itemAnalyseComments = new AnalyseCommentsItem(backend);
		itemAnalyseStrings = new AnalyseStringsItem(backend);
		
		add(itemRememberProject);
		add(itemAnalyseComments);
		add(itemAnalyseStrings);
	}
}
