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
        var envDir: String = "env",
        var envFile: String = "",
        var activeFileSuffix: String = "",
        var beginToken: String = "@",
        var endToken: String = "@",
        var extraVersion: Boolean = false,
        var excludeOther: Boolean = true,
        var closure: MutableSet<Project.(ProfileExtension) -> Unit> = mutableSetOf(),
        val profileClosure: MutableMap<String, MutableSet<Project.(ProfileExtension) -> Unit>> = mutableMapOf()
) {

    val confDir: String = "conf"

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(ProfileExtension::class.java)

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
                    addActiveFile(project, profile, array, ".yml")
                    addActiveFile(project, profile, array, ".yaml")
                    addActiveFile(project, profile, array, ".properties")
                }

                return array.toTypedArray()
            }

        private fun Project.addActiveFile(project: Project, profile: ProfileExtension, array: MutableList<File>, suffix: String = "") {
            val defaultEnvFile = activeFile(project, profile, PROFILES_DEFAULT_ACTIVE, suffix)
            if (defaultEnvFile.exists()) {
                array.add(defaultEnvFile)
            }
            if (profilesActive != PROFILES_DEFAULT_ACTIVE) {
                val activeFile = activeFile(project, profile, profilesActive, suffix)
                if (activeFile.exists()) {
                    if (profilesActive.contains("test")) {
                        val testFile = activeFile(project, profile, "test", suffix)
                        if (testFile.exists()) {
                            array.add(testFile)
                        }
                    } else if (profilesActive.contains("release")) {
                        val releaseFile = activeFile(project, profile, "release", suffix)
                        if (releaseFile.exists()) {
                            array.add(releaseFile)
                        }
                    }
                    array.add(activeFile)
                }
            }
        }

        private fun activeFile(project: Project, profile: ProfileExtension, active: String, suffix: String): File {
            var file = project.file("${profile.envDir}/$active${profile.activeFileSuffix}$suffix")
            if (!file.exists()) {
                file = project.file("${profile.confDir}/$active${profile.activeFileSuffix}$suffix")
            }
            return file
        }


        internal val Project.profiles: Set<String>
            get() {
                val profile = extensions.getByType(ProfileExtension::class.java)
                val set = mutableSetOf<String>()
                configProject { project ->
                    var envFile = project.file(profile.envDir)
                    if (!envFile.exists()) {
                        envFile = project.file(profile.confDir)
                    }
                    if (envFile.exists()) {
                        set.addAll(envFile.listFiles()?.filter { it.isFile }?.map {
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
            var active = run(SIMPLE_PROFILES_ACTIVE_NAME)
            return if (active.isNullOrBlank()) {
                active = run(PROFILES_ACTIVE_NAME)
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
                var filter = profiles.filter { it.startsWith(active) }
                if (filter.isEmpty()) {
                    filter = profiles.filter { it.contains(active) }
                }
                if (filter.isEmpty() || filter.size > 1) {
                    log.warn("未找到适合的profiles.active:${active}配置文件,使用${PROFILES_DEFAULT_ACTIVE}默认配置")
                    PROFILES_DEFAULT_ACTIVE
                } else {
                    filter[0]
                }
            } else {
                find
            }
        }


        const val PROFILES_DEFAULT_ACTIVE: String = "default"
        const val SIMPLE_PROFILES_ACTIVE_NAME: String = "P"
        const val PROFILES_ACTIVE_NAME: String = "profiles.active"

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
                    val yaml = Yaml()
                    loadActiveFile(project, profile, yaml, props, ".yml")
                    loadActiveFile(project, profile, yaml, props, ".yaml")
                    loadActiveFile(project, profile, yaml, props, ".properties")
                }
                if (profile.envFile.isNotBlank()) {
                    val uri = uri(profile.envFile)
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

                props[PROFILES_ACTIVE_NAME] = profilesActive
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

        private fun Project.loadActiveFile(project: Project, profile: ProfileExtension, yaml: Yaml, props: Properties, suffix: String = "") {
            val defaultEnvFile = activeFile(project, profile, PROFILES_DEFAULT_ACTIVE, suffix)
            if (defaultEnvFile.exists()) {
                loadProps(suffix, props, defaultEnvFile, yaml)
            }
            if (profilesActive != PROFILES_DEFAULT_ACTIVE) {
                val activeFile = activeFile(project, profile, profilesActive, suffix)
                if (activeFile.exists()) {
                    if (profilesActive.contains("test")) {
                        val testFile = activeFile(project, profile, "test", suffix)
                        if (testFile.exists()) {
                            loadProps(suffix, props, testFile, yaml)
                        }
                    } else if (profilesActive.contains("release")) {
                        val releaseFile = activeFile(project, profile, "release", suffix)
                        if (releaseFile.exists()) {
                            loadProps(suffix, props, releaseFile, yaml)
                        }
                    }
                    loadProps(suffix, props, activeFile, yaml)
                }
            }
        }

        private fun loadProps(suffix: String, props: Properties, releaseFile: File, yaml: Yaml) {
            if (suffix == ".properties") {
                props.load(releaseFile.inputStream())
            } else {
                loadYml(releaseFile, yaml, props)
            }
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
                    rootProject.findProperty(name) as String?
                }
                return if (findActive.isNullOrBlank()) {
                    PROFILES_DEFAULT_ACTIVE
                } else
                    findActive
            }

    }
}