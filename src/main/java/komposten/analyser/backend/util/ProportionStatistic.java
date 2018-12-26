package komposten.analyser.backend.util;

public class ProportionStatistic extends DoubleStatistic
{
	private double[] allValues;
	private double maxValue;

	public ProportionStatistic(double value, double[] allValues, double threshold)
	{
		super(value, threshold);
		
		this.allValues = allValues;
		for (int i = 0; i < allValues.length; i++)
			this.maxValue += allValues[i];
	}
	
	
	public double[] getAllValues()
	{
		return allValues;
	}
	
	
	@Override
	public String asReadableString()
	{
		return String.format("%.01f %", (value/maxValue) * 100);
	}
}
