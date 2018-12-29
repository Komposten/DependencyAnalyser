package komposten.analyser.backend.statistics;

public class DoubleStatistic extends Statistic
{
	private static final long serialVersionUID = 0L;
	
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
