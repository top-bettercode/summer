package top.bettercode.summer.gradle.plugin.project

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import top.bettercode.summer.gradle.plugin.dist.DistPlugin
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profileFiles
import top.bettercode.summer.gradle.plugin.profile.ProfilePlugin

/**
 *
 * @author Peter Wu
 */
class ProjectDistPlugin : Plugin<Project> {

    companion object {
        fun config(project: Project, isBoot: (Project) -> Boolean = { true }) {
            project.description = project.findProperty("application.name") as String?

            project.allprojects.forEach { subProject ->
                subProject.plugins.apply("idea")
                subProject.plugins.apply("java")

                subProject.group = (subProject.findProperty("app.package")
                    ?: subProject.findProperty("app.packageName")) as String? ?: ""
                subProject.version = subProject.findProperty("app.version") as String? ?: "1.0"

                //idea
                subProject.extensions.configure(org.gradle.plugins.ide.idea.model.IdeaModel::class.java) { idea ->
                    idea.module {
                        it.isDownloadJavadoc = false
                        it.isDownloadSources = true

                        val sourceSets =
                            subProject.extensions.getByType(JavaPluginExtension::class.java).sourceSets
                        it.outputDir =
                            sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).java.classesDirectory.get().asFile
                        it.testOutputDir =
                            sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME).java.classesDirectory.get().asFile
                    }

                }

                val version = subProject.findProperty("java.version") ?: "8"
                val javaVersion = JavaVersion.toVersion(version)
                //java
                subProject.extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) { java ->
                    java.sourceCompatibility = javaVersion
                    java.targetCompatibility = javaVersion
                }


                //plugins
                subProject.plugins.apply {
                    apply(ProfilePlugin::class.java)

                    if ("true" == project.findProperty("kotlin.enabled")) {
                        apply("org.jetbrains.kotlin.jvm")
                        apply("org.jetbrains.kotlin.plugin.spring")
                        subProject.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java)
                            .configureEach {
                                it.kotlinOptions {
                                    jvmTarget = javaVersion.toString()
                                }
                            }
                    }
                    if (isBoot(subProject)) {
                        apply("application")
                        apply(DistPlugin::class.java)
                    }
                }

                //tasks
                subProject.tasks.apply {
                    named("test", Test::class.java) {
                        it.useJUnitPlatform()
                        it.reports.html.required.set(false)
                        it.reports.junitXml.required.set(false)
                    }

                    named("build") {
                        it.setDependsOn(listOf("testClasses"))
                    }

                    named("compileJava", JavaCompile::class.java) {
                        it.options.compilerArgs.add("-Xlint:unchecked")
                        it.options.compilerArgs.add("-parameters")
                        it.options.encoding = "UTF-8"
                        it.inputs.files(*project.profileFiles)
                    }

                    named("compileTestJava", JavaCompile::class.java) {
                        it.options.compilerArgs.add("-Xlint:unchecked")
                        it.options.compilerArgs.add("-parameters")
                        it.options.encoding = "UTF-8"
                        it.inputs.files(*project.profileFiles)
                    }
                    withType(JavaExec::class.java) {
                        System.getProperties().forEach { t, u ->
                            it.systemProperty(t as String, u)
                        }
                    }
                }
            }
        }
    }

    override fun apply(project: Project) {
        config(project)
    }
}