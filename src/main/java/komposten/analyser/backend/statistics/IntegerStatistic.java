package komposten.analyser.backend.statistics;

public class IntegerStatistic extends DoubleStatistic
{
	private static final long serialVersionUID = 0L;
	
	protected int intValue;

	public IntegerStatistic(int value, int threshold)
	{
		super(value, threshold);
		
		this.intValue = value;
	}
	
	
	@Override
	public String asReadableString()
	{
		return Integer.toString(intValue);
	}
}
