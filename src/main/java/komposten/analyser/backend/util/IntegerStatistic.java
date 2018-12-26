package komposten.analyser.backend.util;

public class IntegerStatistic extends DoubleStatistic
{
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
