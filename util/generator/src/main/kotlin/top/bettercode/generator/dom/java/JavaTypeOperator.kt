package top.bettercode.generator.dom.java

class JavaTypeOperator(private val collections: MutableCollection<JavaType>) {


    operator fun String.unaryPlus() {
        collections.add(JavaType(this))
    }

    operator fun JavaType.unaryPlus() {
        collections.add(this)
    }
}