package top.bettercode.summer.tools.generator.dom.unit

import java.io.File
import java.io.StringWriter

/**
 *
 * @author Peter Wu
 */
class FileUnit(
        override val name: String,
        override var overwrite: Boolean = true,
        override val sourceSet: SourceSet = SourceSet.ROOT,
        override val directorySet: DirectorySet = DirectorySet.RESOURCES
) : StringWriter(), GenUnit {

    constructor(
            file: File,
            overwrite: Boolean = true,
            sourceSet: SourceSet = SourceSet.ROOT,
            directorySet: DirectorySet = DirectorySet.RESOURCES
    ) : this(file.path, overwrite, sourceSet, directorySet)

    override val write: File.(String) -> Boolean =
            {
                val content = this@FileUnit.toString()
                if (content != it) {
                    printWriter().use { writer -> writer.print(content) }
                    true
                } else {
                    false
                }
            }

    operator fun String.unaryPlus() {
        appendLine(this)
    }

}