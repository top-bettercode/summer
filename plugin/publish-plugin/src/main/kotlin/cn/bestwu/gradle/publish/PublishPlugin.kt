package cn.bestwu.gradle.publish

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class PublishPlugin : AbstractPlugin() {

    /**
     * {@inheritDoc}
     */
    override fun apply(project: Project) {
        beforeConfigigure(project)

        project.afterEvaluate { _ ->
            project.tasks.create("javadocJar", Jar::class.java) {
                it.archiveClassifier.set("javadoc")
                it.from(project.tasks.getByName("javadoc").outputs)
            }
            configPublish(project)
        }
    }

}