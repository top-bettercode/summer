package top.bettercode.summer.tools.generator.dom.java.element

import top.bettercode.summer.tools.generator.dom.java.JavaDomUtils
import top.bettercode.summer.tools.lang.util.JavaType

class Field : JavaElement() {
    lateinit var type: JavaType
    lateinit var name: String
    var initializationString: String? = null
    var isTransient: Boolean = false
    var isVolatile: Boolean = false

    private fun isSingle(): Boolean {
        return annotations.size == 1 && !annotations[0].contains("(")
    }

    override fun getFormattedContent(indentLevel: Int, compilationUnit: CompilationUnit): String {
        val sb = StringBuilder()

        addFormattedJavadoc(sb, indentLevel)
        if (isSingle()) {
            indent(sb, indentLevel)
            sb.append(annotations[0])
            sb.append(" ")
        } else {
            addFormattedAnnotations(sb, indentLevel)
            indent(sb, indentLevel)
        }
        sb.append(visibility.value)

        if (isStatic) {
            sb.append("static ")
        }

        if (isFinal) {
            sb.append("final ")
        }

        if (isTransient) {
            sb.append("transient ")
        }

        if (isVolatile) {
            sb.append("volatile ")
        }

        sb.append(JavaDomUtils.calculateTypeName(compilationUnit, type))

        sb.append(' ')
        sb.append(name)

        if (initializationString != null && initializationString!!.isNotEmpty()) {
            sb.append(" = ")
            sb.append(initializationString)
        }

        sb.append(';')

        return sb.toString()
    }
}
