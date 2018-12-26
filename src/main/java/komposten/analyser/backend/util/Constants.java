package komposten.analyser.backend.util;

import java.util.Arrays;

public class Constants
{
	public static class SettingKeys
	{
		public static final String LAST_OPENED_DIRECTORY = "lastOpenDir";
		public static final String LAST_OPENED_PROJECT = "lastOpenedProj";
		public static final String REMEMBER_LAST_PROJECT = "rememberLastProj";
		public static final String REMEMBER_LAST_DIRECTORY = "rememberLastDir";
		public static final String RECENT_PROJECTS = "recent";
		public static final String ANALYSE_COMMENTS = "analyseComments";
		public static final String ANALYSE_STRINGS = "analyseStrings";
		public static final String FILE_LENGTH_THRESHOLD = "fileLengthThreshold";
		public static final String CLASS_LENGTH_THRESHOLD = "classLengthThreshold";
		public static final String METHOD_LENGTH_THRESHOLD = "methodLengthThreshold";
	}
	
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
