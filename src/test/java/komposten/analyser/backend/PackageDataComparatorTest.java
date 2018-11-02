package komposten.analyser.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

class PackageDataComparatorTest
{

	@Test
	void compare_equal()
	{
		Comparator<PackageData> comp = new PackageDataComparator();
		
		PackageData package1 = new PackageData("package");
		PackageData package2 = new PackageData("package");
		
		assertEquals(0, comp.compare(package1, package2));
	}


	@Test
	void compare_sameLevel()
	{
		Comparator<PackageData> comp = new PackageDataComparator();
		
		PackageData package1 = new PackageData("package.sub");
		PackageData package2 = new PackageData("package.dub");
		
		assertEquals(1, comp.compare(package1, package2));
		assertEquals(-1, comp.compare(package2, package1));
	}


	@Test
	void compare_subPackage()
	{
		Comparator<PackageData> comp = new PackageDataComparator();
		
		PackageData package1 = new PackageData("package.sub");
		PackageData package2 = new PackageData("package.sub.subsub");
		
		assertEquals(-1, comp.compare(package1, package2));
		assertEquals(1, comp.compare(package2, package1));
	}
}
