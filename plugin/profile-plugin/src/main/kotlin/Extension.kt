import cn.bestwu.gradle.profile.ProfileExtension
import cn.bestwu.gradle.profile.configProject
import cn.bestwu.gradle.profile.findActive
import org.gradle.api.Project
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*

const val profilesDefaultActive: String = "local"
const val simpleProfilesActiveName: String = "P"
const val defaultConfigName: String = "default"
const val profilesActiveName: String = "profiles.active"

val Project.profileProperties: Properties
    get() {
        val props = Properties()
        val gradleProperties = rootProject.file("gradle.properties")
        if (gradleProperties.exists()) {
            props.load(gradleProperties.inputStream())
            props.keys.forEach { t ->
                props[t] = rootProject.properties[t]
            }
        }

        val profile = extensions.getByType(ProfileExtension::class.java)
        configProject { project ->
            val defaultConfigYmlFile = project.file("${profile.configDir}/$defaultConfigName.yml")
            if (defaultConfigYmlFile.exists()) {
                props.putAll(parseYml(Yaml().loadAs(defaultConfigYmlFile.inputStream(), Map::class.java)))
            }
            val activeYmlFile = project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.yml")
            if (activeYmlFile.exists()) {
                props.putAll(parseYml(Yaml().loadAs(activeYmlFile.inputStream(), Map::class.java)))
            }
            val defaultConfigYamlFile = project.file("${profile.configDir}/$defaultConfigName.yaml")
            if (defaultConfigYamlFile.exists()) {
                props.putAll(parseYml(Yaml().loadAs(defaultConfigYamlFile.inputStream(), Map::class.java)))
            }
            val activeYamlFile = project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.yaml")
            if (activeYamlFile.exists()) {
                props.putAll(parseYml(Yaml().loadAs(activeYamlFile.inputStream(), Map::class.java)))
            }
            val defaultConfigFile = project.file("${profile.configDir}/$defaultConfigName.properties")
            if (defaultConfigFile.exists()) {
                props.load(defaultConfigFile.inputStream())
            }
            val activeFile = project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.properties")
            if (activeFile.exists()) {
                props.load(activeFile.inputStream())
            }
        }
        if (profile.configFile.isNotBlank()) {
            val uri = uri(profile.configFile)
            if (uri.scheme.isNullOrEmpty()) {
                val configFile = File(uri)
                if (configFile.exists())
                    props.load(configFile.inputStream())
            } else {
                props.load(uri.toURL().openStream())
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

private fun parseYml(map: Map<*, *>, result: MutableMap<Any, Any> = mutableMapOf(), prefix: String = ""): MutableMap<Any, Any> {
    map.forEach { (k, u) ->
        if (u != null) {
            if (u is Map<*, *>) {
                parseYml(u, result, "$prefix$k.")
            } else {
                result["$prefix$k"] = u
            }
        }
    }
    return result
}

val Project.profilesActive: String
    get() {
        val systemProperties = System.getProperties()
        val findActive = findActive { name ->
            systemProperties.getProperty(name)
        } ?: findActive { name ->
            rootProject.findProperty(name) as? String
        }
        return if (findActive.isNullOrBlank()) {
            profilesDefaultActive
        } else
            findActive
    }

fun Project.profileClosure(closure: Project.(ProfileExtension) -> Unit) {
    val profile = project.extensions.getByType(ProfileExtension::class.java)
    profile.closure.add(closure)
}

fun Project.profileClosure(active: String, closure: Project.(ProfileExtension) -> Unit) {
    val profile = project.extensions.getByType(ProfileExtension::class.java)
    profile.profileClosure.computeIfAbsent(active) { mutableSetOf() }.add(closure)
}
