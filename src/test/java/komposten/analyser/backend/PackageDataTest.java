package komposten.analyser.backend;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import komposten.analyser.backend.GraphCycleFinder.GraphNode;

class PackageDataTest
{
	static PackageData source;
	static PackageData target1;
	static PackageData target2;
	static Dependency dependency1;
	static Dependency dependency2;


	@BeforeAll
	static void setUp()
	{
		source = new PackageData("source");
		target1 = new PackageData("target1");
		target2 = new PackageData("target2");
		
		dependency1 = new Dependency(target1, source);
		dependency2 = new Dependency(target2, source);
		
		source.dependencies = new Dependency[] { dependency1, dependency2 };
	}
	

	@Test
	void getSuccessorNodes()
	{
		GraphNode[] successors = source.getSuccessorNodes();
		
		assertArrayEquals(new GraphNode[] { target1, target2 }, successors);
	}

	
	@Test
	void getDependencyForPackage_existingDependency()
	{
		assertEquals(dependency2, source.getDependencyForPackage(target2));
	}

	
	@Test
	void getDependencyForPackage_invalidDependency()
	{
		assertNull(source.getDependencyForPackage(new PackageData("target3")), "should be null since the dependency doesn't exist!");
	}
	
	
	@Test
	void hashCode_()
	{
		assertEquals(source.fullName.hashCode(), source.hashCode());
	}
	
	
	@Test
	void equals_sameName_true()
	{
		PackageData clone = new PackageData("source");
		assertTrue(source.equals(clone), "should be equal since same package name!");
	}
	
	
	@Test
	void equals_bothNullName_true()
	{
		assertTrue(new PackageData(null).equals(new PackageData(null)), "should be equal since same package name!");
	}
	
	
	@Test
	void equals_differentName_false()
	{
		assertFalse(source.equals(new PackageData("src")), "should not be equal to package with different name!");
	}
	
	
	@Test
	void equals_oneNullName_false()
	{
		assertFalse(source.equals(new PackageData(null)), "should not be equal to package with different name!");
		assertFalse(new PackageData(null).equals(source), "should not be equal to package with different name!");
	}
	
	
	@Test
	void equals_sameObject_true()
	{
		assertTrue(source.equals(source), "should be equal since same instance!");
	}
	
	
	@Test
	void equals_null_false()
	{
		assertFalse(source.equals(null), "should never be equal to null!");
	}
	
	
	@Test
	void equals_differentClass_false()
	{
		assertFalse(source.equals(dependency1), "should not be equal to object of different class!");
	}
}
