package cn.bestwu.generator.dom.java

import cn.bestwu.generator.dom.java.element.JavaElement.Companion.indent

class StringOperator(val collections: MutableCollection<String>) {

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

class StringOperator1(val collections: MutableCollection<String>) {

    operator fun String.unaryPlus() {
        collections.add(this)
    }
}