package top.bettercode.summer.gradle.plugin.publish

import org.gradle.api.Project
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
class PluginPublishPlugin : AbstractPublishPlugin() {
    /**
     * {@inheritDoc}
     */
    override fun apply(project: Project) {
        beforeConfigigure(project)

        project.plugins.apply("java-gradle-plugin")
        project.plugins.apply("com.gradle.plugin-publish")

        when {
            project.plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> {
                if (project.findProperty("dokka.enabled") == "true") {
                    if (!project.plugins.hasPlugin("org.jetbrains.dokka"))
                        project.plugins.apply("org.jetbrains.dokka")

                    dokkaTask(project)

                    project.tasks.create("javadocJar", Jar::class.java) {
                        it.group = "documentation"
                        it.archiveClassifier.set("javadoc")
                        it.from(project.tasks.getByName("dokkaJavadoc").outputs)
                    }
                }
            }

            project.plugins.hasPlugin("groovy") -> project.tasks.create(
                "javadocJar",
                Jar::class.java
            ) {
                it.group = "documentation"
                it.archiveClassifier.set("javadoc")
                it.from(project.tasks.getByName("groovydoc").outputs)
            }

            else -> project.tasks.create("javadocJar", Jar::class.java) {
                it.group = "documentation"
                it.archiveClassifier.set("javadoc")
                it.from(project.tasks.getByName("javadoc").outputs)
            }
        }

        val gradlePlugin = (project.findProperty("gradlePlugin.${project.name}.plugins") as String?
            ?: project.findProperty("gradlePlugin.plugins") as String?)?.split(",")
        project.extensions.configure(GradlePluginDevelopmentExtension::class.java) {
            gradlePlugin?.forEach { plugin ->
                val pluginId = project.findProperty("gradlePlugin.plugins.$plugin.id") as String

                it.plugins.create(plugin) {
                    it.id = pluginId
                    it.implementationClass =
                        project.findProperty("gradlePlugin.plugins.$plugin.implementationClass") as String
                }
            }
        }

        project.tasks.withType(Javadoc::class.java) {
            with(it.options as StandardJavadocDocletOptions) {
                encoding = project.findProperty("project.encoding") as String? ?: "UTF-8"
                charSet = project.findProperty("project.encoding") as String? ?: "UTF-8"
                isAuthor = true
                isVersion = true
            }
        }

        val projectUrl = project.findProperty("projectUrl") as String?
        val projectVcsUrl = project.findProperty("vcsUrl") as String?

        project.tasks.withType(GenerateModuleMetadata::class.java) {
            it.enabled = false
        }

        project.afterEvaluate {
            project.extensions.configure(PublishingExtension::class.java) { p ->
                conifgRepository(project, p)
                val plugins =
                    project.extensions.getByType(GradlePluginDevelopmentExtension::class.java).plugins
                val publicationNames =
                    plugins.map { it.name + "PluginMarkerMaven" }.toMutableSet()
                publicationNames.add("pluginMaven")
                publicationNames.forEach {
                    p.publications.getByName(it) { publication ->
                        publication as MavenPublication
                        publication.pom.withXml(configurePomXml(project, projectUrl, projectVcsUrl))
                    }
                }
            }
        }

    }

}