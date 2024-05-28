package top.bettercode.summer.gradle.plugin.project

import isBoot
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profileProperties
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 * @since
 */
@Suppress("ObjectLiteralToLambda")
class Docker : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.apply {
            create("buildDockerCompose") {
                it.dependsOn("processResources", "genDbScript")
                it.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        project.copy { copySpec ->
                            copySpec.from(project.file("docker"))
                            copySpec.exclude { ft -> ft.file == project.file("docker/README.md") }
                            copySpec.into(File(project.layout.buildDirectory.get().asFile, "docker"))

                            val hashtable = Hashtable<String, String>()
                            project.rootProject.profileProperties.forEach { (t, u) ->
                                hashtable[t.toString()] = u.toString()
                            }
                            copySpec.filter(mapOf("tokens" to hashtable), ReplaceTokens::class.java)
                        }
                        project.copy { copySpec ->
                            copySpec.from(project.file("database"))
                            copySpec.exclude { ft ->
                                ft.file == project.file("database/update.sql") || ft.file.startsWith(
                                        project.file("database/doc")
                                ) || ft.file == project.file("database/README.md")
                            }
                            copySpec.into(File(project.layout.buildDirectory.get().asFile, "docker/database"))
                        }
                    }
                })
            }
            create("deployDockerCompose") { task ->
                val mainProjects =
                        project.subprojects.flatMap { it.subprojects }.filter { it.isBoot }
                val bootProjectNames =
                        mainProjects.map { ":${it.parent?.name}:${it.name}:installDist" }
                                .toTypedArray()
                task.dependsOn("buildDockerCompose", *bootProjectNames)

                task.doLast(object : Action<Task> {
                    override fun execute(it: Task) {
                        mainProjects.forEach { p ->
                            File(p.layout.buildDirectory.get().asFile, "install/${p.name}").renameTo(
                                    File(
                                            project.layout.buildDirectory.get().asFile,
                                            "docker/${p.name}"
                                    )
                            )
                        }
                    }
                })
            }
        }
    }
}
