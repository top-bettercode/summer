package top.bettercode.gradle.publish

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.javadoc.Groovydoc
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

        project.tasks.withType(Groovydoc::class.java) {
            it.source(
                project.extensions.getByType(JavaPluginExtension::class.java).sourceSets.getByName(
                    SourceSet.MAIN_SOURCE_SET_NAME
                ).allSource
            )
        }
        project.tasks.create("javadocJar", Jar::class.java) {
            it.archiveClassifier.set("javadoc")
            it.from(project.tasks.getByName("groovydoc").outputs)
        }
        configPublish(project)
    }
}