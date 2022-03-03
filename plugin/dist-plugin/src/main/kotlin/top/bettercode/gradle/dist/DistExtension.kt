package top.bettercode.gradle.dist

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import top.bettercode.gradle.profile.ProfileExtension.Companion.profilesActive


/**
 * @author Peter Wu
 */
open class DistExtension(
    var unwrapResources: Boolean = true,
    var autoStart: Boolean = true,
    var includeJre: Boolean = false,
    var x64: Boolean = true,
    var windows: Boolean = false,
    var urandom: Boolean = false,
    /**
     * 相对当前项目的路径
     */
    var nativePath: String = "native",
    /**
     * 运行用户
     */
    var runUser: String = "",
    var jreWindowsI586Gz: String = "",
    var jreWindowsX64Gz: String = "",
    var jreLinuxI586Gz: String = "",
    var jreLinuxX64Gz: String = "",
    /**
     * windows service 老版本路径 用于生成更新包
     */
    var windowsServiceOldPath: String = "",
    /**
     * dist 老版本路径 用于生成更新包
     */
    var distOldPath: String = "",
    var jvmArgs: List<String> = listOf(),
    var excludeUnWrapResources: List<String> =
        listOf(
            "META-INF/additional-spring-configuration-metadata.json",
            "META-INF/spring.factories"
        )
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
}
