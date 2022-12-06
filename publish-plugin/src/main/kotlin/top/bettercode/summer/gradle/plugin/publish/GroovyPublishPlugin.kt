package top.bettercode.summer.gradle.plugin.publish

import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class GroovyPublishPlugin : AbstractPublishPlugin() {
    /**
     * {@inheritDoc}
     */
    override fun apply(project: Project) {
        beforeConfigigure(project)

        project.tasks.create("javadocJar", Jar::class.java) {
            it.group = "documentation"
            it.archiveClassifier.set("javadoc")
            it.from(project.tasks.getByName("groovydoc").outputs)
        }

        configPublish(project)
    }
}