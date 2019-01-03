package komposten.analyser.backend;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class PackageProperties implements Serializable
{
	private final Map<String, Object> values;

	public PackageProperties()
	{
		this.values = new LinkedHashMap<>();
	}
	
	
	public int count()
	{
		return values.size();
	}
	
	
	public Map<String, Object> getValues()
	{
		return values;
	}
	
	
	public void set(String key, Object value)
	{
		values.put(key, value);
	}
	
	
	public Object get(String key)
	{
		return values.get(key);
	}
	
	
	public String getString(String key)
	{
		Object value = get(key);
		
		if (value != null && value instanceof String)
			return (String) value;
		
		return null;
	}
	
	
	public Integer getInteger(String key)
	{
		Object value = get(key);
		
		if (value != null && value instanceof Integer)
			return (Integer) value;
		
		return null;
	}
	
	
	/**
	 * Adds all data from <code>other</code> to this
	 * <code>PackageProperties</code> instance.
	 * 
	 * @param other
	 * @param overwriteExisting How to handle keys present in both objects.
	 *          <code>true</code> means that the values in <code>other</code> will
	 *          overwrite values in this object when necessary.
	 */
	public void merge(PackageProperties other, boolean overwriteExisting)
	{
		for (Map.Entry<String, Object> entry : other.values.entrySet())
		{
			Object value = values.get(entry.getKey());
			if (value != null && value instanceof PackageProperties && entry.getValue() instanceof PackageProperties)
			{
				((PackageProperties)value).merge((PackageProperties) entry.getValue(), overwriteExisting);
			}
			else if (overwriteExisting)
				values.put(entry.getKey(), entry.getValue());
			else
				values.putIfAbsent(entry.getKey(), entry.getValue());
		}
	}
}