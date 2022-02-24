package plugin

import hudson.cli.CLI
import org.gradle.api.Project
import top.bettercode.generator.dom.unit.FileUnit
import top.bettercode.gradle.generator.GeneratorPlugin


/**
 *
 * @author Peter Wu
 */
object RootProjectTasks {

    fun config(project: Project) {
        project.tasks.apply {
            val jenkinsJobs = project.findProperty("jenkins.jobs")?.toString()?.split(",")
                ?.filter { it.isNotBlank() }
            val jenkinsServer = project.findProperty("jenkins.server")?.toString()
            val jenkinsAuth = project.findProperty("jenkins.auth")?.toString()
            if (!jenkinsJobs.isNullOrEmpty() && !jenkinsAuth.isNullOrBlank() && !jenkinsServer.isNullOrBlank()) {
                create("jenkins[All]") {
                    it.group = "jenkins"
                    it.doLast {
                        jenkinsJobs.forEach { jobName ->
                            CLI._main(
                                arrayOf(
                                    "-s",
                                    jenkinsServer,
                                    "-auth",
                                    jenkinsAuth,
                                    "build",
                                    jobName,
                                    "-s",
                                    "-v"
                                )
                            )
                        }
                    }
                }
                jenkinsJobs.forEach { jobName ->
                    val jobTaskName = jobName.replace(
                        "[()\\[\\]{}|/]|\\s*|\t|\r|\n|".toRegex(),
                        ""
                    )
                    create("jenkins[$jobTaskName]") {
                        it.group = "jenkins"
                        it.doLast {
                            CLI._main(
                                arrayOf(
                                    "-s",
                                    jenkinsServer,
                                    "-auth",
                                    jenkinsAuth,
                                    "build",
                                    jobName,
                                    "-s",
                                    "-v"
                                )
                            )
                        }
                    }
                }
            }

            create("genDbScript") { t ->
                t.group = GeneratorPlugin.taskGroup
                t.doLast {
                    val destFile = FileUnit("database/init.sql")
                    destFile.apply {
                        +("SET NAMES 'utf8';")
//                    +(project.rootProject.file("database/database.sql").readText())
                        project.rootProject.file("database/ddl").listFiles()?.filter { it.isFile }
                            ?.forEach {
                                +(it.readText())
                            }
                        project.rootProject.file("database/init").listFiles()?.filter { it.isFile }
                            ?.forEach {
                                +(it.readText())
                            }
                    }
                    destFile.writeTo(project.rootDir)
                }
            }

            create("prettyConfig") { t ->
                t.doLast {
                    ConfigTool.prettyConfig(
                        project.file("conf"),
                        project.subprojects.map { it.file("src/main/resources/application.yml") }
                            .filter { it.exists() })
                }
            }
        }

    }
}