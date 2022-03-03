package top.bettercode.gradle.profile

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.language.jvm.tasks.ProcessResources
import top.bettercode.gradle.profile.ProfileExtension.Companion.profileFiles
import top.bettercode.gradle.profile.ProfileExtension.Companion.profileProperties
import top.bettercode.gradle.profile.ProfileExtension.Companion.profilesActive
import top.bettercode.gradle.profile.ProfileExtension.Companion.profilesActiveName
import java.util.*

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class ProfilePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(JavaPlugin::class.java)

        project.extensions.create("profile", ProfileExtension::class.java)
        project.extensions.configure(ProfileExtension::class.java) {
            it.extraVersion =
                (project.findProperty("profile.extra-version") as? String)?.toBoolean()
                    ?: false
            it.excludeOther =
                (project.findProperty("profile.exclude-other") as? String)?.toBoolean()
                    ?: true
            it.configDir = (project.findProperty("profile.conf-dir") as? String) ?: "conf"
            it.configFile = (project.findProperty("profile.config-file") as? String) ?: ""
            it.activeFileSuffix = (project.findProperty("profile.active-file-suffix") as? String)
                ?: ""
            it.beginToken = (project.findProperty("profile.begin-token") as? String) ?: "@"
            it.endToken = (project.findProperty("profile.end-token") as? String) ?: "@"
            it.matchFiles = ((project.findProperty("profile.match-files") as? String)
                ?: "**/*.yml,**/*.yaml,**/*.properties,**/*.xml,**/*.conf").split(",").toSet()
        }
        val props = project.profileProperties
        val hashtable = Hashtable<String, String>()
        props.forEach { t, u ->
            val k = t.toString()
            hashtable[k] = u.toString()
        }
        project.tasks.getByName("processTestResources") {
            it as ProcessResources
            doFilter(it, project, hashtable)
            it.mustRunAfter("clean")
        }

        project.tasks.getByName("processResources") {
            it.doFirst {
                println("$profilesActiveName:${project.profilesActive}")
            }
            it as ProcessResources
            doFilter(it, project, hashtable)
            it.mustRunAfter("clean")
        }
    }

    private fun doFilter(it: ProcessResources, project: Project, hash: Hashtable<String, String>) {
        it.inputs.property(profilesActiveName, project.profilesActive)
        it.inputs.files(*project.profileFiles)
        val profile = project.extensions.getByType(ProfileExtension::class.java)
        it.doFirst {
            if (profile.extraVersion)
                project.version =
                    (if ("unspecified" == project.version) project.rootProject.version else project.version).toString() + "." + project.profilesActive.toUpperCase()
        }

        it.filesMatching(profile.matchFiles) {
            it.filter(
                mapOf(
                    "tokens" to hash,
                    "beginToken" to profile.beginToken,
                    "endToken" to profile.endToken
                ), ReplaceTokens::class.java
            )
        }
        if (profile.excludeOther)
            it.filesMatching("application-*.yml") { f ->
                if (f.sourceName != "application-${project.profilesActive}.yml") {
                    f.exclude()
                }
            }
        it.doLast {
            profile.closure.forEach { it(project, profile) }
            profile.profileClosure.filter { project.profilesActive == it.key }.values.flatten()
                .forEach { it(project, profile) }
        }
    }

}