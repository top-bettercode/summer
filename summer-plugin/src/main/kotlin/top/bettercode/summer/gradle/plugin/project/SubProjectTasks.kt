package top.bettercode.summer.gradle.plugin.project

import isBoot
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.application.tasks.CreateStartScripts
import org.springframework.boot.gradle.tasks.bundling.BootJar
import top.bettercode.summer.gradle.plugin.project.template.*
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.dsl.Generators
import top.bettercode.summer.tools.lang.capitalized


/**
 *
 * @author Peter Wu
 */
object SubProjectTasks {

    fun config(project: Project) {
        project.tasks.apply {
            val ext = project.extensions.getByType(GeneratorExtension::class.java)
            ext.run { module, tableHolder ->
                val prefix = module.capitalized()

                if (project.isBoot) {
                    val group = "gen $prefix code"

                    create("gen${prefix}SerializationViews") { t ->
                        t.group = group
                        t.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                ext.tableNames = emptyArray()
                                ext.generators = arrayOf(SerializationViews())
                                Generators.call(ext, tableHolder)
                            }
                        })
                    }

                    project.tasks.create("gen${prefix}Entity") { task ->
                        task.group = group
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                ext.generators = arrayOf(Entity())
                                Generators.call(ext, tableHolder)
                            }
                        })
                    }

                    project.tasks.create("gen${prefix}Form") { task ->
                        task.group = group
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                ext.generators = arrayOf(Form())
                                Generators.call(ext, tableHolder)
                            }
                        })
                    }

                    project.tasks.create("gen${prefix}Service") { task ->
                        task.group = group
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                ext.generators = arrayOf(Service())
                                Generators.call(ext, tableHolder)
                            }
                        })
                    }

                    project.tasks.create("gen${prefix}Controller") { task ->
                        task.group = group
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                ext.generators = arrayOf(Form(), Controller())
                                Generators.call(ext, tableHolder)
                            }
                        })
                    }

                    project.tasks.create("gen${prefix}") { task ->
                        task.group = group
                        task.dependsOn(":core:genCoreEntity")
                        task.doLast(object : Action<Task> {
                            override fun execute(it: Task) {
                                ext.generators = arrayOf(Form(), Service(), Controller())
                                Generators.call(ext, tableHolder)
                            }
                        })
                    }

                }
            }

            if (project.isBoot) {
                create("resolveMainClass") {
                    it.dependsOn("classes")
                    it.doLast(object : Action<Task> {
                        override fun execute(it: Task) {
                            project.tasks.findByName("startScripts").apply {
                                this as CreateStartScripts
                                if (!this.mainClass.isPresent) {
                                    val bootJar = project.tasks.getByName("bootJar")
                                    bootJar as BootJar
                                    mainClass.set(bootJar.mainClassName)
                                }
                            }
                        }
                    })
                }
                named("startScripts") { task ->
                    task.dependsOn("resolveMainClass")
                }
                named("bootJar", BootJar::class.java) {
                    it.launchScript()
                    it.archiveFileName.set("${project.name}-latest.jar")
                }
                named("distZip", Zip::class.java) {
                    it.archiveFileName.set("${project.name}.zip")
                }
            } else {
                named("jar", Jar::class.java) {
                    it.enabled = true
                    it.archiveClassifier.convention("")
                }
                named("bootRun") { it.enabled = false }
                named("bootJar") { it.enabled = false }
                named("bootBuildImage") { it.enabled = false }
            }

            if (!project.isBoot) {
                named("asciidoc") { it.enabled = false }
                named("htmldoc") { it.enabled = false }
                named("postman") { it.enabled = false }
            }
        }
    }
}