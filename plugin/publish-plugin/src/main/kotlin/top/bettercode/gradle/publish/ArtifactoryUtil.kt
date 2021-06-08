package top.bettercode.gradle.publish

import org.gradle.api.Project
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.DoubleDelegateWrapper
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

/**
 *
 * @author Peter Wu
 */
object ArtifactoryUtil {

    /**
     * 发布到artifactory仓库
     */
    fun configureArtifactory(project: Project, publicationNames: MutableSet<String>) {
        val conv = project.convention.plugins["artifactory"] as ArtifactoryPluginConvention
        conv.setContextUrl(project.findProperty("artifactoryContextUrl"))
        conv.publish(closureOf<PublisherConfig> {

            repository(closureOf<DoubleDelegateWrapper> {
                setProperty("repoKey", project.findProperty("artifactoryRepoKey"))
                setProperty("username", project.findProperty("artifactoryUsername"))
                setProperty("password", project.findProperty("artifactoryPassword"))
                setProperty("maven", true)
            })
            defaults(closureOf<ArtifactoryTask> {
                setPublishArtifacts(true)
                publications(*publicationNames.toTypedArray())
            })
        })

        project.tasks.getByName("artifactoryPublish").dependsOn("publishToMavenLocal")
    }

}