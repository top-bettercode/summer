package top.bettercode.gradle.dist

import org.gradle.api.Project
import org.gradle.jvm.application.scripts.TemplateBasedScriptGenerator

/**
 *
 * @author Peter Wu
 */
object StartScript {

    fun startScriptGenerator(
        project: Project,
        dist: DistExtension,
        windows: Boolean
    ): TemplateBasedScriptGenerator {
        return TemplateBasedStartScriptGenerator(project, dist, windows)
    }


}