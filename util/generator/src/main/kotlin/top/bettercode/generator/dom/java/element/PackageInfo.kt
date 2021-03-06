package top.bettercode.generator.dom.java.element

import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.StringOperator
import top.bettercode.generator.dom.unit.DirectorySet
import top.bettercode.generator.dom.unit.GenUnit
import top.bettercode.generator.dom.unit.SourceSet
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
        get() = "${type.fullyQualifiedNameWithoutTypeParameters.replace(".", File.separator)}.${if (DirectorySet.JAVA == directorySet) "java" else "kt"}"


    private val javaDocLines: MutableList<String> = mutableListOf()


    override val write: File.(String) -> Boolean
        get() = {
            val sb = StringBuilder()
            addFormattedJavadoc(sb, 0)
            sb.append("package ${type.packageName}${if (DirectorySet.JAVA == directorySet) ";" else ""}")
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
