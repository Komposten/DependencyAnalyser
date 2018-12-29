package komposten.analyser.backend.statistics;

public class CycleCountStatistic extends IntegerStatistic
{
	private static final long serialVersionUID = 0L;
	
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
