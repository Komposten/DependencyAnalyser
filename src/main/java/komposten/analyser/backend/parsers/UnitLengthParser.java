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
			+ "(" + RETURN_TYPE + ")"																							//return type supporting arrays and (nested) type parameterisation
			+ "\\s*(" + IDENTIFIER + ")" 																					//method name
			+ "\\s*(\\(" 																													//parameter list start
				+ "(?:" + CLASS_REFERENCE + "(?:"																		//a class reference, possibly type parameterised
						+ "(?:\\s+" + IDENTIFIER + "\\s*(?:" + ARRAY_BRACKETS + ")?)" 	//an identifier, possibly followed by []
						+ "|" 																													//or
						+ "(?:\\s*" + ARRAY_BRACKETS + "\\s*" + IDENTIFIER + ")" 				//[] followed by an identifier
				+ ")\\s*,?\\s*|\\s)*" 																							//possibly a comma, and 0 or more repeats of the parameter declaration pattern
			+ "\\))(?:\\s*[\\{;])"); 																							//parameter list end
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
			+ "\\s*(?:\\{)");
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
//		anonymousClassMatcher = ANON_CLASS_PATTERN.matcher("");
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
		//NEXT_TASK 1 Fully implement method and class length, before tackling anonymous classes and lambdas.
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
				name = unitSymbol.match.group(3);
				type = Unit.Type.Method;
				hasBody = !(unitSymbol.match.group().endsWith(";"));
				unitInfo = new MethodInfo(name, parentInfo);
				break;
			case Class :
				name = unitSymbol.match.group(3);
				type = Unit.Type.Class;
				
				for (Unit unit = parentUnit; unit != null; unit = unit.parent)
				{
					if (unit.type == Unit.Type.Class)
					{
						type = Unit.Type.InnerClass;
					}
				}

				unitInfo = new ClassInfo(name, parentInfo);
				break;
			case LocalBlock :
				name = unitSymbol.match.group(1);
				
				if (parentUnit != null && Unit.Type.isClassVariant(parentUnit.type))
				{
					type = Unit.Type.Initialiser;
					if (name.isEmpty())
						name = "<initialiser>";
					unitInfo = new MethodInfo(name, parentInfo);
				}
				else
				{
					type = Unit.Type.LocalBlock;
					if (name.isEmpty())
						name = "<unnamed>";
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
		findUnitSymbolsOnLine(previousLines, Unit.Type.LocalBlock, initialiserOrLocalBlockMatcher, unitSymbols);
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
	
	
	//NEXT_TASK Re-factor this complete mess into something actually readable.
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
					isPartOfExistingSymbol = (lastSymbol.type != Unit.Type.UnitEnd && !lastSymbol.match.group().endsWith("{") && !lastSymbol.match.group().endsWith(";"));
				}
				else
				{
					for (UnitSymbol unitSymbol : outputList)
					{
						if (MathOps.isInInterval(result.start(), unitSymbol.match.start(), unitSymbol.match.end()-1, true))
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
		//NEXT_TASK Maybe extract the match info in here directly, so we e.g. only call substring() once per group.
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
	
	
	private abstract class Info
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
	
	
	private class FileInfo extends Info
	{
		File file;
		public FileInfo(File file, Info parent)
		{
			super(file.getName(), parent);
			this.file = file;
		}
	}
	
	
	private class ClassInfo extends Info
	{
		public ClassInfo(String name, Info parent)
		{
			super(name, parent);
		}
	}
	
	
	private class MethodInfo extends Info
	{
		public MethodInfo(String name, Info parent)
		{
			super(name, parent);
		}
	}
	
	
	private class BlockInfo extends Info
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
