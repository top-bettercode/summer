package top.bettercode.summer.gradle.plugin.dist

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profilesActive


/**
 * @author Peter Wu
 */
open class DistExtension(
    var unwrapResources: Boolean = true,
    var autoStart: Boolean = true,
    var urandom: Boolean = false,
    /**
     * 运行用户
     */
    var runUser: String = "",
    var jvmArgs: List<String> = listOf(),
    var excludeUnWrapResources: List<String> = emptyList(),
    /**
     * 相对当前项目的路径
     */
    var nativePath: String = "native",
    var jdkArchive: String = "",
    /**
     * 老版本路径 用于生成更新包
     */
    var prevArchive: String = ""
) {
    companion object {

        val Project.jvmArgs: Set<String>
            get() {
                val jvmArgs =
                    (findDistProperty("jvm-args") ?: "").split(" +".toRegex())
                        .filter { it.isNotBlank() }
                        .toMutableSet()
                val encoding = "-Dfile.encoding=UTF-8"
                jvmArgs += encoding
                if (Os.isFamily(Os.FAMILY_UNIX) && (findDistProperty("urandom")
                        ?: "false").toBoolean()
                ) {
                    jvmArgs += "-Djava.security.egd=file:/dev/urandom"
                }
                if (project.extensions.findByName("profile") != null) {
                    jvmArgs += "-Dspring.profiles.active=${project.profilesActive}"
                }
                if (project.file(nativePath).exists()) {
                    jvmArgs += nativeLibArgs
                }
                return jvmArgs
            }


        fun Project.findDistProperty(key: String) =
            (findProperty("dist.${name}.$key") as? String ?: findProperty("dist.$key") as? String)

        private val Project.nativePath: String
            get() = findDistProperty("native-path") ?: "native"

        internal val Project.nativeLibArgs: String
            get() {
                return "-Djava.library.path=${this.file(nativePath).absolutePath}"
            }

    }

    internal fun includeJdk(project: Project): Boolean {
        if (jdkArchive.isNotBlank()) {
            val file = project.file(jdkArchive)
            if (file.exists())
                return true
            else
                System.err.println("${project.path} jdk archive:$file 不存在 ")
        }
        return false
    }


    internal val isX64: Boolean get() = jdkArchive.contains("x64")
}
