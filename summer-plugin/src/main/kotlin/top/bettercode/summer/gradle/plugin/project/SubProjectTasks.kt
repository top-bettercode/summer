package top.bettercode.summer.gradle.plugin.project

import isBoot
import isCore
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.application.tasks.CreateStartScripts
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import top.bettercode.summer.gradle.plugin.dist.DistExtension.Companion.jvmArgs
import top.bettercode.summer.gradle.plugin.project.template.Controller
import top.bettercode.summer.gradle.plugin.project.template.Entity
import top.bettercode.summer.gradle.plugin.project.template.Service
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.dsl.Generators


/**
 *
 * @author Peter Wu
 */
object SubProjectTasks {

    fun config(project: Project) {
        project.tasks.apply {
            val jvmArgs = project.jvmArgs

            val application = project.extensions.findByType(JavaApplication::class.java)
            if (application != null) {
                application.applicationDefaultJvmArgs += jvmArgs
                application.applicationDefaultJvmArgs =
                    application.applicationDefaultJvmArgs.distinct()
            }

            named("test", Test::class.java) {
                it.useJUnitPlatform()
                it.reports.html.required.set(false)
                it.reports.junitXml.required.set(false)
                if (application != null) it.jvmArgs = application.applicationDefaultJvmArgs.toList()
                else it.jvmArgs = jvmArgs.toList()
            }

            named("build") {
                it.setDependsOn(listOf("testClasses"))
            }

            named("compileJava", JavaCompile::class.java) {
                it.options.compilerArgs.add("-Xlint:unchecked")
                it.options.compilerArgs.add("-parameters")
                it.options.encoding = "UTF-8"
            }
            if (project.isBoot) {
                val ext = project.extensions.getByType(GeneratorExtension::class.java)
                project.tasks.create("genEntity") { task ->
                    task.group = "gen code"
                    task.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            ext.generators = arrayOf(Entity())
                            Generators.callInAllModule(ext)
                        }
                    })
                }
                project.tasks.create("genController") { task ->
                    task.group = "gen code"
                    task.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            ext.generators = arrayOf(Controller())
                            Generators.callInAllModule(ext)
                        }
                    })
                }
                project.tasks.create("genService") { task ->
                    task.group = "gen code"
                    task.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            ext.generators = arrayOf(Service())
                            Generators.callInAllModule(ext)
                        }
                    })
                }

            }
            if (project.isCore) {
                val ext = project.extensions.getByType(GeneratorExtension::class.java)
                project.tasks.create("genCoreEntity") { task ->
                    task.group = "gen core code"
                    task.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            ext.generators = arrayOf(Entity())
                            Generators.callInAllModule(ext)
                        }
                    })
                }
                project.tasks.create("genCoreService") { task ->
                    task.group = "gen core code"
                    task.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            ext.generators = arrayOf(Service())
                            Generators.callInAllModule(ext)
                        }
                    })
                }
                project.tasks.create("genCoreController") { task ->
                    task.group = "gen core code"
                    task.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            ext.generators = arrayOf(Controller())
                            Generators.callInAllModule(ext)
                        }
                    })
                }
            }

            if (project.isBoot) {
                create("resolveMainClass") {
                    it.dependsOn("bootJarMainClassName")
                    it.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            project.tasks.findByName("startScripts").apply {
                                this as CreateStartScripts
                                if (!mainClass.isPresent) {
                                    val bootJar = project.tasks.getByName("bootJar")
                                    bootJar as BootJar
                                    mainClass.set(bootJar.mainClass)
                                }
                            }
                        }
                    })
                }
                named("startScripts") { task ->
                    task.dependsOn("resolveMainClass")
                }
                named("bootRun", BootRun::class.java) {
                    System.getProperties().forEach { t, u ->
                        it.systemProperty(t as String, u)
                    }
                }
                named("bootJar", BootJar::class.java) {
                    it.launchScript()
                }
                named("distZip", Zip::class.java) {
                    it.archiveFileName.set("${project.name}.zip")
                }
            } else {
                named("jar", Jar::class.java) {
                    it.enabled = true
                    it.archiveClassifier.convention("")
                }
                named("bootRunMainClassName") { it.enabled = false }
                named("bootRun") { it.enabled = false }
                named("bootJarMainClassName") { it.enabled = false }
                named("bootJar") { it.enabled = false }
                named("bootBuildImage") { it.enabled = false }
            }

            if (!project.isBoot) {
                named("asciidoc") { it.enabled = false }
                named("htmldoc") { it.enabled = false }
                named("postman") { it.enabled = false }
            } else {
                named("bootJarMainClassName") { it.dependsOn("asciidoc", "htmldoc", "postman") }
            }
        }
    }
}