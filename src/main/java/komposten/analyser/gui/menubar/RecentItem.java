package komposten.analyser.gui.menubar;

import komposten.analyser.gui.backend.Backend;

public class RecentItem extends AbstractMenuItem
{
	private String path;
	private Backend backend;

	public RecentItem(String path, char mnemonic, Backend backend)
	{
		super(mnemonic + " " + path, mnemonic);
		
		this.path = path;
		this.backend = backend;
	}

	
	@Override
	protected void onClick()
	{
		backend.getSettings().updateRecentList(path);
		backend.analyseFolder(path);
	}
}
