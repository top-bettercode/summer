package top.bettercode.generator.dom.unit

import java.io.File
import java.io.StringWriter

/**
 *
 * @author Peter Wu
 */
open class FileUnit(
    override val name: String,
    override var overwrite: Boolean = true,
    override val sourceSet: SourceSet = SourceSet.ROOT,
    override val directorySet: DirectorySet = DirectorySet.RESOURCES
) : GenUnit {

    constructor(
        file: File,
        overwrite: Boolean = true,
        sourceSet: SourceSet = SourceSet.ROOT,
        directorySet: DirectorySet = DirectorySet.RESOURCES
    ) : this(file.path, overwrite, sourceSet, directorySet)

    private val stringWriter = StringWriter()
    override val write: File.() -> Unit =
        { printWriter().use { it.print(stringWriter.toString()) } }

    operator fun String.unaryPlus() {
        stringWriter.appendln(this)
    }
}