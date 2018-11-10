package komposten.analyser.backend.util;

import java.util.Arrays;

public class Constants
{
	public static final int CYCLE_LIMIT = 100000;
	public static final String FILE_EXTENSION = ".java";

	public static final String[] KEYWORDS;
	public static final String[] PRIMITIVES;

	static
	{
		KEYWORDS = new String[]
				{
						"abstract",  "continue",  "for", "new", "switch",
						"assert", "default", "goto", "package", "synchronized",
						"boolean", "do",  "if",  "private", "this",
						"break", "double",  "implements",  "protected", "throw",
						"byte",  "else",  "import",  "public",  "throws",
						"case",  "enum",  "instanceof",  "return",  "transient",
						"catch", "extends", "int", "short", "try",
						"char",  "final", "interface", "static",  "void",
						"class", "finally", "long",  "strictfp",  "volatile",
						"const",  "float", "native",  "super", "while"
				};
		PRIMITIVES = new String[]
				{
						"boolean", "double", "byte", "int", "short", "char", "long", "float"
				};

		Arrays.sort(KEYWORDS);
		Arrays.sort(PRIMITIVES);
	}
}
