package top.bettercode.summer.gradle.plugin.dist

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.profile.ProfileExtension.Companion.profilesActive
import java.io.File


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
         * 是否打包jdk
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

    var jdkArchiveSrc: String = ""
        get() {
            return field.ifBlank {
                if (windows)
                    "https://download.java.net/openjdk/jdk17/ri/openjdk-17+35_windows-x64_bin.zip"
                else
                    "https://download.java.net/openjdk/jdk17/ri/openjdk-17+35_linux-x64_bin.tar.gz"
            }
        }

    val jdkArchive: File
        get() {
            val tmpPath = System.getProperty("user.home")
            val file = File(tmpPath + File.separator + ".cache/jdk", File(jdkArchiveSrc).name)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            return file
        }

    internal val isX64: Boolean get() = jdkArchiveSrc.contains("x64")

    fun nativeLibArgs(project: Project): String {
        return "-Djava.library.path=${project.file(nativePath).absolutePath}"
    }

    fun jvmArgs(project: Project): Set<String> {
        val jvmArgs = jvmArgs.toMutableSet()
        val encoding = "-Dfile.encoding=UTF-8"
        jvmArgs += encoding
        if (Os.isFamily(Os.FAMILY_UNIX) && urandom) {
            jvmArgs += "-Djava.security.egd=file:/dev/urandom"
        }
        if (project.extensions.findByName("profile") != null) {
            jvmArgs += "-Dspring.profiles.active=${project.profilesActive}"
        }
        if (project.file(nativePath).exists()) {
            jvmArgs += nativeLibArgs(project)
        }
        return jvmArgs
    }

    companion object {
        fun Project.findDistProperty(key: String) =
                (findProperty("dist.${name}.$key") as? String ?: findProperty("dist.$key") as? String)


    }
}
