package komposten.analyser.backend.statistics;

public class StatisticLink<T>
{
	private T value;
	private Statistic target;

	public StatisticLink(T value, Statistic target)
	{
		this.value = value;
		this.target = target;
	}
	
	
	public T getValue()
	{
		return value;
	}
	
	
	public Statistic getTarget()
	{
		return target;
	}
	
	
	@Override
	public String toString()
	{
		return value.toString();
	}
}
