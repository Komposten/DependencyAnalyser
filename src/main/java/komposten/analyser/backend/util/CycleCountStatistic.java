package komposten.analyser.backend.util;

public class CycleCountStatistic extends IntegerStatistic
{
	private int cycleLimit;


	public CycleCountStatistic(int value, int threshold, int cycleLimit)
	{
		super(value, threshold);
		this.cycleLimit = cycleLimit;
	}


	@Override
	public String asReadableString()
	{
		if (value > cycleLimit)
			return String.format(">%d", cycleLimit);
		else
			return super.asReadableString();
	}
}
