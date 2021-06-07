/**
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.bettercode.generator.dom.java

import java.util.*

/**
 * This class contains a list of Java reserved words.
 *
 * @author Jeff Butler
 */
object JavaReservedWords {

    private var RESERVED_WORDS: MutableSet<String>? = null

    init {
        val words = arrayOf("abstract",
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
