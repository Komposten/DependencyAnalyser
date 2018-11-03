package komposten.analyser.backend.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import komposten.utilities.exceptions.InvalidStateException;

public class SourceUtil
{
	private static final String STRING_REGEX = "\".+?(?<!\\\\)\"";
	/**
	 * Matches:
	 * <li><code>/*</code> not followed by * (i.e. /**)
	 * <li><code>/**</code>
	 * <li><code>//</code>
	 * <li><code>*&#47;</code>
	 * <li><code>"</code>
	 */
	private static final String COMMENT_REGEX = "/\\*\\*|/\\*(?!\\*)|//|\\*/|\"";
	private static final Matcher STRING_MATCHER;
	private static final Matcher COMMENT_MATCHER;
	
	
	static
	{
		STRING_MATCHER = Pattern.compile(STRING_REGEX).matcher("");
		COMMENT_MATCHER = Pattern.compile(COMMENT_REGEX).matcher("");
	}
	

	/**
	 * Replaces all Java strings in the provided string with "".
	 * @param line The string to remove Java strings from. This object is changed directly.
	 * @return <code>line</code>
	 */
	public static StringBuilder removeStrings(StringBuilder line)
	{
		STRING_MATCHER.reset(line.toString());
		
		line.setLength(0);
		line.append(STRING_MATCHER.replaceAll("\"\""));
		
		return line;
	}
	

	/**
	 * Shorthand for {@link #removeComments(StringBuilder, boolean, boolean, boolean) removeComments(line, startsInComment, false, true)}.
	 */
	public static boolean removeStrings(StringBuilder line, boolean startsInComment)
	{
		return removeComments(line, startsInComment, false, true);
	}
	
	
	/**
	 * Shorthand for {@link #removeComments(StringBuilder, boolean, boolean, boolean) removeComments(line, startsInComment, false, false)}.
	 */
	public static boolean removeComments(StringBuilder line, boolean startsInComment)
	{
		return removeComments(line, startsInComment, false, false);
	}
	
	
	/**
	 * Removes all Java-style comments, JavaDoc and Strings from the specified
	 * string.
	 * 
	 * @param line The string to remove comments from. <code>line</code> may
	 *          <i>not</i> contain line breaks. This object is changed directly.
	 * @param startsInComment If a comment was started on a previous line.
	 * @param keepStrings If Strings should be kept intact. Default behaviour is
	 *          to remove their content (<code>"hello world"</code> becomes
	 *          <code>""</code>).
	 * @param keepComments If comments and JavaDoc should be kept intaxt. Default
	 *          behaviour is to remove them.
	 * @return <code>true</code> if the line ends with an un-closed comment,
	 *         <code>false</code> otherwise.
	 */
	public static synchronized boolean removeComments(StringBuilder line, boolean startsInComment, boolean keepStrings, boolean keepComments)
	{
		if (line.indexOf("\n") != -1 || line.indexOf("\r") != -1)
			throw new IllegalArgumentException("line may not contain line breaks!");
		
		Match[] indices = findCommentSymbols(line, startsInComment);
		int offset = 0;
		
		if (keepStrings && keepComments)
		{
			Match lastSymbol = indices[indices.length-1];
			return !(lastSymbol.isString || lastSymbol.isSingleLineComment || lastSymbol.isEnd);
		}
		
		if (startsInComment && !keepComments)
		{
			if (indices.length == 0)
			{
				line.setLength(0);
				return true;
			}
			else
			{
				line.delete(0, indices[0].index+2);
				offset -= indices[0].index+2;
			}
		}
		
		for (int i = 0; i < indices.length; i++)
		{
			Match match = indices[i];
			if (match.isEnd)
				continue;
			
			if (match.isSingleLineComment && !keepComments)
			{
				line.delete(match.index + offset, line.length());
				return false;
			}
			else if ((match.isMultiLineComment || match.isJavaDoc) && !keepComments)
			{
				if (i+1 >= indices.length)
				{
					line.delete(match.index + offset, line.length());
					return true;
				}
				else
				{
					Match end = indices[i+1];
					line.delete(match.index + offset, end.index+2 + offset);
					offset -= end.index+2 - match.index;
					i += 1;
				}
			}
			else if (match.isString && !keepStrings)
			{
				if (i+1 >= indices.length)
				{
					throw new InvalidStateException("String start without end? We should never get here because findCommentIndices() should throw!!");
				}
				else
				{
					Match end = indices[i+1];
					line.delete(match.index+1 + offset, end.index + offset);
					offset -= end.index - (match.index+1);
					i += 1;
				}
			}
		}
		
		return false;
	}
	
	
	/**
	 * Finds all valid comment, String and JavaDoc symbols in the specified line of text.
	 * See {@link #findAllCommentSymbols(StringBuilder)} and {@link #removeInvalidCommentSymbols(boolean, List)}
	 * for more info.
	 * @param line
	 * @param startsInComment
	 * @return An array of {@link Match} instnces describing the symbols.
	 * @see #findAllCommentSymbols(StringBuilder)
	 * @see #removeInvalidCommentSymbols(boolean, List)
	 */
	private static Match[] findCommentSymbols(StringBuilder line, boolean startsInComment)
	{
		List<Match> symbols = findAllCommentSymbols(line);
		
		symbols = removeInvalidCommentSymbols(startsInComment, symbols);
		
		return symbols.toArray(new Match[symbols.size()]);
	}


	/**
	 * Finds all comment, String and JavaDoc symbols in the specified line of text.
	 * Symbols include <code>", /*, /**, *&#47;, //</code>.
	 * @param line
	 * @return A list of {@link Match} instances describing the symbols.
	 */
	private static List<Match> findAllCommentSymbols(StringBuilder line)
	{
		List<Match> symbols = new ArrayList<>();
		COMMENT_MATCHER.reset(line);
		int pos = 0;
		while (COMMENT_MATCHER.find(pos))
		{
			pos = COMMENT_MATCHER.start()+1; // Moving the position to the very next index to ensure that e.g. /*/ is matched twice (/* and */).
			int index = COMMENT_MATCHER.start();
			String text = COMMENT_MATCHER.group();
			
			if (text.indexOf('"') == -1 || isValidStringSymbol(line, index))
				symbols.add(new Match(index, text));
		}
		
		return symbols;
	}
	
	
	/**
	 * Checks if the double-quote (") at <code>symbolIndex</code> is a valid
	 * String start or end. Invalid refers to escaped double-quotes (\") and the
	 * double-quote char ('"').
	 */
	private static boolean isValidStringSymbol(StringBuilder line, int symbolIndex)
	{
		if (symbolIndex > 0)
		{
			if (symbolIndex < line.length()-1 && line.charAt(symbolIndex-1) == '\'' && line.charAt(symbolIndex+1) == '\'')
				return false;
			
			int backslashes = 0;
			int prevIndex = symbolIndex-1;
			while (prevIndex >= 0 && line.charAt(prevIndex) == '\\')
			{
				backslashes++;
				prevIndex--;
			}
			
			if ((backslashes & 1) == 1) //Preceded by odd number of backslashes == the " symbol is escaped!
				return false;
		}
		
		return true;
	}
	

	/**
	 * Removes all "nested" comment, String and JavaDoc symbols from the specified
	 * Match list (containing symbol information). <br />
	 * Examples of nested symbols: <br />
	 * 
	 * <pre>
	 * /* " // *&#47;  -  Double-quote and // nested inside a multi-line comment.
	 * " *&#47;* "  -  Comment start/end nested inside a String.
	 * </pre>
	 * 
	 * @param symbols The list of Matches to look through. The list will be
	 *          modified to some extent.
	 * @return A list of valid matches. Will be empty either if
	 *         <code>matches is empty</code>, or if
	 *         <code>startsInComment == true</code> and <code>matches</code>
	 *         contains no comment end symbols (*&#47;). This is <i>not</i> the
	 *         same list as <code>matches</code>!
	 */
	private static List<Match> removeInvalidCommentSymbols(
			boolean startsInComment, List<Match> symbols)
	{
		if (startsInComment)
		{
			int commentEnd = find("*/", symbols, 0);
			
			if (commentEnd != -1)
			{
				symbols = symbols.subList(commentEnd, symbols.size());
			}
			else
			{
				symbols.clear();
			}
		}
		
		for (int i = 0; i < symbols.size(); i++)
		{
			Match match = symbols.get(i);
			
			if (match.isString)
			{
				int stringEnd = find("\"", symbols, i+1);
				
				if (stringEnd != -1)
				{
					removeBetween(i, stringEnd, symbols);
					i += 1;
				}
				else
				{
					//Broken String, it doesn't have an end!
					throw new IllegalArgumentException("String without end on the line, should not be possible!");
				}
			}
			else if ((match.isMultiLineComment || match.isJavaDoc) && !match.isEnd)
			{
				int commentEnd = find("*/", symbols, i+1);
				
				if (commentEnd != -1)
				{
					removeBetween(i, commentEnd, symbols);
					i += 1;
				}
				else
				{
					symbols = symbols.subList(0, i+1);
				}
			}
			else if (match.isSingleLineComment)
			{
				while (symbols.size() > i+1)
					symbols.remove(i+1);
				
				symbols = symbols.subList(0, i+1);
			}
		}
		
		return symbols;
	}
	
	
	private static int find(String needle, List<Match> haystack, int start)
	{
		for (int i = start; i < haystack.size(); i++)
		{
			Match match = haystack.get(i);
			if (match.text.equals(needle))
				return i;
		}
		
		return -1;
	}


	private static void removeBetween(int start, int end, List<Match> matches)
	{
		Match match = matches.get(end);
		
		while (!matches.get(start+1).equals(match))
			matches.remove(start+1);
	}
	
	
	private static class Match
	{
		final int index;
		final String text;
		
		final boolean isMultiLineComment;
		final boolean isSingleLineComment;
		final boolean isJavaDoc;
		final boolean isString;
		
		final boolean isEnd;
		
		public Match(int index, String text)
		{
			this.index = index;
			this.text = text;
			
			switch (text)
			{
				case "/*" :
					isMultiLineComment = true;
					isSingleLineComment = false;
					isJavaDoc = false;
					isString = false;
					isEnd = false;
					break;
				case "/**" :
					isMultiLineComment = false;
					isSingleLineComment = false;
					isJavaDoc = true;
					isString = false;
					isEnd = false;
					break;
				case "//" :
					isMultiLineComment = false;
					isSingleLineComment = true;
					isJavaDoc = false;
					isString = false;
					isEnd = false;
					break;
				case "\"" :
					isMultiLineComment = false;
					isSingleLineComment = false;
					isJavaDoc = false;
					isString = true;
					isEnd = false;
					break;
				case "*/" :
					isMultiLineComment = true;
					isSingleLineComment = false;
					isJavaDoc = true;
					isString = false;
					isEnd = true;
					break;
				default :
					throw new IllegalArgumentException(text + " is not a valid comment, string or javadoc start/end!");
			}
		}
		
		
		@Override
		public String toString()
		{
			return index + "[" + text + "]";
		}
	}
}
