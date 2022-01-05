package plugin

import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import plugin.ProjectPlugin.Companion.isMain
import plugin.ProjectPlugin.Companion.needDoc
import top.bettercode.gradle.dist.jvmArgs


/**
 *
 * @author Peter Wu
 */
object SubProjectTasks {

    fun config(project: Project) {
        project.tasks.apply {
            val jvmArgs = project.jvmArgs

            val application =
                project.convention.findPlugin(ApplicationPluginConvention::class.java)
            if (application != null) {
                application.applicationDefaultJvmArgs += jvmArgs
                application.applicationDefaultJvmArgs =
                    application.applicationDefaultJvmArgs.distinct()
            }

            named("test", Test::class.java) {
                it.useJUnitPlatform()
                if (application != null)
                    it.jvmArgs = application.applicationDefaultJvmArgs.toList()
                else
                    it.jvmArgs = jvmArgs.toList()
            }

            named("build") {
                it.setDependsOn(listOf("testClasses"))
            }

            named("compileJava", JavaCompile::class.java) {
                it.options.compilerArgs.add("-Xlint:unchecked")
                it.options.compilerArgs.add("-parameters")
                it.options.encoding = "UTF-8"
            }

            if (project.isMain) {
                create("resolveMainClass") {
                    it.doLast {
                        project.tasks.findByName("startScripts").apply {
                            this as CreateStartScripts
                            if (mainClassName.isNullOrBlank()) {
                                val bootJar = project.tasks.getByName("bootJar")
                                bootJar as BootJar
                                mainClassName = bootJar.mainClassName
                            }
                        }
                    }
                }
                named("startScripts", CreateStartScripts::class.java) { scripts ->
                    scripts.dependsOn("resolveMainClass")
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
            }

            if (project.needDoc && !project.isMain) {
                named("asciidoc") { it.enabled = false }
                named("htmldoc") { it.enabled = false }
                named("postman") { it.enabled = false }
            }
        }
    }
}