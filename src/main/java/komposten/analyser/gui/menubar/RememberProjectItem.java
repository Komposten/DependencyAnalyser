package komposten.analyser.gui.menubar;

import komposten.analyser.gui.backend.AnalyserSettings;
import komposten.analyser.gui.backend.Backend;

public class RememberProjectItem extends SettingsItem
{

	public RememberProjectItem(Backend backend)
	{
		super("Remember last project on close", backend);
		
		setMnemonic('p');
		
		setSelected(backend.getSettings().getBoolean(AnalyserSettings.REMEMBER_LAST_PROJECT));
	}


	@Override
	protected void onStateChanged()
	{
		backend.getSettings().set(AnalyserSettings.REMEMBER_LAST_PROJECT, Boolean.toString(isSelected()));
	}
}
