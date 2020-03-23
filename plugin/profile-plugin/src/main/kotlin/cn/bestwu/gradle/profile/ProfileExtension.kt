package cn.bestwu.gradle.profile

import org.gradle.api.Project
import profilesActive
import profilesActiveName
import profilesDefaultActive
import projectMark
import simpleProfilesActiveName
import java.io.File
import java.util.*


internal fun Project.configProject(run: (project: Project) -> Unit) {
    run(rootProject)
    if (rootProject != this)
        run(this)
}

internal val Project.profileProperties: Properties
    get() {
        val props = Properties()
        val gradleProperties = rootProject.file("gradle.properties")
        if (gradleProperties.exists()) {
            props.load(gradleProperties.inputStream())
        }

        val profile = extensions.getByType(ProfileExtension::class.java)
        val activeFileName = "${profile.source}/$profilesActive${profile.activeFileSuffix}.properties"
        configProject { project ->
            val projectFile = project.file("${profile.source}/$projectMark.properties")
            if (projectFile.exists()) {
                props.load(projectFile.inputStream())
            }
            val activeFile = project.file(activeFileName)
            if (activeFile.exists()) {
                props.load(activeFile.inputStream())
            }
        }

        props.putAll(System.getProperties())
        props[profilesActiveName] = profilesActive
        configProject { project ->
            props.forEach { t, u ->
                val k = t as String
                if (project.hasProperty(k)) {
                    project.setProperty(k, u)
                }
            }
        }
        return props
    }

internal val Project.profileFiles: Array<File>
    get() {
        val profile = extensions.getByType(ProfileExtension::class.java)
        val array = mutableListOf<File>()
        array.add(rootProject.file("gradle.properties"))
        val activeFileName = "${profile.source}/$profilesActive${profile.activeFileSuffix}.properties"
        configProject { project ->
            val projectFile = project.file("${profile.source}/$projectMark.properties")
            if (projectFile.exists()) {
                array.add(projectFile)
            }
            val activeFile = project.file(activeFileName)
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
            val configFile = project.file(profile.source)
            if (configFile.exists()) {
                set.addAll(configFile.listFiles()?.filter { it.isFile }?.map { if (profile.activeFileSuffix.isNotBlank()) it.nameWithoutExtension.substringBeforeLast(profile.activeFileSuffix) else it.nameWithoutExtension }
                        ?: emptySet())
            }
        }
        set.add(profilesDefaultActive)
        set.remove(projectMark)
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
        var source: String = "config",
        var activeFileSuffix: String = "",
        var beginToken: String = "@",
        var endToken: String = "@",
        var extraVersion: Boolean = false,
        var excludeOther: Boolean = true,
        var closure: MutableSet<Project.(ProfileExtension) -> Unit> = mutableSetOf(),
        val profileClosure: MutableMap<String, MutableSet<Project.(ProfileExtension) -> Unit>> = mutableMapOf())