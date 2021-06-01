package cn.bestwu.gradle.profile

import org.gradle.api.Project
import profilesActive
import profilesActiveName
import profilesDefaultActive
import defaultConfigName
import simpleProfilesActiveName
import java.io.File

internal fun Project.configProject(run: (project: Project) -> Unit) {
    run(rootProject)
    if (rootProject != this)
        run(this)
}

internal val Project.profileFiles: Array<File>
    get() {
        val profile = extensions.getByType(ProfileExtension::class.java)
        val array = mutableListOf<File>()
        array.add(File(gradle.gradleUserHomeDir, "gradle.properties"))
        array.add(rootProject.file("gradle.properties"))
        configProject { project ->
            val defaultConfigYmlFile = project.file("${profile.configDir}/$defaultConfigName.yml")
            if (defaultConfigYmlFile.exists()) {
                array.add(defaultConfigYmlFile)
            }
            val activeYmlFile = project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.yml")
            if (activeYmlFile.exists()) {
                array.add(activeYmlFile)
            }
            val defaultConfigYamlFile = project.file("${profile.configDir}/$defaultConfigName.yaml")
            if (defaultConfigYamlFile.exists()) {
                array.add(defaultConfigYamlFile)
            }
            val activeYamlFile = project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.yaml")
            if (activeYamlFile.exists()) {
                array.add(activeYamlFile)
            }
            val defaultConfigFile = project.file("${profile.configDir}/$defaultConfigName.properties")
            if (defaultConfigFile.exists()) {
                array.add(defaultConfigFile)
            }
            val activeFile = project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.properties")
            if (activeFile.exists()) {
                array.add(activeFile)
            }
        }

        return array.toTypedArray()
    }

internal val Project.profiles: Set<String>
    get() {
        val profile = extensions.getByType(ProfileExtension::class.java)
        val set = mutableSetOf<String>()
        configProject { project ->
            val configFile = project.file(profile.configDir)
            if (configFile.exists()) {
                set.addAll(configFile.listFiles()?.filter { it.isFile }?.map { if (profile.activeFileSuffix.isNotBlank()) it.nameWithoutExtension.substringBeforeLast(profile.activeFileSuffix) else it.nameWithoutExtension }
                        ?: emptySet())
            }
        }
        set.add(profilesDefaultActive)
        set.remove(defaultConfigName)
        return set
    }


internal fun Project.findActive(run: (String) -> String?): String? {
    val profiles = this.profiles
    var active = run(simpleProfilesActiveName)
    return if (active.isNullOrBlank()) {
        active = run(profilesActiveName)
        return if (active.isNullOrBlank()) {
            null
        } else
            findActive(profiles, active)
    } else {
        findActive(profiles, active)
    }
}

private fun findActive(profiles: Set<String>, active: String): String {
    val find = profiles.find { it == active }
    return if (find == null) {
        val filter = profiles.filter { it.startsWith(active) }
        if (filter.isEmpty() || filter.size > 1) {
            throw IllegalArgumentException("未找到适合的profiles.active:${active}配置文件")
        } else {
            filter[0]
        }
    } else {
        find
    }
}


/**
 * @author Peter Wu
 */
open class ProfileExtension(
        var matchFiles: Set<String> = setOf("**/*.yml", "**/*.yaml", "**/*.properties", "**/*.xml", "**/*.conf"),
        var configDir: String = "conf",
        var configFile: String = "",
        var activeFileSuffix: String = "",
        var beginToken: String = "@",
        var endToken: String = "@",
        var extraVersion: Boolean = false,
        var excludeOther: Boolean = true,
        var closure: MutableSet<Project.(ProfileExtension) -> Unit> = mutableSetOf(),
        val profileClosure: MutableMap<String, MutableSet<Project.(ProfileExtension) -> Unit>> = mutableMapOf())