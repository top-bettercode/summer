package top.bettercode.summer.gradle.plugin.autodoc

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.language.jvm.tasks.ProcessResources
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profileProperties
import top.bettercode.summer.gradle.plugin.profile.ProfilePlugin
import top.bettercode.summer.tools.autodoc.AutodocExtension
import java.io.File
import java.util.*

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
@Suppress("ObjectLiteralToLambda")
class AutodocPlugin : Plugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun apply(project: Project) {
        project.plugins.apply(ProfilePlugin::class.java)
        project.extensions.create("autodoc", AutodocExtension::class.java)

        project.extensions.configure(AutodocExtension::class.java) { autodocExtension ->
            autodocExtension.rootSource = project.rootProject.file("doc")
            val apiHost = findProperty(project, "api-host")
            if (!apiHost.isNullOrBlank())
                autodocExtension.apiHost = apiHost
            val path = findProperty(project, "source") ?: "src/doc"
            autodocExtension.source = if (path.startsWith("/")) {
                autodocExtension.source
            } else
                File(project.projectDir, path)
            autodocExtension.projectName = findProperty(project, "project-name")
                    ?: project.findProperty("application.name") as String?
                            ?: "${project.name}接口文档"
            autodocExtension.author = project.findProperty("autodoc.author") as String? ?: "autodoc"
            var version = project.findProperty("autodoc.version") as String?
            if (version.isNullOrBlank()) {
                version = project.findProperty("app.version") as String?
            }
            if (!version.isNullOrBlank()) {
                autodocExtension.version = if (version.startsWith("v")) version else "v$version"
            }
            val authUri = findProperty(project, "auth-uri")
            if (!authUri.isNullOrBlank()) {
                autodocExtension.authUri = authUri
            }

            autodocExtension.toclevels = (findProperty(project, "toclevels") ?: "2").toInt()
            autodocExtension.maxResponseTime =
                    (findProperty(project, "max-response-time") ?: "2000").toInt()

            autodocExtension.signParam = (findProperty(project, "sign-param") ?: "sign")

            val authVariables = (findProperty(project, "auth-variables")
                    ?: "").split(",").asSequence().filter { it.isNotBlank() }.map { it.trim() }.toList()
                    .toTypedArray()

            if (authVariables.isNotEmpty()) {
                autodocExtension.authVariables = authVariables
            }
        }
        val autodoc = project.extensions.findByType(AutodocExtension::class.java)!!

        val docOutputDir = File(
                (project.tasks.getByName("processResources") as ProcessResources).destinationDir.absolutePath,
                "/META-INF/resources/actuator/doc"
        )
        if (autodoc.output == null)
            autodoc.output = docOutputDir

        val group = "autodoc"
        project.tasks.create("asciidoc") { task ->
            task.group = group
            task.dependsOn("processResources")
            task.mustRunAfter("clean", "processResources")
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    val extension = project.extensions.findByType(AutodocExtension::class.java)!!
                    extension.properties = project.profileProperties
                    AsciidocGenerator.asciidoc(extension)
                }
            })
        }
        project.tasks.create("htmldoc") { task ->
            task.group = group
            task.dependsOn("asciidoc")
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    AutodocPlugin::class.java.getResourceAsStream("/favicon.ico")?.copyTo(
                            File(
                                    (project.tasks.getByName("processResources") as ProcessResources).destinationDir.absolutePath,
                                    "/META-INF/resources/favicon.ico"
                            ).apply { parentFile.mkdirs() }.outputStream()
                    )
                    AsciidocGenerator.html(
                            project.extensions.findByType(
                                    AutodocExtension::class.java
                            )!!
                    )
                }
            })
        }
        project.tasks.create("pdfdoc") { task ->
            task.group = group
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    AsciidocGenerator.pdf(
                            project.extensions.findByType(
                                    AutodocExtension::class.java
                            )!!
                    )
                }
            })
        }
        project.tasks.create("postman") { task ->
            task.group = group
            task.mustRunAfter("clean", "processResources")
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    PostmanGenerator.postman(project.extensions.findByType(AutodocExtension::class.java)!!)
                }
            })
        }
        project.tasks.create("setDefaultDesc") { task ->
            task.group = group
            task.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    val extension = project.extensions.findByType(AutodocExtension::class.java)!!
                    val file = project.file("src/main/resources/messages.properties")
                    val source = Properties()
                    if (file.exists()) {
                        source.load(file.inputStream())
                    }
                    AsciidocGenerator.setDefaultDesc(extension, source)
                }
            })
        }
        project.tasks.getByName("jar") {
            it.dependsOn("htmldoc", "postman")
        }
    }

    private fun findProperty(project: Project, key: String) =
            (project.findProperty("autodoc.${project.name}.$key") as String?
                    ?: project.findProperty("autodoc.$key") as String?)

}