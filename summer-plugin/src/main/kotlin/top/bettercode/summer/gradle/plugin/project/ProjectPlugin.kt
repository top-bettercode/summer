package top.bettercode.summer.gradle.plugin.project

import isBoot
import isCloud
import isCore
import isKotlin
import isSpringRoot
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
        val isSpringRoot = project.isSpringRoot

        project.allprojects.forEach { subProject ->
            subProject.plugins.apply("idea")
            subProject.plugins.apply("java")

            subProject.group = (subProject.findProperty("app.package")
                    ?: subProject.findProperty("app.packageName")) as? String ?: ""
            subProject.version = subProject.findProperty("app.version") as? String ?: "1.0"

            //idea
            subProject.extensions.configure(org.gradle.plugins.ide.idea.model.IdeaModel::class.java) { idea ->
                idea.module {
                    it.isDownloadJavadoc = false
                    it.isDownloadSources = true

                    val sourceSets = subProject.extensions.getByType(JavaPluginExtension::class.java).sourceSets
                    it.outputDir = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.classesDirectory.get().asFile
                    it.testOutputDir = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME).java.classesDirectory.get().asFile
                }

            }

            //java
            subProject.extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) { java ->
                val version = subProject.findProperty("java.version") ?: "8"
                val javaVersion = JavaVersion.toVersion(version)
                java.sourceCompatibility = javaVersion
                java.targetCompatibility = javaVersion
            }


            //plugins
            subProject.plugins.apply {
                apply(ProfilePlugin::class.java)
                apply(GenPackageinfoPlugin::class.java)

                if (project.isKotlin)
                    apply("org.jetbrains.kotlin.jvm")

                if (isSpringRoot) {
                    apply(GeneratorPlugin::class.java)
                    apply(AutodocPlugin::class.java)
                    apply("org.springframework.boot")
                } else {
                    apply("application")
                    apply(DistPlugin::class.java)
                }
            }

            if (subProject.isBoot) {
                subProject.plugins.apply {
                    apply("application")
                    apply(DistPlugin::class.java)
                }
            }

            //tasks
            SubProjectTasks.config(subProject)
            //dependencies
            if (isSpringRoot) {
                ProjectDependencies.config(subProject)
                if (subProject.isCore || subProject.isCloud) {
                    CoreProjectTasks.config(subProject)
                }
            }
        }

        //tasks
        if (isSpringRoot)
            RootProjectTasks.config(project)

        //docker
        if (project.isCloud) {
            project.apply { project.plugins.apply(Docker::class.java) }
        }
    }
}