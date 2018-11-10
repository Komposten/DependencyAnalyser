package komposten.analyser.backend.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.util.SourceUtil;
import komposten.utilities.tools.MathOps;


public class UnitLengthParser implements SourceParser
{
	// NEXT_TASK Split parsers into two groups: IndependentParser (parse data not dependent on Units) and UnitParser. UnitParser does unit matching against lines and then passes that information to a list of parsers which make use of it!
	
	/** Matches any combination of modifiers (private, protected, public, abstract, static or final). */
	private static final String MODIFIER = "(?:(?:private|protected|public|abstract|static|final)\\s+)";
	/** Matches any valid Java identifier. */
	private static final String IDENTIFIER = "[A-Za-z_$][\\w$]*?";
	private static final String REFERENCE = "[A-Za-z_$][\\w$.]*?";
	private static final String TYPE_PARAMETERISATION = "(?:<[\\w$<>,\\s\\[\\]?]+?>)";
	private static final String ARRAY_BRACKETS = "\\[\\s*\\]";
	/**
	 * Matches a reference to a class, as used in e.g. a return type.
	 * Supports arrays and type parameterisation.
	 */
	private static final String CLASS_REFERENCE = REFERENCE + "\\s*" + TYPE_PARAMETERISATION + "?";
	private static final String RETURN_TYPE = REFERENCE + "(?:(?:\\s*" + TYPE_PARAMETERISATION + "|" + ARRAY_BRACKETS + ")+|\\s)";
	private static final String TYPE_PARAMETER = "<[\\w$<>,\\s\\[\\]]+?>";
	/**
	 * Capturing groups:
	 * <ol>
	 * <li>Modifiers
	 * <li>Return type
	 * <li>Method name
	 * <li>Parameter list
	 * </ol>
	 */
	private static final Pattern METHOD_PATTERN = Pattern.compile(
			"(" + MODIFIER + "*)" 																								//modifiers
			+ "(" + RETURN_TYPE + ")"																							//return type supporting arrays and (nested) type parameterisation
			+ "\\s*(" + IDENTIFIER + ")" 																					//method name
			+ "\\s*(\\(" 																													//parameter list start
				+ "(?:" + CLASS_REFERENCE + "(?:"																		//a class reference, possibly type parameterised
						+ "(?:\\s+" + IDENTIFIER + "\\s*(?:" + ARRAY_BRACKETS + ")?)" 	//an identifier, possibly followed by []
						+ "|" 																													//or
						+ "(?:\\s*" + ARRAY_BRACKETS + "\\s*" + IDENTIFIER + ")" 				//[] followed by an identifier
				+ ")\\s*,?\\s*|\\s)*" 																							//possibly a comma, and 0 or more repeats of the parameter declaration pattern
			+ "\\))(?:\\s*[\\{;])"); 																							//parameter list end
	
	/*
	 * FIXME UnitLengthParser; Pattern problems:
	 * 		METHOD: Incorrect matches for generic methods. (Does not allow for a TYPE_PARAMETER between modifiers and return type).
	 * 		METHOD: Does not work for parameters like "Type<Type2>paramName"
	 *		ANON_CLASS: Does not work for declarations like "Type<Type2>varName"
	 * 		ANON_CLASS: Does not allow "new X()" and "new X() {}" as parameters!
	 */
	/**
	 * Capturing groups:
	 * <ol>
	 * <li>Modifiers
	 * <li>Type (class, interface or enum)
	 * <li>Class name
	 * <li>Extended classes
	 * <li>Implemented classes
	 * </ol>
	 */
	private static final Pattern CLASS_PATTERN = Pattern.compile(
			"(" + MODIFIER + "*)"																						//modifiers
			+ "(class|interface|enum)\\s+"																	//class OR interface OR enum
			+ "(" + IDENTIFIER + "(?:\\s*" + TYPE_PARAMETER + ")?)"					//class name and possibly a generic type
			+ "\\s*(?:extends\\s+(" + CLASS_REFERENCE + "))?"								//possible extension of another, possibly generic, class
			+ "\\s*(?:implements\\s+((?:" + CLASS_REFERENCE + ",?\\s*)+))?"	//implementation of zero or more, possibly generic, interfaces
			+ "\\s*(?:\\{)");
	/**
	 * Capturing groups:
	 * <ol>
	 * <li>Modifiers
	 * <li>Type to store the variable as
	 * <li>Variable/field identifier
	 * <li>Type to extend 
	 * <li>Parameter list
	 * </ol>
	 */
	private static final Pattern ANONYMOUS_CLASS_PATTERN = Pattern.compile(
			"(?:(" + MODIFIER + "*)"					//modifiers
			+ "(" + CLASS_REFERENCE + ")?"		//a class reference, possibly type parameterised
			+ "\\s+(" + IDENTIFIER + ")"			//an identifier
			+ "\\s*=)?"												//=
			+ "\\s*new"												//new
			+ "\\s+(" + CLASS_REFERENCE + ")"	//a class reference, possibly type parameterised
			+ "\\s*(\\([^\\)]*\\))"						//parameter list
			+ "\\s*\\{");
	/**
	 * Matches static initialisers, class initialisers, named/labelled blocks and
	 * unnamed blocks. Does not match the blocks in e.g. if-statements or
	 * try-blocks.<br />
	 * Capturing groups:
	 * <ol>
	 * <li>Block name
	 * </ol>
	 */
	private static final Pattern INITIALISER_OR_LOCAL_BLOCK_PATTERN = Pattern.compile(
			"(static|" + IDENTIFIER + "\\s*:|(?<![\\)\\]A-Za-z\\s>']))\\s*\\{"
			);
	
	static
	{
		System.out.println(METHOD_PATTERN.pattern()); //XXX Testing stuff.
		System.out.println(CLASS_PATTERN.pattern()); //XXX Testing stuff.
		System.out.println(ANONYMOUS_CLASS_PATTERN.pattern()); //XXX Testing stuff.
	}

	private Matcher methodMatcher;
	private Matcher classMatcher;
	private Matcher initialiserOrLocalBlockMatcher;
	private Matcher anonymousClassMatcher;
	private Matcher lambdaMatcher;
	private Matcher braceMatcher;
	
	private List<FileInfo> fileInfo;
	private FileInfo currentFileInfo;
	private Stack<Info> infoStack;
	
	private Stack<Unit> unitStack;
	private String previousLines;
	private int currentLine;
	
	private UnitSymbol lastSymbol;
	
	public UnitLengthParser()
	{
		fileInfo = new LinkedList<>();
		infoStack = new Stack<>();
		unitStack = new Stack<>();
		
		methodMatcher = METHOD_PATTERN.matcher("");
		classMatcher = CLASS_PATTERN.matcher("");
		initialiserOrLocalBlockMatcher = INITIALISER_OR_LOCAL_BLOCK_PATTERN.matcher("");
		anonymousClassMatcher = ANONYMOUS_CLASS_PATTERN.matcher("");
//		lambdaMatcher = LAMBDA_PATTERN.matcher("");
		braceMatcher = Pattern.compile("(?<!')(\\{|\\})(?!')").matcher("");
	}


	@Override
	public void nextFile(File file)
	{
		infoStack.clear();
		unitStack.clear();
		previousLines = "";
		currentLine = 0;
		lastSymbol = null;
		
		currentFileInfo = new FileInfo(file, null);
		fileInfo.add(currentFileInfo);
		infoStack.push(currentFileInfo);
	}
	

	@Override
	public void parseLine(String line)
	{
		currentLine++;
		// NEXT_TASK 2 Require that comments be removed before calling this! Maybe
		// have PackageAnalyser store two versions of a line (one without comments, and
		// one based on user settings), then choose line to pass depending on Parser
		// NEXT_TASK 3 Call this method even on empty lines!
		
		List<UnitSymbol> unitSymbols = findAllUnitSymbols(line);
		
		for (UnitSymbol unitSymbol : unitSymbols)
		{
			Unit parentUnit = (unitStack.isEmpty() ? null : unitStack.peek());
			
			if (unitSymbol.type == Unit.Type.UnitEnd)
			{
				endCurrentUnit();
			}
			else if (unitSymbol.type == Unit.Type.Invalid)
			{
				addInvalidUnit(parentUnit);
			}
			else
			{
				addValidUnit(unitSymbol, parentUnit);
			}
		}
	}


	private void endCurrentUnit()
	{
		Unit currentUnit = unitStack.pop();
		
		if (currentUnit.type != Unit.Type.Invalid)
		{
			Info currentInfo = infoStack.pop();
			
			currentInfo.endLine = currentLine;
		}
		
	}


	private void addInvalidUnit(Unit parentUnit)
	{
		Unit unit = new Unit("", Unit.Type.Invalid, unitStack.size(), currentLine, parentUnit);
		unitStack.push(unit);
	}
	
	
	private void addValidUnit(UnitSymbol unitSymbol, Unit parentUnit)
	{
		Info parentInfo = infoStack.peek();
		
		String name;
		Unit.Type type;
		Info unitInfo;
		
		boolean hasBody = true;

		switch (unitSymbol.type)
		{
			case Method :
				name = unitSymbol.matchGroups[3];
				type = Unit.Type.Method;
				hasBody = !(unitSymbol.matchGroups[0].endsWith(";"));

				MethodInfo methodInfo = new MethodInfo(name, parentInfo);
				methodInfo.modifiers = unitSymbol.matchGroups[1];
				methodInfo.returnType = unitSymbol.matchGroups[2];
				methodInfo.parameterClause = unitSymbol.matchGroups[4];
				unitInfo = methodInfo;
				break;
			case Class :
				name = unitSymbol.matchGroups[3];
				type = Unit.Type.Class;
				
				for (Unit unit = parentUnit; unit != null; unit = unit.parent)
				{
					if (unit.type == Unit.Type.Class)
					{
						type = Unit.Type.InnerClass;
					}
				}

				ClassInfo classInfo = new ClassInfo(name, parentInfo);
				classInfo.modifiers = unitSymbol.matchGroups[1];
				classInfo.type = ClassInfo.Type.fromString(unitSymbol.matchGroups[2].trim());
				classInfo.extendClause = unitSymbol.matchGroups[4];
				classInfo.implementsClause = unitSymbol.matchGroups[5];
				unitInfo = classInfo;
				break;
			case AnonymousClass :
				name = unitSymbol.matchGroups[3];
				type = Unit.Type.AnonymousClass;
				
				classInfo = new ClassInfo(name, parentInfo);
				classInfo.modifiers = unitSymbol.matchGroups[1];
				classInfo.type = ClassInfo.Type.Class;
				classInfo.extendClause = unitSymbol.matchGroups[4];
				unitInfo = classInfo;
				break;
			case LocalBlock :
				name = unitSymbol.matchGroups[1];
				
				if (parentUnit != null && Unit.Type.isClassVariant(parentUnit.type))
				{
					if (name.isEmpty())
						name = "<initialiser>";
					
					type = Unit.Type.Initialiser;
					
					MethodInfo methodInfo2 = new MethodInfo(name, parentInfo);
					if (name.equals("static"))
					{
						methodInfo2.modifiers = name;
						name = "<initialiser>";
					}
					unitInfo = methodInfo2;
				}
				else
				{
					if (name.isEmpty())
						name = "<unnamed>";
					type = Unit.Type.LocalBlock;
					unitInfo = new BlockInfo(name, parentInfo);
				}
				break;
			default :
				throw new IllegalStateException(unitSymbol.type + " is a Unit.Type that should not occur here!");
		}

		unitInfo.startLine = currentLine;
		parentInfo.children.add(unitInfo);
		

		if (hasBody)
		{
			Unit unit = new Unit(name, type, unitStack.size(), currentLine, parentUnit);
			unitStack.push(unit);
			infoStack.push(unitInfo);
		}
		else
		{
			unitInfo.endLine = unitInfo.startLine;
		}
	}


	private List<UnitSymbol> findAllUnitSymbols(String line)
	{
		LinkedList<UnitSymbol> unitSymbols = new LinkedList<>();
		
		previousLines = previousLines + "\n" + line;
		
		findUnitSymbolsOnLine(previousLines, Unit.Type.Method, methodMatcher, unitSymbols);
		findUnitSymbolsOnLine(previousLines, Unit.Type.Class, classMatcher, unitSymbols);
		findUnitSymbolsOnLine(previousLines, Unit.Type.AnonymousClass, anonymousClassMatcher, unitSymbols);
		findUnitSymbolsOnLine(previousLines, Unit.Type.LocalBlock, initialiserOrLocalBlockMatcher, unitSymbols);
		findBracesOnLine(previousLines, unitSymbols);

		Collections.sort(unitSymbols);
		
		removeInvalidSymbols(unitSymbols);
		
		if (!unitSymbols.isEmpty())
		{
			previousLines = previousLines.substring(unitSymbols.getLast().endIndex);
			lastSymbol = unitSymbols.getLast();
		}
		
		return unitSymbols;
	}


	private void findUnitSymbolsOnLine(String line, Unit.Type unitType, Matcher matcher, List<UnitSymbol> outputList)
	{
		matcher.reset(line);
		
		while (matcher.find())
		{
			UnitSymbol symbol = new UnitSymbol(unitType, matcher.toMatchResult(), line);
			outputList.add(symbol);
		}
	}
	
	
	private void findBracesOnLine(String line, List<UnitSymbol> outputList)
	{
		braceMatcher.reset(line);
		
		boolean isFirstMatch = true;
		while (braceMatcher.find())
		{
			MatchResult result = braceMatcher.toMatchResult();
			Unit.Type type = null;
			
			boolean isOpeningBrace = result.group().charAt(0) == '{';
			
			if (isOpeningBrace)
			{
				boolean isPartOfExistingSymbol = false;
				boolean isInitialiserOrLocalBlock = false;
				String stringBeforeMatch = line.substring(0, result.start()).trim();
				
				if (isFirstMatch && lastSymbol != null && stringBeforeMatch.isEmpty())
				{
					isPartOfExistingSymbol = (lastSymbol.type != Unit.Type.UnitEnd && !lastSymbol.matchGroups[0].endsWith("{") && !lastSymbol.matchGroups[0].endsWith(";"));
				}
				else
				{
					for (UnitSymbol unitSymbol : outputList)
					{
						if (MathOps.isInInterval(result.start(), unitSymbol.startIndex, unitSymbol.endIndex-1, true))
						{
							isPartOfExistingSymbol = true;
							break;
						}
					}
				}
				
				if (!isPartOfExistingSymbol)
				{
					type = (isInitialiserOrLocalBlock ? Unit.Type.LocalBlock : Unit.Type.Invalid);
				}
			}
			else
			{
				type = Unit.Type.UnitEnd;
			}
			
			if (type != null)
			{
				UnitSymbol symbol = new UnitSymbol(type, result, line);
				outputList.add(symbol);
			}
			
			isFirstMatch = false;
		}
	}
	
	
	private void removeInvalidSymbols(LinkedList<UnitSymbol> unitSymbols)
	{
		Iterator<UnitSymbol> iterator = unitSymbols.iterator();
		
		while (iterator.hasNext())
		{
			UnitSymbol symbol = iterator.next();
			
			if (symbol.type == Unit.Type.Method && symbol.matchGroups[2].trim().equals("new"))
			{
				iterator.remove();
			}
		}
	}


	@Override
	public void postFile()
	{
		currentFileInfo.startLine = 1;
		currentFileInfo.endLine = currentLine;
	}


	@Override
	public void storeResult(PackageData packageData)
	{
		//NEXT_TASK 1; Store the data in the PackageData object somehow.
		//			Maybe have some kind of "property tree" or something that we can stuff the data into.
		//			The data in here could then be parsed automatically, and depth used for indenting.
	}
	
	
	private static class Unit
	{
		enum Type
		{
			Class, Method, InnerClass, AnonymousClass, UnitEnd, Initialiser, LocalBlock, Invalid;
			
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
	
	
	private static class UnitSymbol implements Comparable<UnitSymbol>
	{
		Unit.Type type; 
		String fullString;
		String[] matchGroups;
		int startIndex;
		int endIndex;
		
		public UnitSymbol(Unit.Type type, MatchResult match, String string)
		{
			this.type = type;
			this.fullString = string;
			this.startIndex = match.start();
			this.endIndex = match.end();
			
			this.matchGroups = new String[match.groupCount()+1];
			for (int i = 0; i <= match.groupCount(); i++)
			{
				matchGroups[i] = match.group(i);
			}
		}

		
		@Override
		public int compareTo(UnitSymbol o)
		{
			return Integer.compare(startIndex, o.startIndex);
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
	
	
	public static void main(String[] args)
	{
		UnitLengthParser p = new UnitLengthParser();
		
		File file = new File("src/main/java/komposten/analyser/backend/parsers/UnitLengthParser.java");
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
}
