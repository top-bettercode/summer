package top.bettercode.generator.dom.java.element

import java.io.PrintWriter

/**
 *
 * @author Peter Wu
 */
class SelfOutputUnit(
    override val name: String,
    override val canCover: Boolean = false,
    override val isResourcesFile: Boolean = false,
    override val isTestFile: Boolean = false
) : GenUnit {

    override val output: PrintWriter.() -> Unit
        get() = { }

}