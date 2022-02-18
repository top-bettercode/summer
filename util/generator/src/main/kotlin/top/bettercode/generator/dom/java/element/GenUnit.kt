package top.bettercode.generator.dom.java.element

import java.io.PrintWriter

/**
 *
 * @author Peter Wu
 */
interface GenUnit {

    val name: String

    val canCover: Boolean
    val isTestFile: Boolean
    val isResourcesFile: Boolean
    val output: PrintWriter.() -> Unit
}