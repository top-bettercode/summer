package cn.bestwu.generator.dom.java.element

import cn.bestwu.generator.dom.java.Annotations
import cn.bestwu.generator.dom.java.JavaDomUtils
import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.StringOperator

class Parameter() {
    lateinit var type: JavaType
    lateinit var name: String
    var isVarargs: Boolean = false
    val annotations: Annotations = Annotations()

    constructor(name: String, type: JavaType) : this() {
        this.name = name
        this.type = type
    }


    fun annotation(annotations: StringOperator.() -> Unit) {
        annotations(StringOperator(this.annotations))
    }

    fun annotation(annotation: String) {
        annotations.add(annotation)
    }

    fun getFormattedContent(compilationUnit: CompilationUnit?): String {
        val sb = StringBuilder()

        for (annotation in annotations) {
            sb.append(annotation)
            sb.append(' ')
        }

        sb.append(JavaDomUtils.calculateTypeName(compilationUnit, type))

        sb.append(' ')
        if (isVarargs) {
            sb.append("... ")
        }
        sb.append(name)

        return sb.toString()
    }

    override fun toString(): String {
        return getFormattedContent(null)
    }
}
