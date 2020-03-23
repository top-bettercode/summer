package cn.bestwu.generator.dom.java

class JavaTypeOperator(val collections: MutableCollection<JavaType>) {


    operator fun String.unaryPlus() {
        collections.add(JavaType(this))
    }

    operator fun JavaType.unaryPlus() {
        collections.add(this)
    }
}