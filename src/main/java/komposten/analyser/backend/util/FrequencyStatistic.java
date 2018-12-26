package komposten.analyser.backend.util;

public class FrequencyStatistic extends Statistic
{
	private int intValue;
	private int[] allValues;
	
	public FrequencyStatistic(int value, int[] allValues, int threshold)
	{
		super(value, threshold);
		this.intValue = value;
		this.allValues = allValues;
	}
	
	
	@Override
	public String asReadableString()
	{
		return Integer.toString(intValue);
	}
}
