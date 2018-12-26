package komposten.analyser.backend.parsers;

import java.util.ArrayList;
import java.util.List;

final class UnitTypeStatistics
{
	double mean;
	int min;
	int max;
	String minName;
	String maxName;
	String minLocation;
	String maxLocation;
	int lengthSum;
	int count;
	List<Integer> lengths = new ArrayList<>();
	
	
	public UnitTypeStatistics()
	{
		clear();
	}
	
	
	void clear()
	{
		mean = -1;
		min = -1;
		max = -1;
		minName = null;
		maxName = null;
		minLocation = null;
		maxLocation = null;
		lengthSum = 0;
		count = 0;
		lengths.clear();
	}
	
	
	void mergeWith(UnitTypeStatistics other)
	{
		if (min < 0 || (other.min != -1 && other.min < min))
		{
			min = other.min;
			minName = other.minName;
			minLocation = other.minLocation;
		}
		
		if (other.max > max)
		{
			max = other.max;
			maxName = other.maxName;
			maxLocation = other.maxLocation;
		}
		
		lengthSum = lengthSum + other.lengthSum;
		count = count + other.count;
		mean = lengthSum / (double)count;
		lengths.addAll(other.lengths);
	}
}
