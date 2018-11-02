package komposten.analyser.backend;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CycleTest
{
	private static PackageData data[];
	
	
	@BeforeAll
	static void setUp()
	{
		PackageData start = new PackageData("1");
		data = new PackageData[]
				{
						start,
						new PackageData("2"),
						new PackageData("3"),
						start
				};
	}
	

	@Test
	void getPackages()
	{
		Cycle cycle = new Cycle(data);
		assertArrayEquals(data, cycle.getPackages());
	}
	
	
	@Test
	void contains()
	{
		Cycle cycle = new Cycle(data);
		
		assertTrue(cycle.contains(data[3]));
		assertFalse(cycle.contains(new PackageData("4")));
	}
}
