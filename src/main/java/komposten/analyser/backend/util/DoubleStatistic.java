package komposten.analyser.backend.util;

public class DoubleStatistic extends Statistic
{
	public DoubleStatistic(double value, double threshold)
	{
		super(value, threshold);
	}
	

	@Override
	public String asReadableString()
	{
		return String.format("%.02f", value);
	}
}
