package komposten.analyser.backend.statistics;

public interface StatisticLink<V>
{
	public V getValue();
	public Statistic getLinkTarget();
}
