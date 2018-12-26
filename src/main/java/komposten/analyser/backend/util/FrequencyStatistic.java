package komposten.analyser.backend.util;

public class FrequencyStatistic extends IntegerStatistic
{
	private int[] allValues;
	
	public FrequencyStatistic(int value, int[] allValues, int threshold)
	{
		super(value, threshold);
		this.allValues = allValues;
	}
}
