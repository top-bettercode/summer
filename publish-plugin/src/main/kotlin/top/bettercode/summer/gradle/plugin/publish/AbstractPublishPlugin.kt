package top.bettercode.summer.gradle.plugin.publish

import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.*
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.DokkaVersion
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

/**
 * @author Peter Wu
 * @since
 */
/**
 * 获取单一节点
 */


/**
 * 抽象类
 */
abstract class AbstractPublishPlugin : Plugin<Project> {

    companion object {
        fun conifgRepository(
                project: Project,
                p: PublishingExtension
        ) {
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
        }


    }


    private fun Node.getAt(name: String): Node? {
        val nodeList = get(name) as NodeList
        return if (nodeList.size > 0)
            nodeList[0] as Node
        else null
    }

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
    protected fun configPublish(project: Project) {
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

        project.tasks.withType(GenerateModuleMetadata::class.java) {
            it.enabled = false
        }
        project.extensions.configure(PublishingExtension::class.java) { p ->
            conifgRepository(project, p)

            p.publications.create("mavenJava", MavenPublication::class.java) { mavenPublication ->
                if (project.plugins.hasPlugin("war")) {
                    mavenPublication.from(project.components.getByName("web"))
                } else {
                    mavenPublication.from(project.components.getByName("java"))
                }

                mavenPublication.artifact(project.tasks.getByName("sourcesJar")) {
                    it.classifier = "sources"
                }

                mavenPublication.artifact(project.tasks.getByName("javadocJar")) {
                    it.classifier = "javadoc"
                }

                mavenPublication.pom.withXml(configurePomXml(project, projectUrl, projectVcsUrl))
            }

        }

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
     * 配置pom.xml相关信息
     */
    protected fun configurePomXml(
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

                if (!projectVcsUrl.isNullOrBlank()) {
                    val scm = appendNode("scm")
                    scm.appendNode("url", projectVcsUrl)
                    val tag =
                            if (projectVcsUrl.contains("git")) "git" else if (projectVcsUrl.contains("svn")) "svn" else projectVcsUrl
                    scm.appendNode("connection", "scm:$tag:$projectVcsUrl")
                    scm.appendNode("developerConnection", "scm:$tag:$projectVcsUrl")
                }
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

        project.tasks.withType(Javadoc::class.java) {
            it.isFailOnError = false
        }

//        源文件打包Task
        project.pluginManager.apply(JavaPlugin::class.java)
        project.tasks.create("sourcesJar", Jar::class.java) {
            it.dependsOn("classes")
            it.archiveClassifier.set("sources")
            it.from(
                    project.extensions.getByType(JavaPluginExtension::class.java).sourceSets.getByName(
                            SourceSet.MAIN_SOURCE_SET_NAME
                    ).allSource
            )
        }
        project.tasks.named("jar", Jar::class.java) {
            it.manifest { manifest ->
                manifest.attributes(
                        mapOf(
                                "Manifest-Version" to project.version,
                                "Implementation-Title" to "${project.rootProject.name}${project.path}",
                                "Implementation-Version" to project.version
                        )
                )
            }
        }
        project.tasks.named("javadoc", Javadoc::class.java) {
            it.options { options ->
                options as StandardJavadocDocletOptions
                options.addStringOption("Xdoclint:none", "-quiet")
            }
        }
        project.tasks.named("publishToMavenLocal") {
            it.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    println("${project.group}:${project.name}:${project.version} published to: MavenLocal")
                }
            })
        }
        project.tasks.named("publish") {
            it.doLast(object : Action<Task> {
                override fun execute(it: Task) {
                    project.extensions.getByType(PublishingExtension::class.java).repositories.forEach { repository ->
                        if (repository is MavenArtifactRepository) {
                            println("${project.group}:${project.name}:${project.version} published to: ${repository.url}")
                        } else {
                            println("${project.group}:${project.name}:${project.version} published to: ${repository.name}")
                        }
                    }
                }
            })
        }

//        if (!project.rootProject.plugins.hasPlugin("io.codearte.nexus-staging")) {
//            project.rootProject.plugins.apply("io.codearte.nexus-staging")
//        }
//        project.rootProject.extensions.configure(NexusStagingExtension::class.java) {
//            //required only for projects registered in Sonatype after 2021-02-24
//            it.serverUrl = project.rootProject.findProperty("nexusStaging.serverUrl")?.toString()
//                ?: "https://s01.oss.sonatype.org/service/local/"
//            //optional if packageGroup == project.getGroup()
//            val packageGroup = project.rootProject.findProperty("nexusStaging.packageGroup")
//            if (packageGroup != null) {
//                it.packageGroup = packageGroup.toString()
//            }
//            //when not defined will be got from server using "packageGroup"
//            val stagingProfileId = project.rootProject.findProperty("nexusStaging.stagingProfileId")
//            if (stagingProfileId != null) {
//                it.stagingProfileId = stagingProfileId.toString()
//            }
//            val stagingRepositoryId =
//                project.rootProject.findProperty("nexusStaging.stagingRepositoryId")
//            if (stagingRepositoryId != null) {
//                it.stagingRepositoryId.set(stagingRepositoryId.toString())
//            }
//
//            var mavenRepoUsername = project.findProperty("mavenRepo.username") as? String
//            var mavenRepoPassword = project.findProperty("mavenRepo.password") as? String
//
//            if (project.version.toString().endsWith("SNAPSHOT")) {
//                mavenRepoUsername = project.findProperty("mavenRepo.snapshots.username") as? String
//                    ?: mavenRepoUsername
//                mavenRepoPassword = project.findProperty("mavenRepo.snapshots.password") as? String
//                    ?: mavenRepoPassword
//            }
//            it.username = mavenRepoUsername
//            it.password = mavenRepoPassword
//        }
    }

}
