package top.bettercode.summer.tools.generator.dom.java.element

import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.unit.SourceSet


/**
 * The Class Interface.
 *
 * @author Jeff Butler
 */
class Interface(
        type: JavaType,
        override var overwrite: Boolean = false,
        override val sourceSet: SourceSet = SourceSet.MAIN,
        override var visibility: JavaVisibility = JavaVisibility.PUBLIC
) : InnerInterface(type), CompilationUnit {

    override val importedTypes: MutableSet<JavaType> = mutableSetOf()
    override val staticImports: MutableSet<String> = mutableSetOf()
    override val fileCommentLines: MutableList<String> = mutableListOf()

    override val formattedContent: String by lazy { getFormattedContent(0, this) }


    override fun getFormattedContent(indentLevel: Int, compilationUnit: CompilationUnit): String {
        val sb = StringBuilder()

        for (commentLine in fileCommentLines) {
            sb.append(commentLine)
            newLine(sb)
        }

        if (type.packageName.isNotBlank()) {
            sb.append("package ")
            sb.append(type.packageName)
            sb.append(';')
            newLine(sb)
            newLine(sb)
        }

        for (staticImport in staticImports) {
            sb.append("import static ")
            sb.append(staticImport)
            sb.append(';')
            newLine(sb)
        }

        if (staticImports.size > 0) {
            newLine(sb)
        }

        val importStrings = calculateImports(importedTypes)
        for (importString in importStrings) {
            sb.append(importString)
            newLine(sb)
        }

        if (importStrings.isNotEmpty()) {
            newLine(sb)
        }

        sb.append(super.getFormattedContent(0, this))

        return sb.toString()
    }
}
