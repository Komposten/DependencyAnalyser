package komposten.analyser.gui.menubar;

import javax.swing.JMenuBar;

import komposten.analyser.gui.backend.Backend;

public class AnalyserMenuBar extends JMenuBar
{
	private FileMenu menuFile;
	private SettingsMenu menuSettings;
	
	public AnalyserMenuBar(Backend backend)
	{
		menuFile = new FileMenu(backend);
		menuSettings = new SettingsMenu(backend);
		
		add(menuFile);
		add(menuSettings);
	}
}
