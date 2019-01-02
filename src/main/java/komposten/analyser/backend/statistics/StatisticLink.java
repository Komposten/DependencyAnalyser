package komposten.analyser.backend.statistics;

import java.io.Serializable;

public interface StatisticLink<V> extends Serializable
{
	public V getValue();
	public Statistic getLinkTarget();
}
