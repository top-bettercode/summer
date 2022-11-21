package top.bettercode.gradle.publish

import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class GroovyPublishPlugin : AbstractPlugin() {
    /**
     * {@inheritDoc}
     */
    override fun apply(project: Project) {
        beforeConfigigure(project)

        project.tasks.create("javadocJar", Jar::class.java) {
            it.archiveClassifier.set("javadoc")
            it.from(project.tasks.getByName("groovydoc").outputs)
        }
        configPublish(project)
    }
}