package top.bettercode.generator.dom.java

import top.bettercode.generator.dom.java.element.JavaElement.Companion.indent

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
            prefix += indent
        }
        collections.add(prefix + str)
    }
}

class StringOperator1(private val collections: MutableCollection<String>) {

    operator fun String.unaryPlus() {
        collections.add(this)
    }
}