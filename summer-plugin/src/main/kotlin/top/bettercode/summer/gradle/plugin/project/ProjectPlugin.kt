package top.bettercode.summer.gradle.plugin.project

import isBoot
import isCloud
import isCore
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import top.bettercode.summer.gradle.plugin.autodoc.AutodocPlugin
import top.bettercode.summer.gradle.plugin.dist.DistPlugin
import top.bettercode.summer.gradle.plugin.generator.GenPackageinfoPlugin
import top.bettercode.summer.gradle.plugin.generator.GeneratorPlugin
import top.bettercode.summer.gradle.plugin.profile.ProfilePlugin

/**
 *
 * @author Peter Wu
 */
class ProjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.description = project.findProperty("application.name") as? String

        project.allprojects.forEach { subProject ->
            subProject.plugins.apply("idea")
            subProject.plugins.apply("java")

            subProject.group = subProject.properties["app.packageName"] as String
            subProject.version = subProject.properties["app.version"] as String

            //idea
            subProject.extensions.configure(org.gradle.plugins.ide.idea.model.IdeaModel::class.java) { idea ->
                idea.module {
                    it.inheritOutputDirs = false
                    it.isDownloadJavadoc = false
                    it.isDownloadSources = true
                    val convention = subProject.extensions.getByType(
                        JavaPluginExtension::class.java
                    )
                    it.outputDir = convention.sourceSets
                        .getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.classesDirectory.get().asFile
                    it.testOutputDir = convention.sourceSets
                        .getByName(SourceSet.TEST_SOURCE_SET_NAME).java.classesDirectory.get().asFile
                }
            }

            //java
            subProject.extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) { java ->
                val javaVersionPropertyName = "java.version"
                val javaVersion =
                    JavaVersion.toVersion(subProject.findProperty(javaVersionPropertyName) ?: "8")
                java.sourceCompatibility = javaVersion
                java.targetCompatibility = javaVersion
            }

            //plugins
            subProject.plugins.apply {
                apply(ProfilePlugin::class.java)
                apply(GenPackageinfoPlugin::class.java)
                apply(GeneratorPlugin::class.java)
                apply(AutodocPlugin::class.java)
                apply("org.springframework.boot")
            }

            if (subProject.isBoot) {
                subProject.plugins.apply {
                    apply("application")
                    apply(DistPlugin::class.java)
                }
            }

            //dependencies
            ProjectDependencies.config(subProject)
            //tasks
            SubProjectTasks.config(subProject)

            if (subProject.isCore) {
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