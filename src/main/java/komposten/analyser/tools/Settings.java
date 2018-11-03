package komposten.analyser.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import komposten.utilities.tools.FileOperations;

public abstract class Settings //TODO Settings; Replace with the updated version in komposten utils. When doing this, see if calls to get() can be replaced by any get***().
{
	private File file;
	private Map<String, String> data;
	private Map<String, ArrayList<SettingChangeListener>> listeners;
	private ArrayList<SettingChangeListener> globalListeners;

	
	public Settings(String settingsFilePath) throws FileNotFoundException
	{
		this(new File(settingsFilePath));
	}

	
	public Settings(File file) throws FileNotFoundException
	{
		this.file = file;
		data = FileOperations.loadConfigFile(file, false);
		listeners = new HashMap<String, ArrayList<SettingChangeListener>>();
		globalListeners = new ArrayList<SettingChangeListener>();
	}
	

	/**
	 * Adds a {@link SettingChangeListener} that is called whenever one of the
	 * settings in <code>settingsToListenTo</code> is changed.<br />
	 * <b>Note:</b> If <code>settingsToListenTo</code> is empty, the listener will
	 * be called for all settings!
	 * 
	 * @param listener
	 * @param settingsToListenTo The settings that trigger the listener when
	 *          changed. If no settings are provided, the listener is triggered
	 *          for all settings.
	 */
	public void addListener(SettingChangeListener listener, String... settingsToListenTo)
	{
		if (settingsToListenTo.length == 0)
		{
			globalListeners.add(listener);
		}
		else
		{
			for (String string : settingsToListenTo)
				addListener(listener, string);
		}
	}
	
	
	private void addListener(SettingChangeListener listener, String setting)
	{
		ArrayList<SettingChangeListener> list = listeners.get(setting);
		
		if (list != null)
		{
			list.add(listener);
		}
		else
		{
			list = new ArrayList<SettingChangeListener>();
			list.add(listener);
			listeners.put(setting, list);
		}
	}

	
	public String get(String key)
	{
		return data.get(key);
	}
	
	
	/**
	 * @return <code>true</code> if the value stored under <code>key</code> is
	 *         equal to <code>true</code>, false otherwise.
	 */
	public boolean getBoolean(String key)
	{
		String value = data.get(key);
		
		if (value != null)
			return value.equals(Boolean.TRUE.toString());
		return false;
	}
	
	
	
	/**
	 * @return the value mapped to <code>key</code> as an array by
	 *         {@link String#split(String) splitting} at <code>\\s*,\\s*</code>, or
	 *         <code>null</code> if no value was found.
	 */
	public String[] getArray(String key)
	{
		String value = data.get(key);
		
		if (value != null)
			return value.split("\\s*,\\s*");
		return null;
	}
	
	
	public void set(String key, String[] values)
	{
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < values.length; i++)
		{
			String value = values[i];
			
			if (i == 0)
				builder.append(value);
			else
				builder.append(", " + value);
		}
		
		data.put(key, builder.toString());
		notifyListeners(key, values);
	}


	public void set(String key, String value)
	{
		data.put(key, value);
		notifyListeners(key, value);
	}
	
	
	public void saveToFile() throws IOException
	{
		FileOperations.createFileOrFolder(file, false);
		
		StringBuilder builder = new StringBuilder();
		
		for (Entry<String, String> entry : data.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			
			builder.append(key + "=" + value + "\n");
		}
		
		FileOperations fileOperations = new FileOperations();
		fileOperations.printData(file, builder.toString(), false, false);
	}
	
	
	private void notifyListeners(String key, Object value)
	{
		ArrayList<SettingChangeListener> list = listeners.get(key);
		
		if (list != null)
		{
			for (SettingChangeListener listener : list)
				listener.settingChanged(key, value, this);
		}
		
		for (SettingChangeListener listener : globalListeners)
			listener.settingChanged(key, value, this);
	}
	
	
	public static interface SettingChangeListener
	{
		public void settingChanged(String settingKey, Object value, Settings settings);
	}
}
