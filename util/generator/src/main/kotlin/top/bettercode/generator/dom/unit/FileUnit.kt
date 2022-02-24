package top.bettercode.generator.dom.unit

import java.io.File
import java.io.StringWriter

/**
 *
 * @author Peter Wu
 */
open class FileUnit(
    override val name: String,
    override var replaceable: Boolean = false,
    override val sourceSet: SourceSet = SourceSet.MAIN,
    override val directorySet: DirectorySet = DirectorySet.RESOURCES
) : GenUnit {

    private val stringWriter = StringWriter()
    override val write: File.() -> Unit = { printWriter().use { it.print(stringWriter.toString()) } }

    operator fun String.unaryPlus() {
        stringWriter.appendln(this)
    }
}