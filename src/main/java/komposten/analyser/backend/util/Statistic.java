package komposten.analyser.backend.util;

public abstract class Statistic
{
	public abstract String asReadableString();
	
	
	@Override
	public String toString()
	{
		return asReadableString();
	}
}
