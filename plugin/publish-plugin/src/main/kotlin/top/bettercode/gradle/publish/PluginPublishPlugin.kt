package top.bettercode.gradle.publish

import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class PluginPublishPlugin : AbstractPlugin() {
    /**
     * {@inheritDoc}
     */
    override fun apply(project: Project) {
        beforeConfigigure(project)

        project.plugins.apply("java-gradle-plugin")
        project.plugins.apply("com.gradle.plugin-publish")

        if (project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            if (!project.plugins.hasPlugin("org.jetbrains.dokka"))
                project.plugins.apply("org.jetbrains.dokka")

            dokkaTask(project)
        }

        configureDoc(project)

        val projectUrl = project.findProperty("projectUrl") as? String
        val projectVcsUrl = project.findProperty("vcsUrl") as? String
        configureGradlePlugins(project, projectUrl, projectVcsUrl)

        //发布到gradle plugins
        val name = project.name
        project.extensions.configure(PluginBundleExtension::class.java) {
            if (!projectUrl.isNullOrBlank())
                it.website = projectUrl
            if (!projectVcsUrl.isNullOrBlank())
                it.vcsUrl = projectVcsUrl
            it.description = name
            it.tags = setOf(name)
        }

        project.tasks.withType(GenerateModuleMetadata::class.java) {
            it.enabled = false
        }

        configurePluginsPublication(project, projectUrl, projectVcsUrl)

        project.tasks.getByName("publish").dependsOn("publishToMavenLocal")

    }

    /**
     * 配置每个插件的发布信息
     */
    private fun configurePluginsPublication(
        project: Project,
        projectUrl: String?,
        projectVcsUrl: String?
    ) {
        project.afterEvaluate {
            project.extensions.configure(PublishingExtension::class.java) { p ->
                conifgRepository(project, p)
                p.publications.getByName(
                    "pluginMaven",
                    object : Action<Publication> {
                        override fun execute(mavenPublication: Publication) {
                            mavenPublication as MavenPublication
                            mavenPublication.pom.withXml(
                                configurePomXml(
                                    project,
                                    projectUrl,
                                    projectVcsUrl
                                )
                            )
                        }
                    })
            }

            project.extensions.configure(GradlePluginDevelopmentExtension::class.java) {
                with(it.plugins) {
                    forEach { plugin ->
                        project.extensions.configure(PublishingExtension::class.java) { p ->
                            conifgRepository(project, p)

                            p.publications.getByName(
                                plugin.name + "PluginMarkerMaven",
                                object : Action<Publication> {
                                    override fun execute(mavenPublication: Publication) {
                                        mavenPublication as MavenPublication
                                        mavenPublication.pom.withXml { po ->
                                            po.asNode().apply {
                                                configurePomXml(project, projectUrl, projectVcsUrl)
                                            }
                                        }
                                    }
                                })

                        }
                    }

                }
            }
        }
    }

    /**
     * 配置文档生成
     */
    private fun configureDoc(project: Project) {
        when {
            project.plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> {
                project.tasks.create("javadocJar", Jar::class.java) {
                    it.archiveClassifier.set("javadoc")
                    it.from(project.tasks.getByName("dokkaJavadoc").outputs)
                }
            }

            project.plugins.hasPlugin("groovy") -> project.tasks.create(
                "javadocJar",
                Jar::class.java
            ) {
                it.archiveClassifier.set("javadoc")
                it.from(project.tasks.getByName("groovydoc").outputs)
            }

            else -> project.tasks.create("javadocJar", Jar::class.java) {
                it.archiveClassifier.set("javadoc")
                it.from(project.tasks.getByName("javadoc").outputs)
            }
        }

        project.tasks.withType(Javadoc::class.java) {
            with(it.options as StandardJavadocDocletOptions) {
                encoding = project.findProperty("project.encoding") as? String ?: "UTF-8"
                charSet = project.findProperty("project.encoding") as? String ?: "UTF-8"
                isAuthor = true
                isVersion = true
            }
        }
    }


    /**
     * 配置GradlePlugin
     */
    private fun configureGradlePlugins(
        project: Project,
        projectUrl: String?,
        projectVcsUrl: String?
    ) {
        val gradlePlugin = project.findProperty("gradlePlugin.${project.name}.plugins") as? String
            ?: project.findProperty("gradlePlugin.plugins") as? String
        gradlePlugin?.split(",")?.forEach { plugin ->
            val pluginId = project.findProperty("gradlePlugin.plugins.$plugin.id") as String

            project.extensions.configure(GradlePluginDevelopmentExtension::class.java) { extension ->
                extension.plugins.create(plugin) {
                    it.id = pluginId
                    it.implementationClass =
                        project.findProperty("gradlePlugin.plugins.$plugin.implementationClass") as String
                }
            }
            project.extensions.configure(PluginBundleExtension::class.java) { extension ->
                extension.website = projectUrl
                extension.vcsUrl = projectVcsUrl
            }
        }
    }

}