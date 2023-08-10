package top.bettercode.summer.tools.generator.dom.java

/**
 * This class contains a list of Java reserved words.
 *
 * @author Jeff Butler
 */
object JavaReservedWords {

    private var RESERVED_WORDS: MutableSet<String>? = null

    init {
        val words = arrayOf(
                "abstract",
                "assert",
                "boolean",
                "break",
                "byte",
                "case",
                "catch",
                "char",
                "class",
                "const",
                "continue",
                "default",
                "do",
                "double",
                "else",
                "enum",
                "extends",
                "final",
                "finally",
                "float",
                "for",
                "goto",
                "if",
                "implements",
                "import",
                "instanceof",
                "int",
                "interface",
                "long",
                "native",
                "new",
                "package",
                "private",
                "protected",
                "public",
                "return",
                "short",
                "static",
                "strictfp",
                "super",
                "switch",
                "synchronized",
                "this",
                "throw",
                "throws",
                "transient",
                "try",
                "void",
                "volatile",
                "while"
        )

        RESERVED_WORDS = HashSet(words.size)

        for (word in words) {
            RESERVED_WORDS!!.add(word)
        }
    }

    fun containsWord(word: String?): Boolean {
        return if (word == null) {
            false
        } else {
            RESERVED_WORDS!!.contains(word)
        }
    }
}
/**
 * Utility class - no instances allowed.
 */
