package cn.bestwu.gradle.publish

import org.gradle.api.Project

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

        project.afterEvaluate { _ ->
//            project.tasks.withType(Groovydoc::class.java){
//                it.source(project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName("main").allSource)
//            }
//            project.tasks.create("javadocJar", Jar::class.java) {
//                it.archiveClassifier.set("javadoc")
//                it.from(project.tasks.getByName("groovydoc").outputs)
//            }
            configPublish(project)
        }
    }
}