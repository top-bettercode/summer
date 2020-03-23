package cn.bestwu.gradle.publish

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.DokkaTask

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class KotlinPublishPlugin : AbstractPlugin() {
    /**
     * {@inheritDoc}
     */
    override fun apply(project: Project) {
        beforeConfigigure(project)

        project.plugins.apply("org.jetbrains.dokka")
        project.tasks.create("dokkaJavadoc", DokkaTask::class.java) {
            it.outputFormat = "javadoc"
            it.outputDirectory = "${project.buildDir}/dokkaJavadoc"
        }
        project.afterEvaluate { _ ->
            project.tasks.create("javadocJar", Jar::class.java) {
                it.classifier = "javadoc"
                it.from(project.tasks.getByName("dokkaJavadoc").outputs)
            }

            configPublish(project)
        }
    }

}