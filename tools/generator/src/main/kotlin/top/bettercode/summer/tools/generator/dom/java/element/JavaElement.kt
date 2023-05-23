package top.bettercode.summer.tools.generator.dom.java.element

import ch.qos.logback.classic.spi.ThrowableProxyUtil.indent
import top.bettercode.summer.tools.generator.dom.java.Annotations
import top.bettercode.summer.tools.generator.dom.java.StringOperator

abstract class JavaElement {

    /** The java doc lines.  */
    private val javaDocLines: MutableList<String> = mutableListOf()

    /** The annotations.  */
    val annotations: Annotations = Annotations()

    /** The visibility.  */
    open var visibility = JavaVisibility.DEFAULT

    /** The is static.  */
    var isStatic: Boolean = false

    /** The is final.  */
    var isFinal: Boolean = false

    fun javadoc(javadoc: StringOperator.() -> Unit) {
        javadoc(StringOperator(javaDocLines))
    }

    fun annotation(annotation: StringOperator.() -> Unit) {
        annotation(StringOperator(this.annotations))
    }

    /**
     * Adds the java doc line.
     *
     * @param javaDocLine
     * the java doc line
     */
    fun javadoc(vararg javaDocLine: String) {
        javaDocLines.addAll(javaDocLine)
    }


    /**
     * Adds the annotation.
     *
     * @param annotation
     * the annotation
     */
    fun annotation(vararg annotation: String) {
        annotations.addAll(annotation)
    }

    /**
     * Adds the suppress type warnings annotation.
     */
    fun suppressTypeWarningsAnnotation() {
        annotation("@SuppressWarnings(\"unchecked\")")
    }

    /**
     * Adds the formatted javadoc.
     *
     * @param sb
     * the sb
     * @param indentLevel
     * the indent level
     */
    fun addFormattedJavadoc(sb: StringBuilder, indentLevel: Int) {
        for (javaDocLine in javaDocLines) {
            indent(sb, indentLevel)
            sb.append(javaDocLine)
            newLine(sb)
        }
    }

    /**
     * Adds the formatted annotations.
     *
     * @param sb
     * the sb
     * @param indentLevel
     * the indent level
     */
    open fun addFormattedAnnotations(sb: StringBuilder, indentLevel: Int) {
        for (annotation in annotations) {
            indent(sb, indentLevel)
            sb.append(annotation)
            newLine(sb)
        }
    }

    abstract fun getFormattedContent(indentLevel: Int, compilationUnit: CompilationUnit): String

    companion object {
        private val lineSeparator = System.getProperty("line.separator") ?: "\n"
        var defaultIndent: String = "  "
        var indent: String = defaultIndent

        fun indent(sb: StringBuilder, indentLevel: Int) {
            for (i in 0 until indentLevel) {
                sb.append(indent)
            }
        }

        fun newLine(sb: StringBuilder) {
            sb.append(lineSeparator)
        }
    }
}
