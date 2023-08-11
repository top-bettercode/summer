package top.bettercode.summer.gradle.plugin.publish

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class KotlinPublishPlugin : AbstractPublishPlugin() {
    /**
     * {@inheritDoc}
     */
    override fun apply(project: Project) {
        beforeConfigigure(project)

        if (project.findProperty("dokka.enabled") == "true") {
            project.plugins.apply("org.jetbrains.dokka")
            dokkaTask(project)
            project.tasks.create("javadocJar", Jar::class.java) {
                it.group = "documentation"
                it.archiveClassifier.set("javadoc")
                it.from(project.tasks.getByName("dokkaJavadoc").outputs)
            }
        } else {
            project.tasks.create("javadocJar", Jar::class.java) {
                it.group = "documentation"
                it.archiveClassifier.set("javadoc")
                it.from(project.tasks.getByName("javadoc").outputs)
            }
        }

        configPublish(project)
    }


}