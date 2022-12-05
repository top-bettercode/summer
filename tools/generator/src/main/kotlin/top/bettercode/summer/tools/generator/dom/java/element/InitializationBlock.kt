package top.bettercode.summer.tools.generator.dom.java.element

import top.bettercode.summer.tools.generator.dom.java.StringOperator
import top.bettercode.summer.tools.generator.dom.java.element.JavaElement.Companion.indent
import top.bettercode.summer.tools.generator.dom.java.element.JavaElement.Companion.newLine

class InitializationBlock @JvmOverloads constructor(private var isStatic: Boolean = false) {
    private val bodyLines: MutableList<String> = mutableListOf()
    private val javaDocLines: MutableList<String> = mutableListOf()

    operator fun String.unaryPlus() {
        bodyLines.add(this)
    }

    fun javadoc(javadoc: StringOperator.() -> Unit) {
        javadoc(StringOperator(javaDocLines))
    }

    fun bodyLine(vararg line: String) {
        bodyLines.addAll(line)
    }

    fun javadoc(vararg javaDocLine: String) {
        javaDocLines.addAll(javaDocLine)
    }

    fun getFormattedContent(indentLevel: Int): String {
        var indentLevelInner = indentLevel
        val sb = StringBuilder()

        for (javaDocLine in javaDocLines) {
            indent(sb, indentLevelInner)
            sb.append(javaDocLine)
            newLine(sb)
        }

        indent(sb, indentLevelInner)

        if (isStatic) {
            sb.append("static ")
        }

        sb.append('{')
        indentLevelInner++

        val listIter = bodyLines.listIterator()
        while (listIter.hasNext()) {
            val line = listIter.next()
            if (line.startsWith("}")) {
                indentLevelInner--
            }

            newLine(sb)
            indent(sb, indentLevelInner)
            sb.append(line)

            if (line.endsWith("{") && !line.startsWith("switch")
                || line.endsWith(":")
            ) {
                indentLevelInner++
            }

            if (line.startsWith("break")) {
                // if the next line is '}', then don't outdent
                if (listIter.hasNext()) {
                    val nextLine = listIter.next()
                    if (nextLine.startsWith("}")) {
                        indentLevelInner++
                    }

                    // set back to the previous element
                    listIter.previous()
                }
                indentLevelInner--
            }
        }

        indentLevelInner--
        newLine(sb)
        indent(sb, indentLevelInner)
        sb.append('}')

        return sb.toString()
    }
}
