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
	
//	/** Matches private, protected or public. */
//	private static final String ACCESS_MODIFIER = "private|protected|public";
//	/** Matches static, abstract, final, static final or final static. */
//	private static final String STATIC_FINAL_MODIFIER = "static(?:\\s+final)?|abstract|final(?:\\s+static)?";
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
	private static final String CLASS_REFERENCE = REFERENCE + "\\s*(?:<[\\w$<>,\\s\\[\\]?]+?>)?";
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
//			+ "(" + CLASS_REFERENCE + "(?:\\s*" + ARRAY_BRACKETS + ")?)"				//return type supporting arrays and (nested) type parameterisation
			+ "(" + RETURN_TYPE + ")"																						//return type supporting arrays and (nested) type parameterisation
			+ "\\s*(" + IDENTIFIER + ")" 																					//method name
			+ "\\s*(\\(" 																													//parameter list start
				+ "(?:" + CLASS_REFERENCE + "(?:"																		//a class reference, possibly type parameterised
						+ "(?:\\s+" + IDENTIFIER + "\\s*(?:" + ARRAY_BRACKETS + ")?)" 	//an identifier, possibly followed by []
						+ "|" 																													//or
						+ "(?:\\s*" + ARRAY_BRACKETS + "\\s*" + IDENTIFIER + ")" 				//[] followed by an identifier
				+ ")\\s*,?\\s*|\\s)*" 																							//possibly a comma, and 0 or more repeats of the parameter declaration pattern
			+ "\\))(?:\\s*[\\{;])?"); 																						//parameter list end
	//FIXME UnitLengthParser; METHOD_PATTERN gives incorrect matches for generic methods. Add support for TYPE_PARAMETER between modifiers and return type.
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
			+ "\\s*(?:\\{|$)");
	
	static
	{
		System.out.println(METHOD_PATTERN.pattern()); //XXX Testing stuff.
		System.out.println(CLASS_PATTERN.pattern()); //XXX Testing stuff.
	}

	private Matcher methodMatcher;
	private Matcher classMatcher;
	private Matcher anonymousClassMatcher;
	private Matcher lambdaMatcher;
	private Matcher braceMatcher;
	
	private List<FileInfo> fileInfo;
	private FileInfo currentFileInfo;
	private Stack<Info<?>> infoStack;
	
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
//		anonymousClassMatcher = ANON_CLASS_PATTERN.matcher("");
//		lambdaMatcher = LAMBDA_PATTERN.matcher("");
		braceMatcher = Pattern.compile("(?<!')(\\{|\\})(?!')").matcher("");
	}


	@Override
	public void nextFile(File file)
	{
		currentFileInfo = new FileInfo(file, null);
		fileInfo.add(currentFileInfo);
		
		infoStack.clear();
		unitStack.clear();
		previousLines = "";
		currentLine = 0;
		lastSymbol = null;
	}
	
	
	@Override
	public void parseLine(String line)
	{
		currentLine++;
		//NEXT_TASK 1 Fully implement method and class length, before tackling anonymous classes and lambdas.
		// NEXT_TASK 2 Require that comments be removed before calling this! Maybe
		// have PackageAnalyser store two versions of a line (one without comments, and
		// one based on user settings), then choose line to pass depending on Parser
		// NEXT_TASK 3 Call this method even on empty lines!
		
		/* 
		 * Find indexes for all units in this line. Start and end.
		 * Check each unit.
		 * 		If it is a class-unit, check if we are in a class already, then add it to the stack as Class, InnerClass or AnonymousClass.
		 * 			Store inner and anonymous classes as separate ClassInfo objects, but keep track of their parent?
		 * 		If it is a method-unit, add it to the unit stack.
		 * 		
		 * At the end of a unit, store the UnitInfo in the current FileInfo object.
		 */

		/*
		 * FIXME
		 * Problem: Does not work properly for class initialisers ({ } blocks without name) and static initialisers if there are other declarations before them.
		 * Solution: Find all nameless blocks (initialisers or local scopes) and static initialisers as valid units. Then filter out the local scopes.
		 */
		
		List<UnitSymbol> unitSymbols = findAllUnitSymbols(line);
		
		for (UnitSymbol symbol : unitSymbols)
		{
			Unit parent = (unitStack.isEmpty() ? null : unitStack.peek());
			
			if (symbol.type == Unit.Type.UnitEnd)
			{
				Unit currentUnit = unitStack.pop();
				
				if (currentUnit.type != Unit.Type.Invalid)
				{
					Info<?> currentInfo = infoStack.pop();
					
					currentInfo.length = currentLine - currentUnit.startLine;
				}
				
				for (int i = 0; i < unitStack.size(); i++)
					System.out.print(" ");
				System.out.println("------" + currentLine);
			}
			else if (symbol.type == Unit.Type.Invalid)
			{
				for (int i = 0; i < unitStack.size(); i++)
					System.out.print(" ");
				System.out.println("Brace: " + currentLine);
				Unit unit = new Unit("", Unit.Type.Invalid, unitStack.size(), currentLine, parent);
				unitStack.push(unit);

			}
			else
			{
				boolean hasBody = true;
				String name;
				Unit.Type type;
				Info<?> parentInfo = (infoStack.isEmpty() ? null : infoStack.peek());
				Info<?> unitInfo;

				switch (symbol.type)
				{
					case Method :
						name = symbol.match.group(3);
						type = Unit.Type.Method;

						if (symbol.match.group().endsWith(";"))
							hasBody = false;

						unitInfo = new MethodInfo(name, parentInfo);
						break;
					case Class :
						boolean isInner = false;
						for (Unit unit : unitStack)
						{
							if (unit.type == Unit.Type.Class)
							{
								isInner = true;
								break;
							}
						}

						name = symbol.match.group(3);
						type = (isInner ? Unit.Type.InnerClass : Unit.Type.Class);
						unitInfo = new ClassInfo(name, parentInfo);
						break;
					case UnitEnd :
					case AnonymousClass :
					case InnerClass :
					default :
						throw new IllegalStateException(symbol.type + " is a Unit.Type that should not occur here!");
				}

				infoStack.push(unitInfo);
				
				for (int i = 0; i < unitStack.size(); i++)
					System.out.print(" ");
				System.out.println("Unit: " + name + ":" + type + ":" + currentLine);

				if (hasBody)
				{
					Unit unit = new Unit(name, type, unitStack.size(), currentLine, parent);
					unitStack.push(unit);
				}
				else
				{
					unitInfo.length = 1;
					infoStack.pop();
				}
			}
		}
	}


	private List<UnitSymbol> findAllUnitSymbols(String line)
	{
		LinkedList<UnitSymbol> unitSymbols = new LinkedList<>();
		
		previousLines = previousLines + "\n" + line;
		
		findUnitSymbolsOnLine(previousLines, Unit.Type.Method, methodMatcher, unitSymbols);
		findUnitSymbolsOnLine(previousLines, Unit.Type.Class, classMatcher, unitSymbols);
		findBracesOnLine(previousLines, unitSymbols);

		Collections.sort(unitSymbols);
		
		removeInvalidSymbols(unitSymbols);
		
		if (!unitSymbols.isEmpty())
		{
			previousLines = previousLines.substring(unitSymbols.getLast().match.end());
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
		
		boolean isFirst = true;
		while (braceMatcher.find())
		{
			MatchResult result = braceMatcher.toMatchResult();
			
			boolean isPartOfValid = false;
			
			if (isFirst && lastSymbol != null && result.group().charAt(0) == '{' && line.substring(0, result.start()).trim().length() == 0)
			{
				isPartOfValid = (lastSymbol.type != Unit.Type.Invalid && lastSymbol.type != Unit.Type.Method) || !lastSymbol.match.group().endsWith("{");
			}
			else
			{
				for (UnitSymbol unitSymbol : outputList)
				{
					if (MathOps.isInInterval(result.start(), unitSymbol.match.start(), unitSymbol.match.end()-1, true))
					{
						isPartOfValid = true;
						break;
					}
				}
			}
			
			if (!isPartOfValid)
			{
				Unit.Type type = (result.group().charAt(0) == '{' ? Unit.Type.Invalid : Unit.Type.UnitEnd);
				UnitSymbol symbol = new UnitSymbol(type, result, line);
				outputList.add(symbol);
			}
			
			isFirst = false;
		}
	}
	
	
	private void removeInvalidSymbols(LinkedList<UnitSymbol> unitSymbols)
	{
		Iterator<UnitSymbol> iterator = unitSymbols.iterator();
		
		while (iterator.hasNext())
		{
			UnitSymbol symbol = iterator.next();
			
			if (symbol.type == Unit.Type.Method && symbol.match.group(2).trim().equals("new"))
			{
				iterator.remove();
			}
		}
	}


	@Override
	public void postFile()
	{
	}


	@Override
	public void storeResult(PackageData packageData)
	{
		//TODO Store the data in the PackageData object somehow.
		//			Maybe have some kind of "property tree" or something that we can stuff the data into.
		//			The data in here could then be parsed automatically, and depth used for indenting.
	}
	
	
	private static class Unit
	{
		enum Type { Class, Method, InnerClass, AnonymousClass, UnitEnd, Invalid }
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
		MatchResult match;
		String string;
		
		public UnitSymbol(Unit.Type type, MatchResult match, String string)
		{
			this.type = type;
			this.match = match;
			this.string = string;
		}

		
		@Override
		public int compareTo(UnitSymbol o)
		{
			return Integer.compare(match.start(), o.match.start());
		}
	}
	
	
	private abstract class Info<T>
	{
		Info<?> parent;
		String name;
		int length;
		List<T> children;
		
		public Info(String name, Info<?> parent)
		{
			this.parent = parent;
			this.name = name;
		}
	}
	
	
	private class FileInfo extends Info<ClassInfo>
	{
		File file;
		public FileInfo(File file, Info<?> parent)
		{
			super(file.getName(), parent);
			this.file = file;
		}
	}
	
	
	private class ClassInfo extends Info<MethodInfo>
	{

		public ClassInfo(String name, Info<?> parent)
		{
			super(name, parent);
		}
	}
	
	
	private class MethodInfo extends Info<String>
	{
		public MethodInfo(String name, Info<?> parent)
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
