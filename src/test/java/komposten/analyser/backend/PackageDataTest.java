package komposten.analyser.backend;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
	
	
	@Nested
	static class FileTests
	{
		File[] files;
		PackageData data1;
		PackageData data2;
		
		@BeforeEach
		void setUp()
		{
			files = new File[]
					{
							new File("file1"),
							new File("file2"),
							new File("file3")
					};
			
			data1 = new PackageData("data1");
			data2 = new PackageData("data2", null, files);
		}

		
		@Test
		void getSourceFiles_noFiles_empty()
		{
			assertNotNull(data1.getSourceFiles());
			assertTrue(data1.getSourceFiles().isEmpty());
		}
		
		
		@Test
		void setSourceFiles_fromArray()
		{
			data1.setSourceFiles(files);
			
			assertNotNull(data1.getSourceFiles());
			assertEquals(3, data1.getSourceFiles().size());
		}
		
		
		@Test
		void setSourceFiles_fromCollection()
		{
			Collection<File> fileCollection = Arrays.asList(files);
			data1.setSourceFiles(fileCollection);
			
			assertNotNull(data1.getSourceFiles());
			assertEquals(3, data1.getSourceFiles().size());
		}
		
		
		@Test
		void getSourceFileByName_existingFile()
		{
			assertEquals(files[1], data2.getSourceFileByName("file2"));
		}
		
		
		@Test
		void getSourceFileByName_invalidFile_null()
		{
			assertNull(data2.getSourceFileByName("iDontExist"));
		}
	}
	
	
	@Nested
	static class CycleTests
	{
		static PackageData data1;
		static PackageData data2;
		static PackageData data3;
		static PackageData data4;
		static PackageData data5;
		
		@BeforeAll
		static void setUpCycles()
		{
			data1 = new PackageData("data1");
			data2 = new PackageData("data2");
			data3 = new PackageData("data3");
			data4 = new PackageData("data4");
			data5 = new PackageData("data5");

			Cycle cycle1 = new Cycle(new PackageData[] { data1, data2 });
			Cycle cycle2 = new Cycle(new PackageData[] { data2, data3 });
			
			data1.isInCycle = true;
			data1.cycles.add(cycle1);
			data2.isInCycle = true;
			data2.cycles.add(cycle1);
			data2.cycles.add(cycle2);
			data3.isInCycle = true;
			data3.cycles.add(cycle2);
		}
		
		
		@Test
		void sharesCycleWith_isNotInCycle_false()
		{
			assertFalse(data4.sharesCycleWith(data5));
		}
		
		
		@Test
		void sharesCycleWith_otherIsNotInCycle_false()
		{
			assertFalse(data1.sharesCycleWith(data5));
		}
		
		
		@Test
		void sharesCycleWith_noSharedCycle_false()
		{
			assertFalse(data1.sharesCycleWith(data3));
		}
		
		
		@Test
		void sharesCycleWith_sharedCycle_true()
		{
			assertTrue(data1.sharesCycleWith(data2));
		}
	}
}
