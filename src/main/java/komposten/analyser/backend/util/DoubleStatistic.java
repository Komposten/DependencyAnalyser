package komposten.analyser.backend.util;

public class DoubleStatistic extends Statistic
{
	private double value;
	
	public DoubleStatistic(double value)
	{
		this.value = value;
	}
	

	@Override
	public String asReadableString()
	{
		return String.format("%.02f", value);
	}
}
