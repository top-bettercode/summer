package cn.bestwu.gradle.publish

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

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
        dokkaTask(project)
        project.afterEvaluate { _ ->
            project.tasks.create("javadocJar", Jar::class.java) {
                it.archiveClassifier.set("javadoc")
                it.from(project.tasks.getByName("dokkaJavadoc").outputs)
            }

            configPublish(project)
        }
    }


}