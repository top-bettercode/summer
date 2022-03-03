package top.bettercode.generator.dom.java

class StringOperator1(private val collections: MutableCollection<String>) {

    operator fun String.unaryPlus() {
        collections.add(this)
    }
}
