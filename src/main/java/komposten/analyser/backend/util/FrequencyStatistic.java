package komposten.analyser.backend.util;

public class FrequencyStatistic extends IntegerStatistic
{
	private static final long serialVersionUID = 0L;
	
	private int[] allValues;
	
	public FrequencyStatistic(int value, int[] allValues, int threshold)
	{
		super(value, threshold);
		this.allValues = allValues;
	}
	
	
	public int[] getAllValues()
	{
		return allValues;
	}
}
