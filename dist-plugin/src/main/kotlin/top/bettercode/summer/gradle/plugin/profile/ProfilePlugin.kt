package top.bettercode.summer.gradle.plugin.profile

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.language.jvm.tasks.ProcessResources
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.PROFILES_ACTIVE_NAME
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profileFiles
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profileProperties
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profilesActive
import java.util.*

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
@Suppress("ObjectLiteralToLambda")
class ProfilePlugin : Plugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun apply(project: Project) {
        project.plugins.apply(JavaPlugin::class.java)

        project.extensions.create("profile", ProfileExtension::class.java)
        project.extensions.configure(ProfileExtension::class.java) {
            it.extraVersion =
                    (project.findProperty("profile.extra-version") as String?)?.toBoolean()
                            ?: false
            it.excludeOther =
                    (project.findProperty("profile.exclude-other") as String?)?.toBoolean()
                            ?: true
            it.configDir = (project.findProperty("profile.conf-dir") as String?) ?: "conf"
            it.configFile = (project.findProperty("profile.config-file") as String?) ?: ""
            it.activeFileSuffix = (project.findProperty("profile.active-file-suffix") as String?)
                    ?: ""
            it.beginToken = (project.findProperty("profile.begin-token") as String?) ?: "@"
            it.endToken = (project.findProperty("profile.end-token") as String?) ?: "@"
            it.matchFiles = ((project.findProperty("profile.match-files") as String?)
                    ?: "**/*.yml,**/*.yaml,**/*.properties,**/*.xml,**/*.conf").split(",").toSet()
        }
        val props = project.profileProperties
        val hashtable = Hashtable<String, String>()
        props.forEach { t, u ->
            val k = t.toString()
            hashtable[k] = u.toString()
        }
        project.tasks.getByName(JavaPlugin.PROCESS_TEST_RESOURCES_TASK_NAME) {
            it as ProcessResources
            doFilter(it, project, hashtable)
            it.mustRunAfter("clean")
        }

        project.tasks.getByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME) {
            it.doFirst(object : Action<Task> {
                override fun execute(it: Task) {
                    project.logger.lifecycle("$PROFILES_ACTIVE_NAME:${project.profilesActive}")
                }
            })
            it as ProcessResources
            doFilter(it, project, hashtable)
            it.mustRunAfter("clean")
        }
    }

    private fun doFilter(
            @Suppress("UnstableApiUsage") it: ProcessResources,
            project: Project,
            hash: Hashtable<String, String>
    ) {
        it.inputs.property(PROFILES_ACTIVE_NAME, project.profilesActive)
        it.inputs.files(*project.profileFiles)
        val profile = project.extensions.getByType(ProfileExtension::class.java)
        it.doFirst(object : Action<Task> {
            override fun execute(it: Task) {
                if (profile.extraVersion)
                    project.version =
                            (if ("unspecified" == project.version) project.rootProject.version else project.version).toString() + "." + project.profilesActive.uppercase(
                                    Locale.getDefault()
                            )
            }
        })

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
        it.doLast(object : Action<Task> {
            override fun execute(it: Task) {
                profile.closure.forEach { it(project, profile) }
                profile.profileClosure.filter { project.profilesActive == it.key }.values.flatten()
                        .forEach { it(project, profile) }
            }
        })
    }

}