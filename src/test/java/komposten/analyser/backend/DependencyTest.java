package komposten.analyser.backend;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DependencyTest
{
	private PackageData source;
	private PackageData target;
	private Dependency dependency;


	@BeforeEach
	void setUp() throws URISyntaxException
	{
		try
		{
			source = new PackageData("testpackages.package3", new File(DependencyTest.class.getResource("/testpackages/package3").toURI()), new File[0]);
			target = new PackageData("testpackages.package4", new File(DependencyTest.class.getResource("/testpackages/package4").toURI()), new File[0]);
		}
		catch (URISyntaxException e)
		{
			throw e;
		}
		
		dependency = new Dependency(target, source);
	}

	@Test
	void constructor()
	{
		assertEquals(source, dependency.source);
		assertEquals(target, dependency.target);
		assertEquals(0, dependency.byCompilationUnit.size());
	}


	@Test
	void addClass()
	{
		String className = "File3_1";
		String[] references = new String[]
				{
						"File4_1",
						"File4_2"
				};
		dependency.addReferences(className, references);
		
		assertTrue(dependency.byCompilationUnit.containsKey(className), "the class was not added to dependency map!");
		assertArrayEquals(references, dependency.byCompilationUnit.get(className));
	}
	
	
	@Test
	void addClass_twoClasses()
	{
		String className = "File3_1";
		String[] references = new String[]
				{
						"File4_1",
						"File4_2"
				};
		dependency.addReferences(className, references);
		
		className = "File3_2";
		references = new String[]
				{
						"File 2_1"
				};
		dependency.addReferences(className, references);
		
		assertTrue(dependency.byCompilationUnit.containsKey(className), "the second class was not added to dependency map!");
		assertArrayEquals(references, dependency.byCompilationUnit.get(className));
	}
	
	
	@Test
	void toString_noArgs()
	{
		dependency.addReferences("File3_1", null);
		
		String actual = dependency.toString();
		String expected = "testpackages.package4[File3_1]";
		
		assertEquals(expected, actual);
	}
	
	
	@Test
	void toString_includeAll()
	{
		dependency.addReferences("File3_1", null);
		
		String actual = dependency.toString(true, true, true);
		String expected = "testpackages.package3-->testpackages.package4[File3_1]";
		
		assertEquals(expected, actual);
	}
	
	
	@Test
	void toString_remainingCombinations()
	{
		dependency.addReferences("File3_1", null);
		
		String actual = dependency.toString(true, true, false);
		String expected = "testpackages.package3-->testpackages.package4";
		assertEquals(expected, actual);
		
		actual = dependency.toString(true, false, true);
		expected = "testpackages.package3[File3_1]";
		assertEquals(expected, actual);
		
		actual = dependency.toString(true, false, false);
		expected = "testpackages.package3";
		assertEquals(expected, actual);
		
		actual = dependency.toString(false, true, true);
		expected = "testpackages.package4[File3_1]";
		assertEquals(expected, actual);
		
		actual = dependency.toString(false, true, false);
		expected = "testpackages.package4";
		assertEquals(expected, actual);
		
		actual = dependency.toString(false, false, true);
		expected = "[File3_1]";
		assertEquals(expected, actual);
		
		actual = dependency.toString(false, false, false);
		expected = "";
		assertEquals(expected, actual);
	}
}
