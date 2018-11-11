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
	/*
	 * Design 3:
	 * - nextLine():
	 * 		* Add the new line to the previous ones, until we eventually store the whole file.
	 *    * Find all {, }, ( and ) in the line and add their indices (sorted) into a master list.
	 * - postFile():
	 * 		1 Build "Unknown" units from all the braces and parentheses.
	 *    2 Go through all { in the list.
	 *    3 If preceded by a ), found which unit that ) belongs to, and then the (-index.
	 *    4 Check the string before the { to get the unit definition.
	 *    		a) If we had a ) in step 3, set the search region to end at the matching (.
	 *    		b) Set the search region to start at the first {, } or ( (unless there is a matching )) preceding our current {.
	 */
	
	
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
			+ "\\s*(" + IDENTIFIER + ")\\s*$"); 	//method name
	private static final Pattern PATTERN_CONSTRUCTOR = Pattern.compile(
			"(" + MODIFIER + "*)" 							//modifiers
			+ "\\s*(" + IDENTIFIER + ")\\s*$"); 	//constructor name
	private static final Pattern PATTERN_CLASS = Pattern.compile(
			"(" + MODIFIER + "*)"																						//modifiers
			+ "(class|interface|enum)\\s+"																	//class OR interface OR enum
			+ "(" + IDENTIFIER + "(?:\\s*" + TYPE_PARAMETER + ")?)"					//class name and possibly a generic type
			+ "\\s*(?:extends\\s+(" + CLASS_REFERENCE + "))?"								//possible extension of another, possibly generic, class
			+ "\\s*(?:implements\\s+([^\\{]+?))?"														//implementation of zero or more interfaces
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
	
	private Deque<FileInfo> fileInfoList;
	private Stack<Info> infoStack;
	private Stack<Unit> unitStack;
	private List<BracketPair> bracketPairList;
	private BracketPair currentBracketPair;
	private int currentLineNumber;
	

	public UnitParser()
	{
		bracketList = new LinkedList<>();
		bracketPairList = new ArrayList<>();
		fileInfoList = new LinkedList<>();
		infoStack = new Stack<>();
		unitStack = new Stack<>();

		methodMatcher = PATTERN_METHOD.matcher("");
		constructorMatcher = PATTERN_CONSTRUCTOR.matcher("");
		classMatcher = PATTERN_CLASS.matcher("");
		anonymousClassMatcher = PATTERN_ANONYMOUS_CLASS.matcher("");
		blockMatcher = PATTERN_BLOCK.matcher("");
		statementMatcher = PATTERN_STATEMENT.matcher("");
		bracketMatcher = Pattern.compile("[\\{\\}\\(\\)]").matcher("");
		
//		System.out.println(methodMatcher.pattern().toString());
//		System.out.println(classMatcher.pattern().toString());
//		System.out.println(anonymousClassMatcher.pattern().toString());
//		System.out.println(blockMatcher.pattern().toString());
//		System.out.println(statementMatcher.pattern().toString());
//		System.out.println(constructorMatcher.pattern().toString());
	}


	@Override
	public void nextFile(File file)
	{
		bracketList.clear();
		bracketPairList.clear();
		infoStack.clear();
		unitStack.clear();
		currentLineNumber = 0;
		fileContent = new StringBuilder();
		
		fileInfoList.add(new FileInfo(file, null));
		infoStack.push(fileInfoList.getLast());
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
		fileInfoList.getLast().startLine = 1;
		fileInfoList.getLast().endLine = currentLineNumber;
		
		createBracketPairs();
		createUnits();
	}


	private void createBracketPairs()
	{
		for (IntPair bracketInfo : bracketList)
		{
			int bracketIndex = bracketInfo.getFirst();
			int bracketLine = bracketInfo.getSecond();
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
		Unit parentUnit  = (unitStack.isEmpty() ? null : unitStack.peek());
		
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
			createUnitFromBracketPair(childPair, i);
		}
		
		
		if (createdUnit)
		{
			//NEXT_TASK Remove Unit and UnitStack. Rename Info to Unit and use that instead. But what about Unit.Type?
			unitStack.pop();
			infoStack.pop();
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
		UnitDefinition unitDef = getUnitDefinition(searchRegionStart, searchRegionEnd);
		if (unitDef == null)
		{
			System.out.format("Is null: %s:%d->%d:%d->%d\n", pair.character, pair.startLine, pair.endLine, searchRegionStart, searchRegionEnd);
			System.out.println("=====================");
			System.out.println(fileContent.subSequence(searchRegionStart, searchRegionEnd));
			System.out.println("=====================");
			return false;
		}
		
		Unit unit = null;
		Info info = null;
		
		String fullMatch = unitDef.matchGroups[0].trim();
		switch (unitDef.type)
		{
			case Method :
//				System.out.println("Method: " + fullMatch);
				break;
			case Class :
//				System.out.println("Class: " + fullMatch);
				break;
			case InnerClass :
//				System.out.println("Inner class: " + fullMatch);
				break;
			case AnonymousClass :
//				System.out.println("Anonymous class: " + fullMatch);
				break;
			case Constructor :
//				System.out.println("Constructor: " + fullMatch);
				break;
			case LocalBlock :
//				System.out.println("Local block: " + fullMatch);
				break;
			case Initialiser :
//				System.out.println("Initialiser: " + fullMatch);
				break;
			case Statement :
//				System.out.println("Statement: " + fullMatch);
				break;
			default :
				throw new IllegalStateException(unitDef.type + " is a Unit.Type that should not occur here!");
		}
		
//		info.startLine = pair.startLine;
//		info.endLine = pair.endLine;
//		currentInfo.children.add(info);
		//NEXT_TASK 1: How to handle this? Can't do currentInfo = info or currentInfo = currentInfo.parent unless I know if the current unit has any children or siblings!
		
		unitStack.push(unit);
		infoStack.push(info);
		return true;
	}


	private UnitDefinition getUnitDefinition(int searchRegionStart, int searchRegionEnd)
	{
		/* 
		 * CURRENT 1: It would technically be possible to limit which matchers to run depending on the current parent unit.
		 *  You can only define methods inside classes, not at top-level or inside methods.
		 * 	You can define classes anywhere.
		 * 	You cannot define anonymous classes at top-level.
		 *  You can only define statements inside methods (or initialisers).
		 *  You can only define initialisers inside classes.
		 */
		MatchResult result;
		
		result = endsWith(fileContent, classMatcher, searchRegionStart, searchRegionEnd);
		if (result != null)
			return new UnitDefinition(Unit.Type.Class, result);
		result = endsWith(fileContent, anonymousClassMatcher, searchRegionStart, searchRegionEnd);
		if (result != null)
			return new UnitDefinition(Unit.Type.AnonymousClass, result);
		result = endsWith(fileContent, statementMatcher, searchRegionStart, searchRegionEnd);
		if (result != null && isValidStatement(result))
			return new UnitDefinition(Unit.Type.Statement, result);
		result = endsWith(fileContent, methodMatcher, searchRegionStart, searchRegionEnd);
		if (result != null && isValidMethod(result))
			return new UnitDefinition(Unit.Type.Method, result);
		result = endsWith(fileContent, blockMatcher, searchRegionStart, searchRegionEnd);
		if (result != null)
			return new UnitDefinition(Unit.Type.LocalBlock, result);
		result = endsWith(fileContent, constructorMatcher, searchRegionStart, searchRegionEnd);
		if (result != null && isValidConstructor(result))
			return new UnitDefinition(Unit.Type.Constructor, result);
		
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
	
	
	private boolean isValidConstructor(MatchResult match)
	{
		String modifier = match.group(1).trim();
		if (!modifier.isEmpty() && !isKeyword(modifier, false))
			return false;
		if (isKeyword(match.group(2).trim(), false))
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
	
	
	private static class UnitDefinition
	{
		Unit.Type type; 
		String[] matchGroups;
		int startIndex;
		int endIndex;
		
		public UnitDefinition(Unit.Type type, MatchResult match)
		{
			this.type = type;
			this.startIndex = match.start();
			this.endIndex = match.end();
			
			this.matchGroups = new String[match.groupCount()+1];
			for (int i = 0; i <= match.groupCount(); i++)
			{
				matchGroups[i] = match.group(i);
			}
		}
	}
	
	
	public static abstract class Info
	{
		Info parent;
		String name;
		int startLine;
		int endLine;
		List<Info> children;
		
		public Info(String name, Info parent)
		{
			this.parent = parent;
			this.name = name;
			this.children = new LinkedList<>();
		}
	}
	
	
	public static class FileInfo extends Info
	{
		File file;
		public FileInfo(File file, Info parent)
		{
			super(file.getName(), parent);
			this.file = file;
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
	
	
	private static class Unit
	{
		enum Type
		{
			Class, Method, InnerClass, AnonymousClass, Constructor, Initialiser, LocalBlock, Statement, Unknown;
			
			public static boolean isClassVariant(Type type)
			{
				return type == Class || type == InnerClass || type == AnonymousClass;
			}
		}
		
		String name;
		Type type;
		Unit parent;
		int depth;
		int startLine;
		
		Unit(String name, Type type, int depth, int startLine, Unit parent)
		{
			this.name = name;
			this.type = type;
			this.depth = depth;
			this.startLine = startLine;
			this.parent = parent;
		}
	}
	
	
	public static class ClassInfo extends Info
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
		
		public ClassInfo(String name, Info parent)
		{
			super(name, parent);
		}
	}
	
	
	public static class MethodInfo extends Info
	{
		String modifiers;
		String returnType;
		String parameterClause;
		
		public MethodInfo(String name, Info parent)
		{
			super(name, parent);
		}
	}
	
	
	public static class BlockInfo extends Info
	{
		public BlockInfo(String name, Info parent)
		{
			super(name, parent);
		}
	}
}
