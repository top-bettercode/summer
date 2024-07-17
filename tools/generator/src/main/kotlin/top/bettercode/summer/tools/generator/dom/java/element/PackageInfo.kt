package top.bettercode.summer.tools.generator.dom.java.element

import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.generator.dom.java.StringOperator
import top.bettercode.summer.tools.generator.dom.unit.DirectorySet
import top.bettercode.summer.tools.generator.dom.unit.GenUnit
import top.bettercode.summer.tools.generator.dom.unit.SourceSet
import java.io.File


/**
 * The Class Interface.
 *
 * @author Jeff Butler
 */
class PackageInfo(
        val type: JavaType,
        override var overwrite: Boolean = false,
        override val sourceSet: SourceSet = SourceSet.MAIN,
        override val directorySet: DirectorySet = DirectorySet.JAVA
) : GenUnit {

    override val name: String
        get() = "${
            type.fullyQualifiedNameWithoutTypeParameters.replace(
                    ".",
                    File.separator
            )
        }.${if (DirectorySet.JAVA == directorySet) "java" else "kt"}"


    private val javaDocLines: MutableList<String> = mutableListOf()


    override val write: File.(String) -> Boolean
        get() = {
            val sb = StringBuilder()
            addFormattedJavadoc(sb, 0)
            sb.appendLine("package ${type.packageName}${if (DirectorySet.JAVA == directorySet) ";" else ""}")
            val content = sb.toString()
            if (content != it) {
                printWriter().use { writer -> writer.print(content) }
                true
            } else {
                false
            }
        }

    fun javadoc(javadoc: StringOperator.() -> Unit) {
        javadoc(StringOperator(javaDocLines))
    }

    /**
     * Adds the formatted javadoc.
     *
     * @param sb
     * the sb
     * @param indentLevel
     * the indent level
     */
    private fun addFormattedJavadoc(sb: StringBuilder, indentLevel: Int) {
        for (javaDocLine in javaDocLines) {
            JavaElement.indent(sb, indentLevel)
            sb.append(javaDocLine)
            JavaElement.newLine(sb)
        }
    }

}
