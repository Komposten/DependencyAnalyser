package komposten.analyser.gui.menubar;

import java.util.ArrayList;

import javax.swing.JMenu;

import komposten.analyser.backend.util.Constants;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.backend.Backend.PropertyChangeListener;

public class FileMenu extends JMenu
{
	private Backend backend;
	private OpenItem itemOpen;
	private ExportItem itemExport;
	private ExitItem itemExit;
	
	private ArrayList<RecentItem> recentItems;
	
	public FileMenu(Backend backend)
	{
		super("File");
		setMnemonic('f');
		
		this.backend = backend;
		
		itemOpen = new OpenItem(backend);
		itemExport = new ExportItem();
		itemExit = new ExitItem();
		
		add(itemOpen);
		add(itemExport);
		addSeparator();
		addRecentItems();
		addSeparator();
		add(itemExit);
		
		backend.addPropertyChangeListener(listener, Constants.SettingKeys.RECENT_PROJECTS);
	}

	
	private void addRecentItems()
	{
		recentItems = new ArrayList<RecentItem>();
		String[] recent = backend.getSettings().getArray(Constants.SettingKeys.RECENT_PROJECTS);
		
		addItems(recent);
	}


	private void addItems(String[] recent)
	{
		if (recent != null)
		{
			for (int i = 0; i < recent.length; i++)
			{
				RecentItem item = new RecentItem(recent[i], Character.forDigit(i+1, 10), backend);
				insert(item, 3+i);
				recentItems.add(item);
			}
		}
	}
	
	
	private PropertyChangeListener listener = new PropertyChangeListener()
	{
		@Override
		public void propertyChanged(String key, Object value)
		{
			for (RecentItem recentItem : recentItems)
				remove(recentItem);
			
			addItems((String[])value);
		}
	};
}
