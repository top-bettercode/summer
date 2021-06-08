package top.bettercode.gradle.publish

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Project

/**
 *
 * @author Peter Wu
 */
object BintrayUtil {

    /**
     * 发布到Jcenter 私有仓库 同步中央仓库或者同步到mavenCentral
     */
    fun configureBintray(
        project: Project,
        publicationNames: MutableSet<String>,
        projectUrl: String?,
        projectVcsUrl: String?
    ) {
        project.extensions.configure(BintrayExtension::class.java) { bintray ->
            with(bintray) {
                user = project.findProperty("bintrayUsername") as? String
                key = project.findProperty("bintrayApiKey") as? String
                setPublications(*publicationNames.toTypedArray())

                publish = true

                with(pkg) {
                    repo = "maven"
                    name = project.findProperty("bintrayPackage") as? String ?: project.name
                    desc = project.name
                    if (!projectUrl.isNullOrBlank()) {
                        websiteUrl = projectUrl
                    }
                    if (!projectVcsUrl.isNullOrBlank())
                        vcsUrl = projectVcsUrl
                    setLicenses(project.findProperty("license.shortName") as? String)
                    setLabels(project.name)

                    with(version) {
                        desc = "${project.name} ${project.version}"
                        with(mavenCentralSync) {
                            sync =
                                (project.findProperty("mavenCentralSync") as? String)?.toBoolean()
                                    ?: false
                            user = project.findProperty("mavenCentralUsername") as? String
                            password = project.findProperty("mavenCentralPassword") as? String
                            close = "1"
                        }
                    }
                }
            }
        }
        project.tasks.getByName("bintrayUpload").dependsOn("publishToMavenLocal")
    }

}