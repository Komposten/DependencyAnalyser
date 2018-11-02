package komposten.analyser.backend;

import java.util.Comparator;

public class PackageDataComparator implements Comparator<PackageData>
{

	public PackageDataComparator()
	{
	}

	@Override
	public int compare(PackageData o1, PackageData o2)
	{
		if (o1.fullName.equals(o2.fullName))
			return 0;
		if (o1.fullName.startsWith(o2.fullName))
			return 1;
		else if (o2.fullName.startsWith(o1.fullName))
			return -1;
		
		int stringComp = o1.fullName.compareTo(o2.fullName);
		
		if (stringComp < 0)
			return -1;
		else if (stringComp > 0)
			return 1;
		else
			return 0;
	}
}
