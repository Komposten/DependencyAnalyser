package komposten.analyser.gui.menubar;

import komposten.analyser.gui.backend.AnalyserSettings;
import komposten.analyser.gui.backend.Backend;

public class AnalyseCommentsItem extends SettingsItem
{

	public AnalyseCommentsItem(Backend backend)
	{
		super("Analyse comments", backend);
		
		setMnemonic('c');
		
		setSelected(backend.getSettings().getBoolean(AnalyserSettings.ANALYSE_COMMENTS));
	}


	@Override
	protected void onStateChanged()
	{
		backend.getSettings().set(AnalyserSettings.ANALYSE_COMMENTS, Boolean.toString(isSelected()));
	}
}
