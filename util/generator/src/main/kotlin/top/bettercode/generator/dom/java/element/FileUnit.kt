package top.bettercode.generator.dom.java.element

import java.io.PrintWriter
import java.io.StringWriter

/**
 *
 * @author Peter Wu
 */
open class FileUnit(
    override val name: String,
    override val canCover: Boolean = false,
    override val isTestFile: Boolean = false,
    override val isResourcesFile: Boolean = false
) : GenUnit {

    private val stringWriter = StringWriter()

    override val output: PrintWriter.() -> Unit
        get() = { use { println(stringWriter.toString()) } }

    operator fun String.unaryPlus() {
        stringWriter.appendln(this)
    }
}