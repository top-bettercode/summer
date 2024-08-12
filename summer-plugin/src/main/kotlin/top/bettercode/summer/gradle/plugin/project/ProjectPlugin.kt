package top.bettercode.summer.gradle.plugin.project

import isBoot
import isCloud
import isCore
import org.gradle.api.Plugin
import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.autodoc.AutodocPlugin
import top.bettercode.summer.gradle.plugin.generator.GenPackageinfoPlugin
import top.bettercode.summer.gradle.plugin.generator.GeneratorPlugin

/**
 *
 * @author Peter Wu
 */
class ProjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        ProjectDistPlugin.config(project) {
            it.isBoot
        }

        project.allprojects.forEach { subProject ->
            //plugins
            subProject.plugins.apply {
                apply(GenPackageinfoPlugin::class.java)
                apply(GeneratorPlugin::class.java)
                apply(AutodocPlugin::class.java)
                apply("org.springframework.boot")
            }

            //dependencies
            ProjectDependencies.config(subProject)

            //tasks
            SubProjectTasks.config(subProject)

            if (subProject.isCore || subProject.isCloud) {
                CoreProjectTasks.config(subProject)
            }
        }

        //tasks
        RootProjectTasks.config(project)

        //docker
        if (project.isCloud) {
            project.apply { project.plugins.apply(Docker::class.java) }
        }
    }
}