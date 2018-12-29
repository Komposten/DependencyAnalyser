package komposten.analyser.backend.util;

import komposten.utilities.tools.MathOps;

public class FrequencyStatistic extends IntegerStatistic
{
	private static final long serialVersionUID = 0L;
	
	private int[] allValues;
	
	public FrequencyStatistic(double value, int[] allValues, int threshold)
	{
		this(MathOps.round(value), allValues, threshold);
	}
	
	
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
