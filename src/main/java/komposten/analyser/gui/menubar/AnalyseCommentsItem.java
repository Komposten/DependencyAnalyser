package komposten.analyser.gui.menubar;

import komposten.analyser.backend.util.Constants;
import komposten.analyser.gui.backend.Backend;

public class AnalyseCommentsItem extends SettingsItem
{

	public AnalyseCommentsItem(Backend backend)
	{
		super("Analyse comments", backend);
		
		setMnemonic('c');
		
		setSelected(backend.getSettings().getBoolean(Constants.SettingKeys.ANALYSE_COMMENTS));
	}


	@Override
	protected void onStateChanged()
	{
		backend.getSettings().set(Constants.SettingKeys.ANALYSE_COMMENTS, Boolean.toString(isSelected()));
	}
}
