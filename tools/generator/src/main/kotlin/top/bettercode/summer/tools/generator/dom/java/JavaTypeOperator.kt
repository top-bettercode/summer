package top.bettercode.summer.tools.generator.dom.java

import top.bettercode.summer.tools.lang.util.JavaType

class JavaTypeOperator(private val collections: MutableCollection<JavaType>) {


    operator fun String.unaryPlus() {
        collections.add(JavaType(this))
    }

    operator fun JavaType.unaryPlus() {
        collections.add(this)
    }
}