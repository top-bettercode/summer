package top.bettercode.gradle.publish

import com.jfrog.bintray.gradle.BintrayExtension
import groovy.lang.Closure
import groovy.util.Node
import groovy.util.NodeList
import io.codearte.gradle.nexus.CloseAndReleaseAllRepositoryTask
import io.codearte.gradle.nexus.NexusStagingExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.DokkaVersion
import org.jetbrains.dokka.gradle.DokkaTask
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.DoubleDelegateWrapper
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask
import java.net.URI

/**
 * @author Peter Wu
 * @since
 */
/**
 * 获取单一节点
 */
fun Node.getAt(name: String): Node? {
    val nodeList = get(name) as NodeList
    return if (nodeList.size > 0)
        nodeList[0] as Node
    else null
}

/**
 * 配置工具类
 */
class KotlinClosure1<in T : Any, V : Any>(
    /**
     *
     */
    private val function: T.() -> V?,
    owner: Any? = null,
    thisObject: Any? = null
) : Closure<V?>(owner, thisObject) {
    /**
     * 实际调用方法
     */
    @Suppress("unused") // to be called dynamically by Groovy
    fun doCall(it: T): V? = it.function()
}

/**
 * 配置工具类
 */
fun <T : Any> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
    KotlinClosure1(action, this, this)

/**
 * 抽象类
 */
abstract class AbstractPlugin : Plugin<Project> {

    /**
     * 配置dokkaDoc
     */
    protected fun dokkaTask(project: Project) {
        val dokkaJavadoc = project.tasks.findByName("dokkaJavadoc")
        dokkaJavadoc as DokkaTask
        dokkaJavadoc.offlineMode.set(true)
        dokkaJavadoc.plugins.dependencies.add(project.dependencies.create("org.jetbrains.dokka:kotlin-as-java-plugin:${DokkaVersion.version}"))
    }

    /**
     * 公用配置
     */
    protected fun configPublish(project: Project, publicationName: Array<String> = arrayOf()) {

        if (project.group.toString().isBlank()) {
            return
        }

        project.tasks.withType(Javadoc::class.java) {
            with(it.options as StandardJavadocDocletOptions) {
                encoding = project.findProperty("project.encoding") as? String ?: "UTF-8"
                charSet = project.findProperty("project.encoding") as? String ?: "UTF-8"
                isAuthor = true
                isVersion = true
            }
        }

        val projectUrl = project.findProperty("projectUrl") as? String
        val projectVcsUrl = project.findProperty("vcsUrl") as? String

        configurePublishing(project, projectUrl, projectVcsUrl)

        val publicationNames = mutableSetOf<String>()
        publicationNames.add("mavenJava")
        publicationNames.addAll(publicationName)

        project.extensions.getByType(SigningExtension::class.java).apply {
            sign(
                project.extensions.getByType(PublishingExtension::class.java).publications.findByName(
                    "mavenJava"
                )
            )
        }

        project.tasks.getByName("publish").dependsOn("publishToMavenLocal")

        if (project.plugins.hasPlugin("com.jfrog.artifactory"))
            configureArtifactory(project, publicationNames)


        configureBintray(project, publicationNames, projectUrl, projectVcsUrl)
    }

    /**
     * 配置 Publishing
     */
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun configurePublishing(project: Project, projectUrl: String?, projectVcsUrl: String?) {
        project.tasks.withType(GenerateModuleMetadata::class.java) {
            it.enabled = false
        }
        project.extensions.configure(PublishingExtension::class.java) { p ->
            var mavenRepoName = project.findProperty("mavenRepo.name") as? String ?: "mavenDeployer"
            var mavenRepoUrl = project.findProperty("mavenRepo.url") as? String
            var mavenRepoUsername = project.findProperty("mavenRepo.username") as? String
            var mavenRepoPassword = project.findProperty("mavenRepo.password") as? String

            if (project.version.toString().endsWith("SNAPSHOT")) {
                mavenRepoName = project.findProperty("mavenRepo.snapshots.name") as? String
                    ?: mavenRepoName
                mavenRepoUrl = project.findProperty("mavenRepo.snapshots.url") as? String
                    ?: mavenRepoUrl
                mavenRepoUsername = project.findProperty("mavenRepo.snapshots.username") as? String
                    ?: mavenRepoUsername
                mavenRepoPassword = project.findProperty("mavenRepo.snapshots.password") as? String
                    ?: mavenRepoPassword
            }
            if (mavenRepoUrl != null)
                p.repositories { handler ->
                    handler.maven { repository ->
                        repository.name = mavenRepoName
                        repository.url = URI(mavenRepoUrl)
                        repository.isAllowInsecureProtocol = true
                        repository.credentials {
                            it.username = mavenRepoUsername
                            it.password = mavenRepoPassword
                        }
                    }
                }

            p.publications.create("mavenJava", MavenPublication::class.java) { m ->
                if (project.plugins.hasPlugin("war")) {
                    m.from(project.components.getByName("web"))
                } else {
                    m.from(project.components.getByName("java"))
                }

                m.artifact(project.tasks.getByName("sourcesJar")) {
                    it.classifier = "sources"
                }

                m.artifact(project.tasks.getByName("javadocJar")) {
                    it.classifier = "javadoc"
                }

                m.pom.withXml(configurePomXml(project, projectUrl, projectVcsUrl))
            }

        }
    }

    /**
     * 配置pom.xml相关信息
     */
    private fun configurePomXml(
        project: Project,
        projectUrl: String?,
        projectVcsUrl: String?
    ): (XmlProvider) -> Unit {
        return {
            val root = it.asNode()

            /**
             * 配置pom.xml相关信息
             */
            root.apply {
                if (getAt("packaging") == null)
                    appendNode("packaging", if (project.plugins.hasPlugin("war")) "war" else "jar")
                configurePomXml(project, projectUrl, projectVcsUrl)
            }
        }
    }

    /**
     * 发布到artifactory仓库
     */
    private fun configureArtifactory(project: Project, publicationNames: MutableSet<String>) {
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

    /**
     * 发布到Jcenter 私有仓库 同步中央仓库或者同步到mavenCentral
     */
    private fun configureBintray(
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

    /**
     * 前置配置
     */
    protected fun beforeConfigigure(project: Project) {
        if (!project.plugins.hasPlugin("maven-publish")) {
            project.plugins.apply("maven-publish")
        }
        if (!project.plugins.hasPlugin("signing")) {
            project.plugins.apply("signing")
        }
        if (!project.rootProject.plugins.hasPlugin("io.codearte.nexus-staging")) {
            project.rootProject.plugins.apply("io.codearte.nexus-staging")
        }
        if (!project.plugins.hasPlugin("com.jfrog.bintray")) {
            project.plugins.apply("com.jfrog.bintray")
        }

//        源文件打包Task
        project.pluginManager.apply(JavaPlugin::class.java)
        project.tasks.create("sourcesJar", Jar::class.java) {
            it.dependsOn("classes")
            it.archiveClassifier.set("sources")
            it.from(
                project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(
                    "main"
                ).allSource
            )
        }
        project.tasks.named("jar", Jar::class.java) {
            it.manifest { manifest ->
                manifest.attributes(
                    mapOf(
                        "Manifest-Version" to project.version,
                        "Implementation-Title" to "${if (project != project.rootProject) "${project.rootProject.name}:" else ""}${project.name}",
                        "Implementation-Version" to project.version
                    )
                )
            }
        }
        val extension = project.rootProject.extensions.getByType(NexusStagingExtension::class.java)
        extension.apply {
            //required only for projects registered in Sonatype after 2021-02-24
            serverUrl = project.rootProject.findProperty("nexusStaging.serverUrl")?.toString()
                ?: "https://s01.oss.sonatype.org/service/local/"
            //optional if packageGroup == project.getGroup()
            val packageGroup = project.rootProject.findProperty("nexusStaging.packageGroup")
            if (packageGroup != null) {
                this.packageGroup = packageGroup.toString()
            }
            //when not defined will be got from server using "packageGroup"
            val stagingProfileId = project.rootProject.findProperty("nexusStaging.stagingProfileId")
            if (stagingProfileId != null) {
                this.stagingProfileId = stagingProfileId.toString()
            }
            val stagingRepositoryId =
                project.rootProject.findProperty("nexusStaging.stagingRepositoryId")
            if (stagingRepositoryId != null) {
                this.stagingRepositoryId.set(stagingRepositoryId.toString())
            }

            var mavenRepoUsername = project.findProperty("mavenRepo.username") as? String
            var mavenRepoPassword = project.findProperty("mavenRepo.password") as? String

            if (project.version.toString().endsWith("SNAPSHOT")) {
                mavenRepoUsername = project.findProperty("mavenRepo.snapshots.username") as? String
                    ?: mavenRepoUsername
                mavenRepoPassword = project.findProperty("mavenRepo.snapshots.password") as? String
                    ?: mavenRepoPassword
            }
            this.username = mavenRepoUsername
            this.password = mavenRepoPassword
        }

//        if (project.rootProject.tasks.findByName("closeAndReleaseAllRepository") == null) {
//            project.rootProject.tasks.create(
//                "closeAndReleaseAllRepository",
//                CloseAndReleaseAllRepositoryTask::class.java,
//                project,
//                extension
//            ).apply {
//                group = "release"
//                description = "Closes and Releases all open artifacts repository in Nexus"
//
//                serverUrl = extension.serverUrl
//                username = extension.username
//                password = extension.password
//                packageGroup = if (extension.packageGroup.isNullOrBlank()) {
//                    project.rootProject.group.toString()
//                } else {
//                    extension.packageGroup
//                }
//
//                stagingProfileId = extension.stagingProfileId
//                numberOfRetries = extension.numberOfRetries
//                delayBetweenRetriesInMillis = extension.delayBetweenRetriesInMillis
//                repositoryDescription = extension.repositoryDescription
//            }
//        }

        project.tasks.withType(Javadoc::class.java) {
            it.isFailOnError = false
        }
    }

    /**
     * 配置pom.xml相关信息
     */
    protected fun Node.configurePomXml(
        project: Project,
        projectUrl: String?,
        projectVcsUrl: String?
    ) {
        appendNode("name", project.name)
        appendNode(
            "description",
            if (!project.description.isNullOrBlank()) project.description else project.name
        )
        if (!projectUrl.isNullOrBlank())
            appendNode("url", projectUrl)

        val license = appendNode("licenses").appendNode("license")
        license.appendNode("name", project.findProperty("license.name"))
        license.appendNode("url", project.findProperty("license.url"))
        license.appendNode("distribution", project.findProperty("license.distribution"))

        val developer = appendNode("developers").appendNode("developer")
        developer.appendNode("id", project.findProperty("developer.id"))
        developer.appendNode("name", project.findProperty("developer.name"))
        developer.appendNode("email", project.findProperty("developer.email"))

        if (projectVcsUrl != null && projectVcsUrl.isNotBlank()) {
            val scm = appendNode("scm")
            scm.appendNode("url", projectVcsUrl)
            val tag =
                if (projectVcsUrl.contains("git")) "git" else if (projectVcsUrl.contains("svn")) "svn" else projectVcsUrl
            scm.appendNode("connection", "scm:$tag:$projectVcsUrl")
            scm.appendNode("developerConnection", "scm:$tag:$projectVcsUrl")
        }
    }
}
