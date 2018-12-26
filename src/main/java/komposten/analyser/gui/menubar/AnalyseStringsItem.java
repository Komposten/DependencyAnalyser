package komposten.analyser.gui.menubar;

import komposten.analyser.backend.util.Constants;
import komposten.analyser.gui.backend.Backend;

public class AnalyseStringsItem extends SettingsItem
{

	public AnalyseStringsItem(Backend backend)
	{
		super("Analyse strings", backend);
		
		setMnemonic('s');
		
		setSelected(backend.getSettings().getBoolean(Constants.SettingKeys.ANALYSE_STRINGS));
	}


	@Override
	protected void onStateChanged()
	{
		backend.getSettings().set(Constants.SettingKeys.ANALYSE_STRINGS, Boolean.toString(isSelected()));
	}
}
