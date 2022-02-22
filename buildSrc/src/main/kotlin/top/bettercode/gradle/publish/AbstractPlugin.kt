package top.bettercode.gradle.publish

import groovy.lang.Closure
import groovy.util.Node
import groovy.util.NodeList
import io.codearte.gradle.nexus.NexusStagingExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension
//import org.jetbrains.dokka.DokkaVersion
import org.jetbrains.dokka.gradle.DokkaTask
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

    companion object {
        private const val serialVersionUID: Long = 1L
    }
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
        project.tasks.create("dokkaJavadoc", DokkaTask::class.java) {
            it.outputFormat = "javadoc"
            it.outputDirectory = "${project.buildDir}/dokkaJavadoc"
            it.configuration.apply {
                noAndroidSdkLink = true
                noJdkLink = true
                noStdlibLink = true
            }
        }
//        dokkaJavadoc.offlineMode.set(true)
//        dokkaJavadoc.plugins.dependencies.add(project.dependencies.create("org.jetbrains.dokka:kotlin-as-java-plugin:${DokkaVersion.version}"))
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

        if (project.hasProperty("signing.keyId"))
            project.extensions.getByType(SigningExtension::class.java).apply {
                sign(
                    project.extensions.getByType(PublishingExtension::class.java).publications.findByName(
                        "mavenJava"
                    )
                )
            }

        project.tasks.getByName("publish").dependsOn("publishToMavenLocal")
    }

    /**
     * 配置 Publishing
     */
    private fun configurePublishing(project: Project, projectUrl: String?, projectVcsUrl: String?) {
        project.tasks.withType(GenerateModuleMetadata::class.java) {
            it.enabled = false
        }
        project.extensions.configure(PublishingExtension::class.java) { p ->
            project.findProperty("mavenRepos")?.toString()?.split(",")?.forEach {
                var mavenRepoName = project.findProperty("$it.name") as? String ?: it
                var mavenRepoUrl = project.findProperty("$it.url") as? String
                var mavenRepoUsername = project.findProperty("$it.username") as? String
                var mavenRepoPassword = project.findProperty("$it.password") as? String

                if (project.version.toString().endsWith("SNAPSHOT")) {
                    mavenRepoName = project.findProperty("$it.snapshots.name") as? String
                        ?: mavenRepoName
                    mavenRepoUrl = project.findProperty("$it.snapshots.url") as? String
                        ?: mavenRepoUrl
                    mavenRepoUsername = project.findProperty("$it.snapshots.username") as? String
                        ?: mavenRepoUsername
                    mavenRepoPassword = project.findProperty("$it.snapshots.password") as? String
                        ?: mavenRepoPassword
                }
                if (mavenRepoUrl != null)
                    p.repositories { handler ->
                        handler.maven { repository ->
                            repository.name = mavenRepoName
                            repository.url = URI(mavenRepoUrl)
                            repository.isAllowInsecureProtocol = true
                            repository.credentials { credentials ->
                                credentials.username = mavenRepoUsername
                                credentials.password = mavenRepoPassword
                            }
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
     * 前置配置
     */
    protected fun beforeConfigigure(project: Project) {
        if (!project.plugins.hasPlugin("maven-publish")) {
            project.plugins.apply("maven-publish")
        }
        if (project.hasProperty("signing.keyId") && !project.plugins.hasPlugin("signing")) {
            project.plugins.apply("signing")
        }
        if (!project.rootProject.plugins.hasPlugin("io.codearte.nexus-staging")) {
            project.rootProject.plugins.apply("io.codearte.nexus-staging")
        }

        project.tasks.withType(Javadoc::class.java) {
            it.isFailOnError = false
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
        project.tasks.named("publishToMavenLocal") {
            it.doLast {
                println("${project.name}:${project.version} published to: MavenLocal")
            }
        }
        project.tasks.named("publish") {
            it.doLast {
                project.extensions.getByType(PublishingExtension::class.java).repositories.forEach { repository ->
                    if (repository is MavenArtifactRepository) {
                        println("${project.name}:${project.version} published to: ${repository.url}")
                    } else {
                        println("${project.name}:${project.version} published to: ${repository.name}")
                    }
                }

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
