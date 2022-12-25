package top.bettercode.summer.gradle.plugin.dist

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profilesActive
import java.io.File
import java.net.URL


/**
 * @author Peter Wu
 */
open class DistExtension(
    var windows: Boolean = false,
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
     * 是否打包JDK
     */
    var includeJdk: Boolean = false,
    /**
     * 相对当前项目的路径
     */
    var nativePath: String = "native",
    /**
     * 老版本路径 用于生成更新包
     */
    var prevArchiveSrc: String = ""
) {

    private val jdkFileName: String
        get() = if (windows)
            "openjdk-8u42-b03-windows-i586-14_jul_2022.zip"
        else
            "openjdk-8u42-b03-linux-x64-14_jul_2022.tar.gz"

    var jdkArchiveSrc: String = ""
        get() {
            return field.ifBlank {
                "https://download.java.net/openjdk/jdk8u42/ri/$jdkFileName"
            }
        }
    val jdkArchive: File
        get() {
            val tmpPath = System.getProperty("java.io.tmpdir")
            return File(tmpPath, File(jdkArchiveSrc).name)
        }

    internal val isX64: Boolean get() = jdkArchiveSrc.contains("x64")

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
