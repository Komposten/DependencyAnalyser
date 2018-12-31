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
		assertEquals(0, dependency.classDependencies.size());
	}


	@Test
	void addClass()
	{
		String className = "File3_1";
		String classNameQualified = source.fullName + "." + className;
		File file = new File(source.folder.getPath() + "/" + className + ".jav");
		String[] references = new String[]
				{
						"File4_1",
						"File4_2"
				};
		dependency.addClass(file, references);
		
		assertArrayEquals(new File[] { file }, dependency.classToFileMap.values().toArray(new File[1]));
		assertTrue(dependency.classDependencies.containsKey(classNameQualified), "the class was not added to dependency map!");
		assertArrayEquals(references, dependency.classDependencies.get(classNameQualified));
	}
	
	
	@Test
	void addClass_twoClasses()
	{
		String className = "File3_1";
		String classNameQualified = source.fullName + "." + className;
		File file = new File(source.folder.getPath(), className + ".jav");
		String[] references = new String[]
				{
						"File4_1",
						"File4_2"
				};
		dependency.addClass(file, references);
		
		className = "File3_2";
		classNameQualified = source.fullName + "." + className;
		File file2 = new File(source.folder.getPath(), className + ".jav");
		references = new String[]
				{
						"File 2_1"
				};
		dependency.addClass(file2, references);
		
		assertEquals(2, dependency.classToFileMap.size());
		assertTrue(dependency.classToFileMap.containsValue(file));
		assertTrue(dependency.classToFileMap.containsValue(file2));
		assertTrue(dependency.classDependencies.containsKey(classNameQualified), "the class was not added to dependency map!");
		assertArrayEquals(references, dependency.classDependencies.get(classNameQualified));
	}
	
	
	@Test
	void toString_noArgs()
	{
		dependency.addClass(new File(source.folder, "File3_1.jav"), null);
		
		String actual = dependency.toString();
		String expected = "testpackages.package4[File3_1.jav]";
		
		assertEquals(expected, actual);
	}
	
	
	@Test
	void toString_includeAll()
	{
		dependency.addClass(new File(source.folder, "File3_1.jav"), null);
		
		String actual = dependency.toString(true, true, true);
		String expected = "testpackages.package3-->testpackages.package4[File3_1.jav]";
		
		assertEquals(expected, actual);
	}
	
	
	@Test
	void toString_remainingCombinations()
	{
		dependency.addClass(new File(source.folder, "File3_1.jav"), null);
		
		String actual = dependency.toString(true, true, false);
		String expected = "testpackages.package3-->testpackages.package4";
		assertEquals(expected, actual);
		
		actual = dependency.toString(true, false, true);
		expected = "testpackages.package3[File3_1.jav]";
		assertEquals(expected, actual);
		
		actual = dependency.toString(true, false, false);
		expected = "testpackages.package3";
		assertEquals(expected, actual);
		
		actual = dependency.toString(false, true, true);
		expected = "testpackages.package4[File3_1.jav]";
		assertEquals(expected, actual);
		
		actual = dependency.toString(false, true, false);
		expected = "testpackages.package4";
		assertEquals(expected, actual);
		
		actual = dependency.toString(false, false, true);
		expected = "[File3_1.jav]";
		assertEquals(expected, actual);
		
		actual = dependency.toString(false, false, false);
		expected = "";
		assertEquals(expected, actual);
	}
}
