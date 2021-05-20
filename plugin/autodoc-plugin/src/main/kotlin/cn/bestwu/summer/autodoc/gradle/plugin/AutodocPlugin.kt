package cn.bestwu.summer.autodoc.gradle.plugin

import cn.bestwu.autodoc.core.AsciidocGenerator
import cn.bestwu.autodoc.core.AutodocExtension
import cn.bestwu.autodoc.core.PostmanGenerator
import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.autodoc.core.operation.DocOperationRequest
import cn.bestwu.autodoc.core.operation.DocOperationResponse
import cn.bestwu.gradle.profile.ProfilePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.language.jvm.tasks.ProcessResources
import profileProperties
import java.io.File
import java.util.*

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class AutodocPlugin : Plugin<Project> {

    @Suppress("DEPRECATION")
    override fun apply(project: Project) {
        project.plugins.apply(ProfilePlugin::class.java)
        project.extensions.create("autodoc", AutodocExtension::class.java)

        project.extensions.configure(AutodocExtension::class.java) { autodocExtension ->
            val apiHost = findProperty(project, "api-host")
            if (!apiHost.isNullOrBlank())
                autodocExtension.apiHost = apiHost
            autodocExtension.rootSource = project.rootProject.file("doc")
            val path = findProperty(project, "source") ?: "src/doc"
            autodocExtension.source = if (path.startsWith("/")) {
                autodocExtension.source
            } else
                File(project.file("./"), path)
            autodocExtension.projectName = findProperty(project, "project-name")
                ?: project.findProperty("application.name") as? String
                        ?: "${project.name}接口文档"
            autodocExtension.author = project.findProperty("autodoc.author") as? String ?: "autodoc"
            val output = project.findProperty("autodoc.output") as? String
            if (output != null) {
                autodocExtension.output = File(output)
            }
            var version = project.findProperty("autodoc.version") as? String
            if (version.isNullOrBlank()) {
                version = project.findProperty("app.version") as? String
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
            autodocExtension.wrapResponse =
                (findProperty(project, "wrap-response") ?: "true").toBoolean()

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
            "public/doc"
        )
        if (autodoc.output == null)
            autodoc.output = docOutputDir

        val group = "autodoc"
        project.tasks.create("asciidoc") { task ->
            task.dependsOn("processResources")
            task.mustRunAfter("clean", "processResources")
            configInputOutput(task, group, autodoc, project)
            task.doLast {
                val extension = project.extensions.findByType(AutodocExtension::class.java)!!
                extension.properties = project.profileProperties
                AsciidocGenerator.asciidoc(extension)
            }
        }
        project.tasks.create("htmldoc") { task ->
            task.dependsOn("asciidoc")
            configInputOutput(task, group, autodoc, project)
            task.doLast {
                AsciidocGenerator.html(project.extensions.findByType(AutodocExtension::class.java)!!)
            }
        }
        project.tasks.create("pdfdoc") { task ->
            task.dependsOn("asciidoc")
            configInputOutput(task, group, autodoc, project)
            task.doLast {
                AsciidocGenerator.pdf(project.extensions.findByType(AutodocExtension::class.java)!!)
            }
        }
        project.tasks.create("postman") { task ->
            task.mustRunAfter("clean", "processResources")
            configInputOutput(task, group, autodoc, project)
            task.doLast {
                PostmanGenerator.postman(project.extensions.findByType(AutodocExtension::class.java)!!)
            }
        }
        project.tasks.create("setDefaultDesc") { task ->
            task.group = group
            task.doLast {
                val extension = project.extensions.findByType(AutodocExtension::class.java)!!
                val file = project.file("src/main/resources/messages.properties")
                val source = Properties()
                if (file.exists()) {
                    source.load(file.inputStream())
                }

                setDefaultDesc(extension, source)
            }
        }
        project.tasks.getByName("jar") {
            it.dependsOn("htmldoc", "postman")
        }
        project.afterEvaluate {
            val autodocExtension = project.extensions.findByType(AutodocExtension::class.java)!!
            if (autodocExtension.projectName.isBlank()) {
                autodocExtension.projectName = project.name
            }
        }

        val version = AutodocPlugin::class.java.`package`.implementationVersion
        project.dependencies.add("testImplementation", "cn.bestwu.summer:autodoc-gen:$version")

    }


    fun setDefaultDesc(autodoc: AutodocExtension, properties: Properties) {
        autodoc.listModules { module, _ ->
            module.collections.forEach { collection ->
                collection.operations.forEach { operation ->
                    val request = operation.request as DocOperationRequest
                    val response = operation.response as DocOperationResponse

                    request.uriVariablesExt.setDefaultFieldDesc(properties)
                    request.headersExt.setDefaultFieldDesc(properties)
                    request.parametersExt.setDefaultFieldDesc(properties)
                    request.partsExt.setDefaultFieldDesc(properties)
                    request.contentExt.setDefaultFieldDesc(properties)

                    response.headersExt.setDefaultFieldDesc(properties)
                    response.contentExt.setDefaultFieldDesc(properties)

                    operation.save()
                }
            }
        }
    }

    private fun Set<Field>.setDefaultFieldDesc(properties: Properties) {
        this.forEach {
            if (it.description.isBlank() || it.name == it.description) {
                it.description = properties.getOrDefault(it.name, it.name).toString()
            }
            it.children.setDefaultFieldDesc(properties)
        }
    }

    private fun findProperty(project: Project, key: String) =
        (project.findProperty("autodoc.${project.name}.$key") as? String
            ?: project.findProperty("autodoc.$key") as? String)

    private fun configInputOutput(
        task: Task,
        group: String,
        autodoc: AutodocExtension,
        project: Project
    ) {
        task.group = group

        if (autodoc.source.exists()) {
            task.inputs.dir(autodoc.source)
        }
        if (autodoc.rootSource?.exists() == true) {
            task.inputs.dir(autodoc.rootSource!!)
        }
        task.inputs.file(project.rootProject.file("gradle.properties"))
        if (autodoc.outputFile.exists())
            task.outputs.dir(autodoc.outputFile)
    }
}