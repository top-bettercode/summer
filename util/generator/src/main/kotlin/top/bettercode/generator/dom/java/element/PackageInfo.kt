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
    override var replaceable: Boolean = false,
    override val sourceSet: SourceSet = SourceSet.MAIN,
    override val directorySet: DirectorySet = DirectorySet.JAVA
) : GenUnit {

    override val name: String
        get() = "${type.fullyQualifiedNameWithoutTypeParameters.replace(".", File.separator)}.java"


    private val javaDocLines: MutableList<String> = mutableListOf()


    override val write: File.() -> Unit
        get() = {
            printWriter().use {
                val sb = StringBuilder()
                addFormattedJavadoc(sb, 0)
                it.print(sb.toString())
                it.println("package ${type.packageName};")
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
