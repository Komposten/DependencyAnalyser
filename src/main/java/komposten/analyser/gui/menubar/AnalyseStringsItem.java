package komposten.analyser.gui.menubar;

import komposten.analyser.gui.backend.AnalyserSettings;
import komposten.analyser.gui.backend.Backend;

public class AnalyseStringsItem extends SettingsItem
{

	public AnalyseStringsItem(Backend backend)
	{
		super("Analyse strings", backend);
		
		setMnemonic('s');
		
		setSelected(backend.getSettings().getBoolean(AnalyserSettings.ANALYSE_STRINGS));
	}


	@Override
	protected void onStateChanged()
	{
		backend.getSettings().set(AnalyserSettings.ANALYSE_STRINGS, Boolean.toString(isSelected()));
	}
}
