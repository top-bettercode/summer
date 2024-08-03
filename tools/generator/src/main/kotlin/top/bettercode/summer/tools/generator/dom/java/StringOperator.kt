package top.bettercode.summer.tools.generator.dom.java

import top.bettercode.summer.tools.generator.dom.java.element.JavaElement.Companion.INDENT

class StringOperator(private val collections: MutableCollection<String>) {

    operator fun String.unaryPlus() {
        collections.add(this)
    }

    /**
     * indent this(Int) level
     */
    operator fun Int.plus(str: String) {
        var prefix = ""
        for (i in 0 until this) {
            prefix += INDENT
        }
        collections.add(prefix + str)
    }
}

