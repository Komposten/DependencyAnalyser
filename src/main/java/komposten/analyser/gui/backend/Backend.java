package komposten.analyser.gui.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import komposten.analyser.backend.Analyser;
import komposten.analyser.backend.Edge;
import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.analysis.AnalysisListener;
import komposten.analyser.tools.Settings;
import komposten.analyser.tools.Settings.SettingChangeListener;
import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;

public class Backend
{
	public static final String SELECTED_PACKAGE = "selectedPackage";
	public static final String SELECTED_UNIDIRECTIONAL_EDGE = "selectedUniEdge";
	public static final String SELECTED_BIDIRECTIONAL_EDGE = "selectedBiEdge";
	/**
	 * This property can be used to be notified when uni- or bidirectional edges
	 * are selected, but will not allow the listener to distinguish between the
	 * two. If the nature of the edge is important, use
	 * {@link #SELECTED_UNIDIRECTIONAL_EDGE} and/or
	 * {@link #SELECTED_BIDIRECTIONAL_EDGE} instead.
	 */
	public static final String SELECTED_EDGE = "selectedEdge";
	
	private Analyser analyser;
	private AnalyserSettings settings;
	private HashMap<String, ArrayList<PropertyChangeListener>> listeners;
	
	public Backend()
	{
		analyser = new Analyser();
		
		String settingsFilePath = "data/settings.ini";
		try
		{
			settings = new AnalyserSettings(settingsFilePath);
		}
		catch (FileNotFoundException e)
		{
			String msg = "The settings file does not exist. Using default settings!";
			if (LogUtils.hasInitialised())
				LogUtils.log(Level.INFO, msg);
			
			//FIXME Make it possible to create a settings object even if the file doesn't exist!
			try
			{
				File file = new File(settingsFilePath);
				file.getParentFile().mkdirs();
				file.createNewFile();
				settings = new AnalyserSettings(file);
			}
			catch (IOException e2)
			{
				String msg2 = "Could not create the settings file!";
				if (LogUtils.hasInitialised())
					LogUtils.log(Level.ERROR, Backend.class.getName(), msg2, e2, false);
			}
		}
		settings.addListener(settingChangedListener);
		listeners = new HashMap<>();
	}
	


	/**
	 * Adds a {@link PropertyChangeListener} that is called whenever one of the
	 * properties in <code>propertiesToListenTo</code> is changed.<br />
	 * Listeners will also be called when <i>settings</i> in the {@link Settings
	 * Settings object} are changed (with the setting's name being the property).
	 * <p>
	 * <b>Note:</b> The listener is not added if <code>propertiesToListenTo</code>
	 * is empty.
	 * </p>
	 * 
	 * @param listener
	 * @param propertiesToListenTo The properties that trigger the listener when
	 *          changed. If no properties are provided, the listener is not added.
	 *          A property can either be one of the constants in this class, or a
	 *          setting contained in the {@link Settings Settings object}.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener, String... propertiesToListenTo)
	{
		for (String string : propertiesToListenTo)
			addPropertyChangeListener(listener, string);
	}
	
	
	private void addPropertyChangeListener(PropertyChangeListener listener, String setting)
	{
		ArrayList<PropertyChangeListener> list = listeners.get(setting);
		
		if (list != null)
		{
			list.add(listener);
		}
		else
		{
			list = new ArrayList<>();
			list.add(listener);
			listeners.put(setting, list);
		}
	}
	
	
	public void addAnalysisListener(AnalysisListener analysisListener)
	{
		analyser.addListener(analysisListener);
	}



	public Analyser getAnalyser()
	{
		return analyser;
	}
	
	
	public AnalyserSettings getSettings()
	{
		return settings;
	}
	
	
	public void setSelectedPackage(PackageData packageData)
	{
		if (packageData != null)
		{
			if (packageData.isInCycle && packageData.cycles.isEmpty())
				analysePackageCycles(packageData);
			
			if (!packageData.isExternal)
				notifyListeners(SELECTED_PACKAGE, packageData);
		}
	}
	
	
	private void analysePackageCycles(PackageData packageData)
	{
		if (packageData != null)
		{
			analyser.analysePackage(packageData);
		}
	}


	public void setSelectedEdge(Edge edge, boolean isBidirectional)
	{
		String property = (isBidirectional ? SELECTED_BIDIRECTIONAL_EDGE : SELECTED_UNIDIRECTIONAL_EDGE);
		notifyListeners(property, edge);
		notifyListeners(SELECTED_EDGE, edge);
	}
	
	
	public void analyseFolder(File file)
	{
		analyseFolder(file.getPath());
	}
	
	
	public void analyseFolder(final String folder)
	{
		//FIXME Backend; If "folder" is the same as the last analysed folder, ask to analyse again (something might have changed, so it must be possible to re-analyse)?
		if (folder != null)
		{
			boolean analyseComments = settings.getBoolean(AnalyserSettings.ANALYSE_COMMENTS);
			boolean analyseStrings = settings.getBoolean(AnalyserSettings.ANALYSE_STRINGS);
			String threadCountString = settings.get(AnalyserSettings.THREAD_COUNT);
			int threadCount = (threadCountString == null ? settings.getDefaultThreadCount() : Integer.parseInt(threadCountString));
			analyser.analyseSource(folder, analyseComments, analyseStrings, threadCount);
		}
	}
	
	
	public void abortAnalysis()
	{
		analyser.abortAnalysis();
	}
	
	
	private void notifyListeners(String property, Object value)
	{
		ArrayList<PropertyChangeListener> list = listeners.get(property);
		
		if (list != null)
		{
			for (PropertyChangeListener listener : list)
				listener.propertyChanged(property, value);
		}
	}
	
	
	private SettingChangeListener settingChangedListener = new SettingChangeListener()
	{
		@Override
		public void settingChanged(String settingKey, Object value, Settings settings)
		{
			notifyListeners(settingKey, value);
		}
	};
	
	
	public static interface PropertyChangeListener
	{
		public void propertyChanged(String key, Object value);
	}
}
