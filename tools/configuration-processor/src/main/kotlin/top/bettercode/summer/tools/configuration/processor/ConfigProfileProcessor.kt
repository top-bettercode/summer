package top.bettercode.summer.tools.configuration.processor

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation
import kotlin.io.path.toPath

// 定义一个注解处理器类，实现Processor接口，并使用@SupportedAnnotationTypes注解指定要处理的注解类型
class ConfigProfileProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        if (!roundEnv.processingOver()) {
            return true
        }

        val file = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "temp").toUri().toPath().toFile()
        val configsDir = file.parentFile
        val userDir = findWalkDownTopConf(file, "src")!!.parentFile
        file.delete()
        //读取gradle.properties
        val gradleProperties = Properties()
        val gradlePropertiesFile = findWalkDownTopConf(userDir, "gradle.properties")
        if (gradlePropertiesFile != null) {
            gradleProperties.load(gradlePropertiesFile.inputStream())
        }
        //读取gradleUserHomeDir
        val gradleUserHomeDir = System.getProperty("GRADLE_USER_HOME")
                ?: (System.getProperty("user.home") + File.separator + ".gradle")
        val gradleUserHomePropertiesFile = File(gradleUserHomeDir, "gradle.properties")
        if (gradleUserHomePropertiesFile.exists()) {
            gradleProperties.load(gradleUserHomePropertiesFile.inputStream())
        }

        val confs = mutableMapOf<String, String>()
        val version = gradleProperties.getProperty("app.version") ?: "1.0"
        confs["summer.web.project-name"] = gradleProperties.getProperty("application.name") ?: "app"
        confs["summer.web.version"] = "v${version}"
        confs["summer.web.version-no"] = String.format("%-9s", version.split(".").joinToString("") { String.format("%03d", it.toInt()) }).replace(" ", "0").trimStart('0')


        confs.putAll(gradleProperties.map { it.key.toString() to it.value.toString() }.toMap())

        var confFile: File? = null
        val confDir = findWalkDownTopConf(userDir, "conf")
        if (confDir != null) {
            val profilesDefaultActive = confDir.listFiles()?.firstOrNull { it.nameWithoutExtension == "default" }

            val active = System.getProperty("profiles.active") ?: System.getProperty("P")
            ?: gradleProperties.getProperty("profiles.active") ?: "default"

            val filter = confDir.listFiles()?.filter { it.nameWithoutExtension.startsWith(active) }
            confFile = if (filter.isNullOrEmpty() || filter.size > 1) {
                System.err.println("未找到适合的profiles.active:${active}配置文件" + (if (profilesDefaultActive != null) ",使用${profilesDefaultActive}默认配置" else ""))
                profilesDefaultActive
            } else {
                filter[0]
            }

            //加载default配置
            if (profilesDefaultActive != null && profilesDefaultActive != confFile) {
                if (profilesDefaultActive.extension == "properties") {
                    val properties = Properties()
                    properties.load(profilesDefaultActive.inputStream())
                    confs.putAll(properties.map { it.key.toString() to it.value.toString() }.toMap())
                } else {
                    val yaml = Yaml()
                    val conf = yaml.load<Map<String, Any>>(profilesDefaultActive.inputStream())
                    //yaml 转 configs
                    yamlToConfs(conf, confs)
                }
            }
        }


        if (confFile != null) {
            //读取配置文件
            if (confFile.extension == "properties") {
                val properties = Properties()
                properties.load(confFile.inputStream())
                confs.putAll(properties.map { it.key.toString() to it.value.toString() }.toMap())
            } else {
                val yaml = Yaml()
                val conf = yaml.load<Map<String, Any>>(confFile.inputStream())
                //yaml 转 configs
                yamlToConfs(conf, confs)
            }
        }

        System.getProperties().forEach { t, u -> confs.put(t.toString(), u.toString()) }

        val packageName = confs["app.packageName"]
        if (packageName != null) {
            confs["app.packagePath"] = packageName.replace(".", "/")
        }

        replaceConfigs(configsDir, confs)

        return true
    }

    private fun debug(msg: String) {
        processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "log.txt").toUri().toPath().toFile().writeText(msg)
    }

    private fun yamlToConfs(conf: Map<String, Any>, configs: MutableMap<String, String>, prefix: String = "") {
        conf.forEach { (key, value) ->
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST") yamlToConfs(value as Map<String, Any>, configs, "$prefix$key.")
            } else {
                configs[prefix + key] = value.toString()
            }
        }
    }

    private fun replaceConfigs(dir: File, confs: Map<String, String>) {
        dir.parentFile.walkTopDown().filter { it.extension in arrayOf("yml", "yaml", "properties", "xml", "conf") }.forEach {
            //替换文件中的@key@配置为confs 中对应的 value
            it.writeText(it.readText().replace(Regex("""@(.+?)@""")) { matchResult ->
                confs[matchResult.groupValues[1]] ?: matchResult.value
            })
        }
    }

    private fun findWalkDownTopConf(dir: File, name: String): File? {
        val confFile = File(dir, name)
        return if (!confFile.exists()) {
            val parentFile = dir.parentFile
            if (parentFile != null) {
                findWalkDownTopConf(parentFile, name)
            } else {
                null
            }
        } else confFile
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(ConfigProfile::class.java.name, "org.springframework.context.annotation.Configuration")
    }
}