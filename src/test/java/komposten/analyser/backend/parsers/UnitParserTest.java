package komposten.analyser.backend.parsers;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import komposten.analyser.backend.parsers.UnitParser.AnonymousClassUnit;
import komposten.analyser.backend.parsers.UnitParser.ClassUnit;
import komposten.analyser.backend.parsers.UnitParser.ClassUnit.Type;
import komposten.analyser.backend.parsers.UnitParser.FileUnit;
import komposten.analyser.backend.parsers.UnitParser.MethodUnit;
import komposten.analyser.backend.parsers.UnitParser.Unit;
import komposten.analyser.backend.util.Constants;
import komposten.analyser.gui.backend.AnalyserSettings;
import komposten.analyser.tools.Settings;

class UnitParserTest
{
	private static Settings settings;
	

	@BeforeAll
	static void setupSettings() throws IOException
	{
		File settingsFile = File.createTempFile("dependency_analyser_tmp", ".ini");
		settings = new AnalyserSettings(settingsFile);
		settings.set(Constants.SettingKeys.FILE_LENGTH_THRESHOLD, "0");
		settings.set(Constants.SettingKeys.CLASS_LENGTH_THRESHOLD, "0");
		settings.set(Constants.SettingKeys.METHOD_LENGTH_THRESHOLD, "0");
	}
	
	private FileUnit parseStringList(List<String> linesToParse)
	{
		UnitParser parser = new UnitParser(settings);
		
		parser.nextFile(new File(""));
		
		for (String unitString : linesToParse)
		{
			parser.parseLine(unitString, unitString);
		}
		
		parser.postFile();

		return parser.getFileUnits().iterator().next();
	}
	
	@TestInstance(Lifecycle.PER_CLASS)
	@Nested
	class TypeUnits
	{
		private List<Unit> units;
		private List<String> unitStrings;
		
		@BeforeAll
		private void generateDefinitions()
		{
			units = new ArrayList<>();
			unitStrings = new ArrayList<>();
			
			generateClassDefinitions(units, unitStrings);
			generateEnumDefinitions(units, unitStrings);
			generateInterfaceDefinitions(units, unitStrings);
			generateAnonymousDefinitions(units, unitStrings);
		}

		private void generateClassDefinitions(List<Unit> units, List<String> unitStrings)
		{
			String[] access = { "", "public", "protected", "private" };
			String[] modifiers = { "", "abstract", "final", "static", "static abstract", "static final" };
			Type type = Type.Class;
			String[] names = { "Class", "Class<T>", "Class<K, V>" };
			String[] extend = { "", "Parent", "Parent<Other>", "Parent<Other, Other2>" };
			String[] implement = { "", "Parent", "Parent<Other>", "Parent<Other, Other2>", "Parent, Parent2", "Parent<Other>, Parent2<Other>" };
			
			for (String accessLevel : access)
			{
				for (String modifier : modifiers)
				{
					for (String name : names)
					{
						for (String extendClause : extend)
						{
							for (String implClause : implement)
							{
								String combinedMods = (accessLevel + " " + modifier).trim();
								ClassUnit unit = createClassUnit(combinedMods, type, name, extendClause, implClause, null);
								
								units.add(unit);
								unitStrings.add(createStringFromClassUnit(unit));
							}
						}
					}
				}
			}
		}
		
		private void generateInterfaceDefinitions(List<Unit> units, List<String> unitStrings)
		{
			String[] access = { "", "public", "protected", "private" };
			String[] modifiers = { "", "abstract", "static", "static abstract", "abstract static" };
			Type type = Type.Interface;
			String[] names = { "Class", "Class<T>", "Class<K, V>" };
			String[] extend = { "", "Parent", "Parent<Other>", "Parent<Other, Other2>" };
			
			for (String accessLevel : access)
			{
				for (String modifier : modifiers)
				{
					for (String name : names)
					{
						for (String extendClause : extend)
						{
							String combinedMods = (accessLevel + " " + modifier).trim();
							ClassUnit unit = createClassUnit(combinedMods, type, name, extendClause, "", null);

							units.add(unit);
							unitStrings.add(createStringFromClassUnit(unit));
						}
					}
				}
			}
		}

		private void generateEnumDefinitions(List<Unit> units, List<String> unitStrings)
		{
			String[] access = { "", "public", "protected", "private" };
			String[] modifiers = { "", "static" };
			Type type = Type.Enum;
			String name = "AnEnum";
			String[] implement = { "", "Parent", "Parent<Other>", "Parent<Other, Other2>", "Parent, Parent2", "Parent<Other>, Parent2<Other>" };
			
			for (String accessLevel : access)
			{
				for (String modifier : modifiers)
				{
					for (String implClause : implement)
					{
						String combinedMods = (accessLevel + " " + modifier).trim();
						ClassUnit unit = createClassUnit(combinedMods, type, name, "", implClause, null);

						units.add(unit);
						unitStrings.add(createStringFromClassUnit(unit));
					}
				}
			}
		}

		private String createStringFromClassUnit(ClassUnit unit)
		{
			String typeString = String.format("%s %s %s", unit.modifiers, unit.classType.toString().toLowerCase(), unit.name);
			if (!unit.extendClause.isEmpty())
				typeString += " extends " + unit.extendClause;
			if (!unit.implementsClause.isEmpty())
				typeString += " implements " + unit.implementsClause;
			typeString += " {}";
			return typeString;
		}
		
		private void generateAnonymousDefinitions(List<Unit> units, List<String> unitStrings)
		{
			String[] access = { "", "public", "protected", "private" };
			String[] modifiers = { "", "final", "static", "final static", "static final" };
			String[] types = { "Class", "Class<T>", "Class<K, V>" };
			String name = "field";
			
			ClassUnit parentUnit = new ClassUnit("ParentOfAnonymous", null);
			parentUnit.classType = Type.Class;
			units.add(parentUnit);
			unitStrings.add("class ParentOfAnonymous {");
			
			//access modifier type name = new type
			for (String accessLevel : access)
			{
				for (String modifier : modifiers)
				{
					for (String type : types)
					{
						for (String type2 : types)
						{
							AnonymousClassUnit unit = new AnonymousClassUnit(name, parentUnit);
							unit.extendedType = type2;
							units.add(unit);
							unitStrings.add(String.format("%s %s %s %s = new %s () {}", accessLevel, modifier, type, name, type2));
							
							unit = new AnonymousClassUnit("", parentUnit);
							unit.extendedType = type2;
							units.add(unit);
							unitStrings.add(String.format("new %s () {}", type2));
						}
					}
				}
			}
			
			unitStrings.add("}");
		}
		
		private ClassUnit createClassUnit(String modifiers, Type type, String name, String extend, String implement, Unit parent)
		{
			ClassUnit unit = new ClassUnit(name, parent);
			unit.type = Unit.Type.Class;
			unit.modifiers = modifiers;
			unit.classType = type;
			unit.extendClause = extend;
			unit.implementsClause = implement;
			return unit;
		}
		
		@Test
		void testClassRecognition()
		{
			FileUnit fileUnit = parseStringList(unitStrings);
			
			List<Unit> actualUnits = new ArrayList<>(units.size());
			actualUnits.addAll(fileUnit.children);
			actualUnits.addAll(fileUnit.children.get(fileUnit.children.size()-1).children);
			
			assertEquals(units.size(), actualUnits.size());
			
			for (int i = 0; i < units.size(); i++)
			{
				if (units.get(i) instanceof ClassUnit)
				{
					assertThat(actualUnits.get(i)).isInstanceOf(ClassUnit.class);
	
					ClassUnit expected = (ClassUnit) units.get(i);
					ClassUnit actual = (ClassUnit) actualUnits.get(i);
					
					assertEquals(expected.modifiers, actual.modifiers);
					assertEquals(expected.classType, actual.classType);
					assertEquals(expected.name, actual.name);
					assertEquals(expected.extendClause, actual.extendClause);
					assertEquals(expected.implementsClause, actual.implementsClause);
				}
				else if (actualUnits.get(i) instanceof AnonymousClassUnit)
				{
					assertThat(actualUnits.get(i)).isInstanceOf(AnonymousClassUnit.class);
	
					AnonymousClassUnit expected = (AnonymousClassUnit) units.get(i);
					AnonymousClassUnit actual = (AnonymousClassUnit) actualUnits.get(i);
					
					assertEquals(expected.name, actual.name);
					assertEquals(expected.extendedType, actual.extendedType);
				}
				else
				{
					fail(String.format("typeUnits.get(%d) is neither a ClassUnit nor an AnonymousClassUnit: %s", i, units.get(i).getClass()));
				}
			}
		}
	}
	
	@TestInstance(Lifecycle.PER_CLASS)
	@Nested
	class MethodUnits
	{
		private List<Unit> units;
		private List<String> unitStrings;
		
		@BeforeAll
		private void generateDefinitions()
		{
			units = new ArrayList<>();
			unitStrings = new ArrayList<>();
			
			ClassUnit parentUnit = new ClassUnit("Parent", null);
			parentUnit.modifiers = parentUnit.extendClause = parentUnit.implementsClause = "";
			parentUnit.classType = Type.Class;
			unitStrings.add("class Parent {");
			
			generateMethodDefinitions(units, unitStrings, parentUnit);
			generateConstructorDefinitions(units, unitStrings, parentUnit);
			generateInitialiserDefinitions(units, unitStrings, parentUnit);
			
			unitStrings.add("}");
		}
		
		private void generateMethodDefinitions(List<Unit> units,
				List<String> unitStrings, Unit parentUnit)
		{
			String[] access = { "", "public", "protected", "private" };
			String[] modifiers = { "", "abstract", "final", "static", "static final", "default" };
			String[] returnTypes = { "void", "Class", "Class[]", "Class<Other>", "Class<Other, Other2>" };
			String name = "method";
			String[] params = { "", "Class clazz", "Class<Other> clazz", "Class<Other, Other> clazz", "Class clazz, Other other", "Class[] classes", "Class classes[]" };
			
			for (String accessLevel : access)
			{
				for (String modifier : modifiers)
				{
					for (String returnType : returnTypes)
					{
						for (String param : params)
						{
							String combinedMods = (accessLevel + " " + modifier).trim();
							MethodUnit unit = createMethodUnit(combinedMods, returnType, name, param, Unit.Type.Method, parentUnit);

							units.add(unit);

							if (modifier.equals("abstract"))
							{
								//CURRENT Not working?
								unitStrings.add(String.format("%s %s %s (%s);", combinedMods, returnType, name, param));
							}
							else
							{
								unitStrings.add(String.format("%s %s %s (%s) {}", combinedMods, returnType, name, param));
							}
						}
					}
				}
			}

		}

		private void generateConstructorDefinitions(List<Unit> units,
				List<String> unitStrings, Unit parentUnit)
		{
			String[] access = { "", "public", "protected", "private" };
			String name = parentUnit.name;
			
			for (String accessLevel : access)
			{
				MethodUnit unit = createMethodUnit(accessLevel, "", name, "", Unit.Type.Constructor, parentUnit);

				units.add(unit);
				unitStrings.add(String.format("%s %s() {}", accessLevel, name));
			}
		}

		private void generateInitialiserDefinitions(List<Unit> units,
				List<String> unitStrings, Unit parentUnit)
		{
			MethodUnit initialiser = createMethodUnit("", "", "<initialiser>", "", Unit.Type.Initialiser, parentUnit);
			MethodUnit staticInitialiser = createMethodUnit("static", "", "", "", Unit.Type.Initialiser, parentUnit);
			
			units.add(initialiser);
			units.add(staticInitialiser);
			
			unitStrings.add("{ }");
			unitStrings.add("static { }");
		}
		
		private MethodUnit createMethodUnit(String modifiers, String returnType, String name, String parameterClause, Unit.Type type, Unit parent)
		{
			MethodUnit unit = new MethodUnit(name, parent);
			unit.modifiers = modifiers;
			unit.returnType = returnType;
			unit.parameterClause = parameterClause;
			unit.type = type;
			return unit;
		}

		@Test
		void testMethodRecognition()
		{
			FileUnit fileUnit = parseStringList(unitStrings);
			
			List<Unit> actualUnits = new ArrayList<>(units.size());
			actualUnits.addAll(fileUnit.children.get(0).children);
			
			assertEquals(units.size(), actualUnits.size());
			
			for (int i = 0; i < units.size(); i++)
			{
				if (units.get(i) instanceof MethodUnit)
				{
					assertThat(actualUnits.get(i)).isInstanceOf(MethodUnit.class);
	
					MethodUnit expected = (MethodUnit) units.get(i);
					MethodUnit actual = (MethodUnit) actualUnits.get(i);
					
					assertEquals(expected.modifiers, actual.modifiers);
					assertEquals(expected.returnType, actual.returnType);
					assertEquals(expected.name, actual.name);
//					assertEquals(expected.parameterClause, actual.parameterClause);
				}
				else
				{
					fail(String.format("units.get(%d) is not a MethodUnit: %s", i, units.get(i).getClass()));
				}
			}
		}
	}
}
