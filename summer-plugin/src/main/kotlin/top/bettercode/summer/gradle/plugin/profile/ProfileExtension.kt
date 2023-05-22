package top.bettercode.summer.gradle.plugin.profile

import org.gradle.api.Project
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*

/**
 * @author Peter Wu
 */
open class ProfileExtension(
        var matchFiles: Set<String> = setOf(
                "**/*.yml",
                "**/*.yaml",
                "**/*.properties",
                "**/*.xml",
                "**/*.conf"
        ),
        var configDir: String = "conf",
        var configFile: String = "",
        var activeFileSuffix: String = "",
        var beginToken: String = "@",
        var endToken: String = "@",
        var extraVersion: Boolean = false,
        var excludeOther: Boolean = true,
        var closure: MutableSet<Project.(ProfileExtension) -> Unit> = mutableSetOf(),
        val profileClosure: MutableMap<String, MutableSet<Project.(ProfileExtension) -> Unit>> = mutableMapOf()
) {

    companion object {

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
                    val defaultConfigYmlFile =
                            project.file("${profile.configDir}/$profilesDefaultActive.yml")
                    if (defaultConfigYmlFile.exists()) {
                        array.add(defaultConfigYmlFile)
                    }
                    if (profilesActive != profilesDefaultActive) {
                        val activeYmlFile =
                                project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.yml")
                        if (activeYmlFile.exists()) {
                            array.add(activeYmlFile)
                        }
                    }
                    val defaultConfigYamlFile =
                            project.file("${profile.configDir}/$profilesDefaultActive.yaml")
                    if (defaultConfigYamlFile.exists()) {
                        array.add(defaultConfigYamlFile)
                    }
                    if (profilesActive != profilesDefaultActive) {
                        val activeYamlFile =
                                project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.yaml")
                        if (activeYamlFile.exists()) {
                            array.add(activeYamlFile)
                        }
                    }
                    val defaultConfigFile =
                            project.file("${profile.configDir}/$profilesDefaultActive.properties")
                    if (defaultConfigFile.exists()) {
                        array.add(defaultConfigFile)
                    }
                    if (profilesActive != profilesDefaultActive) {
                        val activeFile =
                                project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.properties")
                        if (activeFile.exists()) {
                            array.add(activeFile)
                        }
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
                        set.addAll(configFile.listFiles()?.filter { it.isFile }?.map {
                            if (profile.activeFileSuffix.isNotBlank()) it.nameWithoutExtension.substringBeforeLast(
                                    profile.activeFileSuffix
                            ) else it.nameWithoutExtension
                        }
                                ?: emptySet())
                    }
                }
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
                    println("未找到适合的profiles.active:${active}配置文件,使用${profilesDefaultActive}默认配置")
                    profilesDefaultActive
                } else {
                    filter[0]
                }
            } else {
                find
            }
        }


        const val profilesDefaultActive: String = "default"
        const val simpleProfilesActiveName: String = "P"
        const val profilesActiveName: String = "profiles.active"

        val Project.profileProperties: Properties
            get() {
                val props = Properties()
                props["summer.web.project-name"] = name
                props["summer.web.version"] = "v${version}"
                props["summer.web.version-no"] = String.format(
                        "%-9s",
                        version.toString().split(".")
                                .joinToString("") { String.format("%03d", it.toInt()) }
                ).replace(" ", "0").trimStart('0')
                val gradleProperties = rootProject.file("gradle.properties")
                if (gradleProperties.exists()) {
                    props.load(gradleProperties.inputStream())
                }
                val gradleuserHomeProperties = File(gradle.gradleUserHomeDir, "gradle.properties")
                if (gradleuserHomeProperties.exists()) {
                    props.load(gradleuserHomeProperties.inputStream())
                }

                props.keys.forEach { t ->
                    val k = t as String
                    if (rootProject.hasProperty(k))
                        props[k] = rootProject.properties[k]
                }

                val profile = extensions.getByType(ProfileExtension::class.java)
                configProject { project ->
                    val defaultConfigYmlFile =
                            project.file("${profile.configDir}/$profilesDefaultActive.yml")
                    val yaml = Yaml()
                    if (defaultConfigYmlFile.exists()) {
                        loadYml(defaultConfigYmlFile, yaml, props)
                    }
                    if (profilesActive != profilesDefaultActive) {
                        val activeYmlFile =
                                project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.yml")
                        if (activeYmlFile.exists()) {
                            loadYml(activeYmlFile, yaml, props)
                        }
                    }
                    val defaultConfigYamlFile =
                            project.file("${profile.configDir}/$profilesDefaultActive.yaml")
                    if (defaultConfigYamlFile.exists()) {
                        loadYml(defaultConfigYamlFile, yaml, props)
                    }
                    if (profilesActive != profilesDefaultActive) {
                        val activeYamlFile =
                                project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.yaml")
                        if (activeYamlFile.exists()) {
                            loadYml(activeYamlFile, yaml, props)
                        }
                    }
                    val defaultConfigFile =
                            project.file("${profile.configDir}/$profilesDefaultActive.properties")
                    if (defaultConfigFile.exists()) {
                        props.load(defaultConfigFile.inputStream())
                    }

                    if (profilesActive != profilesDefaultActive) {
                        val activeFile =
                                project.file("${profile.configDir}/$profilesActive${profile.activeFileSuffix}.properties")
                        if (activeFile.exists()) {
                            props.load(activeFile.inputStream())
                        }
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

                val packageName = props["app.packageName"]?.toString()
                if (packageName != null) {
                    props["app.packagePath"] = packageName.replace(".", "/")
                }

                props[profilesActiveName] = profilesActive
                configProject { project ->
                    props.forEach { t, u ->
                        val k = t as String
                        if (project.hasProperty(k)) {
                            project.setProperty(k, u?.toString())
                        }
                    }
                }
                return props
            }

        private fun loadYml(
                defaultConfigYmlFile: File,
                yaml: Yaml,
                props: Properties
        ) {
            val readText = defaultConfigYmlFile.readText()
            if (readText.isNotBlank()) {
                val yml = parseYml(
                        yaml.loadAs(
                                readText,
                                Map::class.java
                        )
                )
                props.putAll(yml)
            }
        }

        private fun parseYml(
                map: Map<*, *>,
                result: MutableMap<Any, Any> = mutableMapOf(),
                prefix: String = ""
        ): MutableMap<Any, Any> {
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

    }
}