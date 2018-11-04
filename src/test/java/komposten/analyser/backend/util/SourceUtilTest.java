package komposten.analyser.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class SourceUtilTest
{
	static SourceUtil sourceUtil;
	
	@BeforeAll
	static void setUp()
	{
		sourceUtil = new SourceUtil();
	}
	

	@Test
	void removeComments_singleLineComment()
	{
		StringBuilder line = new StringBuilder("int x = 10; //x-coordinate.");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("int x = 10; ", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_multiLineCommentAtEndOfLine()
	{
		StringBuilder line = new StringBuilder("int x = 10; /*x-coordinate.*/");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("int x = 10; ", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_multiLineCommentInMiddleOfLine()
	{
		StringBuilder line = new StringBuilder("int x = /*x-coordinate.*/ 10;");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("int x =  10;", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_javadocAtEndOfLine()
	{
		StringBuilder line = new StringBuilder("int x = 10; /**x-coordinate.*/");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("int x = 10; ", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_javaDocInMiddleOfLine()
	{
		StringBuilder line = new StringBuilder("int x = /**x-coordinate.*/ 10;");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("int x =  10;", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_multiLineCommentUnclosed()
	{
		StringBuilder line = new StringBuilder("int x = 10; /*x-coordinate.");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("int x = 10; ", line.toString());
		assertTrue(endsInUnclosedComment, "should end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_javaDocUnclosed()
	{
		StringBuilder line = new StringBuilder("int x = 10; /**x-coordinate.");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("int x = 10; ", line.toString());
		assertTrue(endsInUnclosedComment, "should end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_multiLineCommentLine2()
	{
		StringBuilder line = new StringBuilder("  * comment line 2...");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, true);
		
		assertEquals("", line.toString());
		assertTrue(endsInUnclosedComment, "should end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_javaDocLine2()
	{
		StringBuilder line = new StringBuilder("  * javadoc line 2...");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, true);
		
		assertEquals("", line.toString());
		assertTrue(endsInUnclosedComment, "should end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_multiLineCommentClosingLine()
	{
		StringBuilder line = new StringBuilder("  * comment end.*/ int y = 5;");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, true);
		
		assertEquals(" int y = 5;", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_javaDocClosingLine()
	{
		StringBuilder line = new StringBuilder("  * javadoc end.*/ int y = 5;");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, true);
		
		assertEquals(" int y = 5;", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_string()
	{
		StringBuilder line = new StringBuilder("String s = \"File \\\"\" + file + \"\\\" not found!\";");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("String s = \"\" + file + \"\";", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_stringEscapedBackslash()
	{
		StringBuilder line = new StringBuilder("String s = \"\\\\\"");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("String s = \"\"", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_stringEscapedBackslash2()
	{
		StringBuilder line = new StringBuilder("String s = \"\\\"\"");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("String s = \"\"", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_quoteChar()
	{
		StringBuilder line = new StringBuilder("String s = \"\\\"\" + '\"'");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false);
		
		assertEquals("String s = \"\" + '\"'", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_unclosedString_exceptionThrown()
	{
		StringBuilder line = new StringBuilder("String s = \"");
		Executable codeToTest = () -> { sourceUtil.removeComments(line, false); };
		
		assertThrows(IllegalArgumentException.class, codeToTest, "invalid strings (i.e. uneven number of \") should throw IllegalArgumentException!");
	}
	
	
	@Test
	void removeComments_stringWithNewlines_exceptionThrown()
	{
		StringBuilder line = new StringBuilder("String s = \"\"\n");
		Executable codeToTest = () -> { sourceUtil.removeComments(line, false); };
		
		assertThrows(IllegalArgumentException.class, codeToTest, "line containing line breaks should throw IllegalArgumentException!");
	}
	
	
	@Test
	void removeComments_mixedCommentsAndStrings()
	{
		StringBuilder line = new StringBuilder("* starts in comment */ /** My string */ String s = \"hello\"; /* ... */ // End!");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, true);
		
		assertEquals("  String s = \"\";  ", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_nestedCommentsAndStrings()
	{
		StringBuilder line = new StringBuilder("* starts // in /* comment */ /** \"My /* string */ String s = \"hello\"; /* \"...\" */ // End! /*");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, true);
		
		assertEquals("  String s = \"\";  ", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_keepStrings()
	{
		StringBuilder line = new StringBuilder("* starts // in /* comment */ /** \"My /* string */ String s = \"hello\"; /* \"...\" */ // End! /*");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, true, true, false);
		
		assertEquals("  String s = \"hello\";  ", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_keepComments()
	{
		StringBuilder line = new StringBuilder("* starts // in /* comment */ /** \"My /* string */ String s = \"hello\"; /* \"...\" */ // End! /*");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, true, false, true);
		
		assertEquals("* starts // in /* comment */ /** \"My /* string */ String s = \"\"; /* \"...\" */ // End! /*", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_keepCommentsAndStringsEndWithSingleLineComment()
	{
		StringBuilder line = new StringBuilder("String s = \"hello\"; /* */ //");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false, true, true);
		
		assertEquals("String s = \"hello\"; /* */ //", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_keepCommentsAndStringsEndWithString()
	{
		StringBuilder line = new StringBuilder("String s = \"hello\";");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false, true, true);
		
		assertEquals("String s = \"hello\";", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_keepCommentsAndStringsEndWithMultiLineComment()
	{
		StringBuilder line = new StringBuilder("String s = \"hello\"; /* */");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false, true, true);
		
		assertEquals("String s = \"hello\"; /* */", line.toString());
		assertFalse(endsInUnclosedComment, "should not end with unclosed comment!");
	}
	
	
	@Test
	void removeComments_keepCommentsAndStringsEndWithOpenComment()
	{
		StringBuilder line = new StringBuilder("String s = \"hello\"; /*");
		boolean endsInUnclosedComment = sourceUtil.removeComments(line, false, true, true);
		
		assertEquals("String s = \"hello\"; /*", line.toString());
		assertTrue(endsInUnclosedComment, "should end with unclosed comment!");
	}
}