package top.bettercode.generator.dom.java.element

import java.io.PrintWriter

/**
 *
 * @author Peter Wu
 */
class ManualUnit(
    override val name: String,
    override val canCover: Boolean = false,
    override val isResourcesFile: Boolean = false,
    override val isTestFile: Boolean = false,
    override val isRootFile: Boolean= false,
    override val isProjectFile: Boolean= false
) : GenUnit {

    override val output: PrintWriter.() -> Unit = { }

}