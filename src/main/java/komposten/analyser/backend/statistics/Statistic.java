package komposten.analyser.backend.statistics;

import java.io.Serializable;

public abstract class Statistic implements Serializable
{
	protected double value;
	protected double threshold;
	
	public Statistic(double value, double threshold)
	{
		this.value = value;
		this.threshold = threshold;
	}
	
	
	public double getValue()
	{
		return value;
	}
	
	
	public double getThreshold()
	{
		return threshold;
	}
	
	
	public abstract String asReadableString();
	
	
	@Override
	public String toString()
	{
		return asReadableString();
	}
}
