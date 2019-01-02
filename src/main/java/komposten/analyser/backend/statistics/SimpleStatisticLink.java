package komposten.analyser.backend.statistics;

public class SimpleStatisticLink<T> implements StatisticLink<T>
{
	private static final long serialVersionUID = 0L;
	
	private T value;
	private Statistic target;

	public SimpleStatisticLink(T value, Statistic target)
	{
		this.value = value;
		this.target = target;
	}
	
	
	@Override
	public T getValue()
	{
		return value;
	}
	
	
	@Override
	public Statistic getLinkTarget()
	{
		return target;
	}
	
	
	@Override
	public String toString()
	{
		return value.toString();
	}
}
