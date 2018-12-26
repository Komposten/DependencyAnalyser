package komposten.analyser.backend.util;

public class ProportionStatistic extends Statistic
{
	private double value;
	private double[] allValues;
	private double maxValue;

	public ProportionStatistic(double value, double[] allValues)
	{
		this.value = value;
		this.allValues = allValues;
		for (int i = 0; i < allValues.length; i++)
			this.maxValue += allValues[i];
	}
	
	
	@Override
	public String asReadableString()
	{
		return String.format("%.01f %", (value/maxValue) * 100);
	}
}
