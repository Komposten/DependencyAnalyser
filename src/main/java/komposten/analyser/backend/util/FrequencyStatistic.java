package komposten.analyser.backend.util;

public class FrequencyStatistic extends Statistic
{
	private int value;
	private int[] allValues;
	
	public FrequencyStatistic(int value, int[] allValues)
	{
		this.value = value;
		this.allValues = allValues;
	}
	
	
	public int getValue()
	{
		return value;
	}
	
	
	@Override
	public String asReadableString()
	{
		return Integer.toString(value);
	}
}
