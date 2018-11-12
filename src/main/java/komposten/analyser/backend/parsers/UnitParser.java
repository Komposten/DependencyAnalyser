package komposten.analyser.backend.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.util.Constants;
import komposten.analyser.backend.util.SourceUtil;
import komposten.utilities.data.IntPair;

public class UnitParser implements SourceParser
{
	//TODO UnitParser; Does not match abstract methods/method definitions in interfaces.
	//FIXME UnitParser; Handle array initialisations (e.g. new int[] { 1, 2, 3 } or int[] array = { 1, 2, 3}).
	
	/** Matches any combination of modifiers (private, protected, public, abstract, static or final). */
	private static final String MODIFIER = "(?:(?:private|protected|public|abstract|static|final)\\s+)";
	/** Matches any valid Java identifier. */
	private static final String IDENTIFIER = "[A-Za-z_$][\\w$]*";
	/** Matches a reference to a Java Type (e.g. <code>java.lang.String</code>) */
	private static final String REFERENCE = "[A-Za-z_$][\\w$.]*?";
	/** Matches a the type parameterisation-part of a generic (i.e. the part
	 * between &lt; and &gt;). */
	private static final String TYPE_PARAMETERISATION = "(?:<[\\w$<>,\\s\\[\\]?]+?>)";
	/** Matches array brackets with zero or more white-space characters between them. */
	private static final String ARRAY_BRACKETS = "\\[\\s*\\]";
	/**
	 * Matches a reference to a class, as used in e.g. a return type.
	 * Supports arrays and type parameterisation.
	 */
	private static final String CLASS_REFERENCE = REFERENCE + "\\s*" + TYPE_PARAMETERISATION + "?";
	private static final String RETURN_TYPE = REFERENCE + "(?:(?:\\s*" + TYPE_PARAMETERISATION + "|" + ARRAY_BRACKETS + ")+|\\s)";
	private static final String TYPE_PARAMETER = "<[\\w$<>,\\s\\[\\]]+?>";

	private static final Pattern PATTERN_METHOD = Pattern.compile(
			"(" + MODIFIER + "*)" 							//modifiers
			+ "(" + RETURN_TYPE + ")"						//return type supporting arrays and (nested) type parameterisation
			+ "\\s*(" + IDENTIFIER + ")\\s*$"); //method name
	private static final Pattern PATTERN_CONSTRUCTOR = Pattern.compile(
			"(" + MODIFIER + "*)" 							//modifiers
			+ "\\s*(" + IDENTIFIER + ")\\s*$"); //constructor name
	private static final Pattern PATTERN_CLASS = Pattern.compile(
			"(" + MODIFIER + "*)"																		//modifiers
			+ "(class|interface|enum)\\s+"													//class OR interface OR enum
			+ "(" + IDENTIFIER + "(?:\\s*" + TYPE_PARAMETER + ")?)"	//class name and possibly a generic type
			+ "\\s*(?:extends\\s+(" + CLASS_REFERENCE + "))?"				//possible extension of another, possibly generic, class
			+ "\\s*(?:implements\\s+([^\\{]+?))?"										//implementation of zero or more interfaces
			+ "\\s*$");
	private static final Pattern PATTERN_ANONYMOUS_CLASS = Pattern.compile(
			"(?:(" + MODIFIER + "?)"					//modifiers
			+ "(" + CLASS_REFERENCE + ")?"		//a class reference, possibly type parameterised
			+ "\\s+(" + IDENTIFIER + ")"			//an identifier
			+ "\\s*=)?"												//=
			+ "\\s*new"												//new
			+ "\\s+(" + CLASS_REFERENCE + ")"	//a class reference, possibly type parameterised
			+ "\\s*$");
	private static final Pattern PATTERN_BLOCK = Pattern.compile(
			"(static|" + IDENTIFIER + "\\s*:|(?<![\\]A-Za-z>'\\s]))\\s*$"
			);
	private static final Pattern PATTERN_STATEMENT = Pattern.compile(
			"(?<=\\s|^)([a-z]+)\\s*$"
			);
	
	private Matcher methodMatcher;
	private Matcher constructorMatcher;
	private Matcher classMatcher;
	private Matcher anonymousClassMatcher;
	private Matcher blockMatcher;
	private Matcher statementMatcher;
	private Matcher bracketMatcher;
	
	private StringBuilder fileContent;
	private List<IntPair> bracketList;
	
	private Deque<FileUnit> fileUnitList;
	private Stack<Unit> unitStack;
	private List<BracketPair> bracketPairList;
	private BracketPair currentBracketPair;
	private int currentLineNumber;
	

	public UnitParser()
	{
		bracketList = new LinkedList<>();
		bracketPairList = new ArrayList<>();
		fileUnitList = new LinkedList<>();
		unitStack = new Stack<>();

		methodMatcher = PATTERN_METHOD.matcher("");
		constructorMatcher = PATTERN_CONSTRUCTOR.matcher("");
		classMatcher = PATTERN_CLASS.matcher("");
		anonymousClassMatcher = PATTERN_ANONYMOUS_CLASS.matcher("");
		blockMatcher = PATTERN_BLOCK.matcher("");
		statementMatcher = PATTERN_STATEMENT.matcher("");
		bracketMatcher = Pattern.compile("[\\{\\}\\(\\)]").matcher("");
	}


	@Override
	public void nextFile(File file)
	{
		bracketList.clear();
		bracketPairList.clear();
		unitStack.clear();
		currentLineNumber = 0;
		fileContent = new StringBuilder();
		
		fileUnitList.add(new FileUnit(file, null));
		unitStack.push(fileUnitList.getLast());
	}


	@Override
	public void parseLine(String line)
	{
		currentLineNumber++;
		
		int offset = fileContent.length();
		fileContent.append(line).append("\n");
		bracketList.addAll(findBrackets(fileContent, offset));
	}


	private List<IntPair> findBrackets(CharSequence line, int startIndex)
	{
		List<IntPair> brackets = new LinkedList<>();
		
		//NEXT_TASK 2: Maybe simply loop and use charAt() == ... instead? Surely that should be faster than Regex?
		bracketMatcher.reset(line);
		bracketMatcher.region(startIndex, line.length());
		
		while (bracketMatcher.find())
		{
			int start = bracketMatcher.start();
			
			if (!isJavaChar(start, line))
			{
				IntPair pair = new IntPair(start, currentLineNumber);
				brackets.add(pair);
			}
		}
		
		return brackets;
	}
	
	
	private boolean isJavaChar(int index, CharSequence line)
	{
		if (index == 0 || index == line.length()-1)
			return false;
		if (line.charAt(index-1) != '\'' || line.charAt(index+1) != '\'')
			return false;
		return true;
	}


	@Override
	public void postFile()
	{
		fileUnitList.getLast().startLine = 1;
		fileUnitList.getLast().endLine = currentLineNumber;
		
		createBracketPairs();
		createUnits();
	}


	private void createBracketPairs()
	{
		for (IntPair bracketUnit : bracketList)
		{
			int bracketIndex = bracketUnit.getFirst();
			int bracketLine = bracketUnit.getSecond();
			char bracketChar = fileContent.charAt(bracketIndex);
			
			if (bracketChar == '{' || bracketChar == '(')
			{
				BracketPair pair = new BracketPair(bracketChar, bracketIndex, bracketLine, currentBracketPair);
				
				if (pair.parent == null)
					bracketPairList.add(pair);
				currentBracketPair = pair;
			}
			else if (currentBracketPair != null)
			{
				currentBracketPair.endIndex = bracketIndex;
				currentBracketPair.endLine = bracketLine;
				
				if (currentBracketPair.parent != null)
					currentBracketPair.parent.children.add(currentBracketPair);
				currentBracketPair = currentBracketPair.parent;
			}
		}
	}
	
	
	private void createUnits()
	{
		for (int i = 0; i < bracketPairList.size(); i++)
		{
			BracketPair bracketPair = bracketPairList.get(i);
			if (bracketPair.parent == null && bracketPair.isCurly())
			{
				createUnitFromBracketPair(bracketPair, i);
			}
		}
	}


	private void createUnitFromBracketPair(BracketPair bracketPair, int siblingIndex)
	{
		Unit parentUnit = (unitStack.isEmpty() ? null : unitStack.peek());
		
		int searchRegionStart = -1;
		int searchRegionEnd = -1;
		
		int precedingParensIndex = -1;
		for (int j = bracketPair.startIndex-1; j >= 0; j--)
		{
			if (!Character.isWhitespace(fileContent.charAt(j)))
			{
				if (fileContent.charAt(j) == ')')
					precedingParensIndex = j;
				break;
			}
		}
		
		if (precedingParensIndex != -1)
		{
			BracketPair precedingParens = null;
			for (int j = siblingIndex-1; j >= 0; j--)
			{
				BracketPair sibling = bracketPair.parent.children.get(j);
				
				if (!sibling.isCurly() && sibling.endIndex == precedingParensIndex)
				{
					precedingParens = sibling;
					precedingParensIndex = j;
					break;
				}
			}
			
			if (precedingParens == null)
				throw new NullPointerException("There should be a ()-sibling before this BracketPair, but none was found!");

			searchRegionEnd = precedingParens.startIndex;
			searchRegionStart = findBraceBefore(precedingParens, precedingParensIndex);
		}
		else
		{
			searchRegionEnd = bracketPair.startIndex;
			searchRegionStart = findBraceBefore(bracketPair, siblingIndex);
		}
		
		boolean createdUnit = createUnit(bracketPair, parentUnit, searchRegionStart, searchRegionEnd);
		
		for (int i = 0; i < bracketPair.children.size(); i++)
		{
			BracketPair childPair = bracketPair.children.get(i);
			
			if (childPair.isCurly())
				createUnitFromBracketPair(childPair, i);
		}
		
		
		if (createdUnit)
		{
			unitStack.pop();
		}
	}
	
	
	private int findBraceBefore(BracketPair pair, int pairSiblingIndex)
	{
		if (pair.parent != null)
		{
			if (pairSiblingIndex == 0)
			{
				return pair.parent.startIndex;
			}
			
			for (int i = pairSiblingIndex-1; i >= 0; i--)
			{
				BracketPair sibling = pair.parent.children.get(i);
				if (sibling.isCurly())
				{
					return sibling.endIndex;
				}
			}
		}
		else
		{
			if (pairSiblingIndex == 0)
			{
				return 0;
			}
			
			for (int i = pairSiblingIndex-1; i >= 0; i--)
			{
				BracketPair sibling = bracketPairList.get(i);
				if (sibling.isCurly())
				{
					return sibling.endIndex;
				}
			}
		}
		
		return 0;
	}


	private boolean createUnit(BracketPair pair, Unit parentUnit,
			int searchRegionStart, int searchRegionEnd)
	{
		UnitDefinition unitDef = getUnitDefinition(searchRegionStart, searchRegionEnd, parentUnit);
		if (unitDef == null)
		{
			return false;
		}
		
		Unit info = null;
		
		switch (unitDef.type)
		{
			case Method :
				MethodUnit methodUnit = new MethodUnit(unitDef.matchGroups[3], parentUnit);
				methodUnit.modifiers = unitDef.matchGroups[1];
				methodUnit.returnType = unitDef.matchGroups[2];
				info = methodUnit;
				break;
			case Class :
			case InnerClass :
				ClassUnit classUnit = new ClassUnit(unitDef.matchGroups[3], parentUnit);
				classUnit.modifiers = unitDef.matchGroups[1];
				classUnit.type = ClassUnit.Type.fromString(unitDef.matchGroups[2]);
				classUnit.extendClause = unitDef.matchGroups[4];
				classUnit.implementsClause = unitDef.matchGroups[5];
				info = classUnit;
				break;
			case AnonymousClass :
				AnonymousClassUnit anonClassUnit = new AnonymousClassUnit(unitDef.matchGroups[3], parentUnit);
				anonClassUnit.extendedType = unitDef.matchGroups[4];
				info = anonClassUnit;
				break;
			case Constructor :
				methodUnit = new MethodUnit(unitDef.matchGroups[2], parentUnit);
				methodUnit.modifiers = unitDef.matchGroups[1];
				info = methodUnit;
				break;
			case Initialiser :
				String name = unitDef.matchGroups[1];
				String modifiers = null;
				
				if (name.equals("static"))
				{
					modifiers = name;
					name = "";
				}
				else
				{
					name = "<initialiser>";
				}
				
				methodUnit = new MethodUnit(name, parentUnit);
				methodUnit.modifiers = modifiers;
				info = methodUnit;
				break;
			case LocalBlock :
				name = unitDef.matchGroups[1];
				if (name.isEmpty())
					name = "<unnamed>";
				BlockUnit blockUnit = new BlockUnit(name, parentUnit);
				info = blockUnit;
				break;
			case Statement :
				info = new BlockUnit(unitDef.matchGroups[1], parentUnit);
				break;
			default :
				throw new IllegalStateException(unitDef.type + " is a Unit.Type that should not occur here!");
		}
		
		info.type = unitDef.type;
		info.startLine = pair.startLine;
		info.endLine = pair.endLine;
		
		if (info.parent != null)
			info.parent.children.add(info);
		
		unitStack.push(info);
		return true;
	}
	
	
	private UnitDefinition getUnitDefinition(int searchRegionStart, int searchRegionEnd, Unit parentUnit)
	{
		MatchResult result;

		// CLASS
		result = endsWith(fileContent, classMatcher, searchRegionStart, searchRegionEnd);
		if (result != null)
		{
			Unit.Type type = (parentUnit == null ? Unit.Type.Class : Unit.Type.InnerClass);
			return new UnitDefinition(type, result);
		}

		if (parentUnit != null)
		{
			// ANONYMOUS CLASS
			result = endsWith(fileContent, anonymousClassMatcher, searchRegionStart, searchRegionEnd);
			if (result != null)
				return new UnitDefinition(Unit.Type.AnonymousClass, result);

			// STATEMENT
			if (!Unit.Type.isClassVariant(parentUnit.type))
			{
				result = endsWith(fileContent, statementMatcher, searchRegionStart, searchRegionEnd);
				if (result != null && isValidStatement(result))
					return new UnitDefinition(Unit.Type.Statement, result);
			}

			// METHOD
			if (Unit.Type.isClassVariant(parentUnit.type))
			{
				result = endsWith(fileContent, methodMatcher, searchRegionStart, searchRegionEnd);
				if (result != null && isValidMethod(result))
					return new UnitDefinition(Unit.Type.Method, result);

			}

			// BLOCK
			result = endsWith(fileContent, blockMatcher, searchRegionStart, searchRegionEnd);
			if (result != null)
			{
				Unit.Type type = (Unit.Type.isClassVariant(parentUnit.type) ? Unit.Type.Initialiser : Unit.Type.LocalBlock);
				return new UnitDefinition(type, result);
			}

			// CONSTRUCTOR
			if (Unit.Type.isClassVariant(parentUnit.type))
			{
				result = endsWith(fileContent, constructorMatcher, searchRegionStart, searchRegionEnd);
				if (result != null && isValidConstructor(result, parentUnit))
					return new UnitDefinition(Unit.Type.Constructor, result);
			}
		}

		return null;
	}


	/**
	 * 
	 * @param line The line to search.
	 * @param matcher A matcher to use.
	 * @param regionStart The start-point (inclusive) for the region the matcher
	 *          should search.
	 * @param regionEnd The end-point (exclusive) for the region the matcher
	 *          should search.
	 * @return A <code>MatchResult</code> if the <code>matcher</code> found a
	 *         match within the specified region, <code>null</code> otherwise.
	 */
	private MatchResult endsWith(CharSequence line, Matcher matcher, int regionStart, int regionEnd)
	{
		matcher.reset(line);
		matcher.region(regionStart, regionEnd);
		
		if (matcher.find())
			return matcher.toMatchResult();
		else
			return null;
	}
	
	
	private boolean isValidMethod(MatchResult match)
	{
		String returnType = match.group(2).trim();
		if (!returnType.equals("void") && isKeyword(returnType, true))
			return false;
		if (isKeyword(match.group(3).trim(), false))
			return false;
		return true;
	}
	
	
	private boolean isValidConstructor(MatchResult match, Unit parentUnit)
	{
		String modifier = match.group(1).trim();
		if (!modifier.isEmpty() && !isKeyword(modifier, false))
			return false;
		if (!match.group(2).trim().equals(parentUnit.name))
			return false;
		return true;
	}
	
	
	private boolean isValidStatement(MatchResult match)
	{
		return isKeyword(match.group(1).trim(), false);
	}
	
	
	private boolean isKeyword(String word, boolean excludePrimitives)
	{
		if (excludePrimitives && Arrays.binarySearch(Constants.PRIMITIVES, word) >= 0)
				return false;
		return (Arrays.binarySearch(Constants.KEYWORDS, word) >= 0);
	}


	@Override
	public void storeResult(PackageData packageData)
	{
	}
	
	
	public static void main(String[] args)
	{
		UnitParser p = new UnitParser();
		
		File file = new File("src/main/java/komposten/analyser/backend/parsers/UnitParser.java");
		p.nextFile(file);
		
		boolean commentOnLastLine = false;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			
			while ((line = reader.readLine()) != null)
			{
				StringBuilder builder = new StringBuilder(line);
				commentOnLastLine = SourceUtil.removeComments(builder, commentOnLastLine, false, false);
				p.parseLine(builder.toString().trim());
			}
			
			p.postFile();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	private static class BracketPair
	{
		final char character;
		final BracketPair parent;
		final List<BracketPair> children;
		final int startIndex;
		final int startLine;
		int endIndex;
		int endLine;
		
		public BracketPair(char character, int startIndex, int startLine, BracketPair parent)
		{
			this.character = character;
			this.startIndex = startIndex;
			this.startLine = startLine;
			this.parent = parent;
			this.children = new LinkedList<>();
			
		}
		
		
		boolean isCurly()
		{
			return character == '{';
		}
	}
	
	
	private static class UnitDefinition
	{
		Unit.Type type; 
		String[] matchGroups;
		
		public UnitDefinition(Unit.Type type, MatchResult match)
		{
			this.type = type;
			
			this.matchGroups = new String[match.groupCount()+1];
			for (int i = 0; i <= match.groupCount(); i++)
			{
				String group = match.group(i);
				matchGroups[i] = (group == null ? "" : group.trim());
			}
		}
	}
	
	
	public static abstract class Unit
	{
		public enum Type
		{
			File, Class, InnerClass, AnonymousClass, Method, Constructor, Initialiser, LocalBlock, Statement, Unknown;
			
			public static boolean isClassVariant(Type type)
			{
				return type == Class || type == InnerClass || type == AnonymousClass;
			}
		}
		
		Unit parent;
		String name;
		Type type;
		int startLine;
		int endLine;
		List<Unit> children;
		
		public Unit(String name, Unit parent)
		{
			this.parent = parent;
			this.name = name;
			this.children = new LinkedList<>();
		}
	}
	
	
	public static class FileUnit extends Unit
	{
		File file;
		public FileUnit(File file, Unit parent)
		{
			super(file.getName(), parent);
			this.file = file;
			this.type = Type.File;
		}
	}
	
	
	public static class ClassUnit extends Unit
	{
		public enum Type
		{
			Class,
			Enum,
			Interface;

			public static Type fromString(String string)
			{
				for (Type type : values())
				{
					if (type.toString().equalsIgnoreCase(string))
						return type;
				}
				
				return null;
			}
		}
		
		String modifiers;
		Type type;
		String extendClause;
		String implementsClause;
		
		public ClassUnit(String name, Unit parent)
		{
			super(name, parent);
		}
	}
	
	
	public static class AnonymousClassUnit extends Unit
	{
		String extendedType;
		
		public AnonymousClassUnit(String name, Unit parent)
		{
			super(name, parent);
		}
	}
	
	
	public static class MethodUnit extends Unit
	{
		String modifiers;
		String returnType;
		String parameterClause;
		
		public MethodUnit(String name, Unit parent)
		{
			super(name, parent);
		}
	}
	
	
	public static class BlockUnit extends Unit
	{
		public BlockUnit(String name, Unit parent)
		{
			super(name, parent);
		}
	}
}
