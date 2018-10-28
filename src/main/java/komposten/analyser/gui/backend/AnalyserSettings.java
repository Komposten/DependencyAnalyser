package komposten.analyser.gui.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import komposten.analyser.tools.Settings;

public class AnalyserSettings extends Settings
{
	public static final String LAST_OPENED_DIRECTORY = "lastOpenDir";
	public static final String LAST_OPENED_PROJECT = "lastOpenedProj";
	public static final String REMEMBER_LAST_PROJECT = "rememberLastProj";
	public static final String REMEMBER_LAST_DIRECTORY = "rememberLastDir";
	public static final String RECENT_PROJECTS = "recent";
	public static final String ANALYSE_COMMENTS = "analyseComments";
	public static final String ANALYSE_STRINGS = "analyseStrings";
	public static final int RECENT_ELEMENTS_COUNT = 5;
	
	
	public AnalyserSettings(String settingsFilePath) throws FileNotFoundException
	{
		super(settingsFilePath);
	}

	public AnalyserSettings(File file) throws FileNotFoundException
	{
		super(file);
	}


	public void updateRecentList(String chosenPath)
	{
		String[] recent = getArray(RECENT_PROJECTS);
		ArrayList<String> list = new ArrayList<String>();
		
		list.add(chosenPath);
		
		if (recent != null)
		{
			for (String string : recent)
			{
				if (!string.equalsIgnoreCase(chosenPath))
					list.add(string);
			}
		}
		
		int elementCount = (list.size() > RECENT_ELEMENTS_COUNT ? RECENT_ELEMENTS_COUNT : list.size());
		recent = list.subList(0, elementCount).toArray(new String[0]);
		
		set(RECENT_PROJECTS, recent);
	}
}
