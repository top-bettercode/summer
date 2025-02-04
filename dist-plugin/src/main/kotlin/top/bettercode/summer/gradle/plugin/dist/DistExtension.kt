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
            //https://jdk.java.net/17/
            //https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
            //https://download.oracle.com/java/17/archive/jdk-17.0.11_windows-x64_bin.zip
            //https://download.oracle.com/java/17/archive/jdk-17.0.11_linux-x64_bin.tar.gz
            return field.ifBlank {
                if (windows)
                    "https://download.java.net/openjdk/jdk17/ri/openjdk-17+35_windows-x64_bin.zip"
                else
                    "https://download.java.net/openjdk/jdk17/ri/openjdk-17+35_linux-x64_bin.tar.gz"
            }
        }

    val jdkArchive: File
        get() {
            val userHome = System.getProperty("user.home")
            val file = File(userHome + File.separator + ".cache/jdk", File(jdkArchiveSrc).name)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            return file
        }

    val isX64: Boolean get() = jdkArchiveSrc.contains("x64")

    private val isWindows: Boolean = Os.isFamily(Os.FAMILY_WINDOWS)

    fun nativePath(project: Project): String {
        var libArgs = project.file("build${File.separator}native").absolutePath

        if (project.file(nativePath).exists()) {
            val pathSeparator: String = if (isWindows) {
                ";"
            } else {
                ":"
            }
            libArgs += pathSeparator + project.file(nativePath).absolutePath
        }

        return libArgs
    }

    fun nativeLibArgs(project: Project): String {
        return "-Djava.library.path=${nativePath(project)}"
    }

    fun startScriptNativeLibArgs(project: Project, windows: Boolean): String {
        var libArgs =
            "-Djava.library.path=${if (windows) "%APP_HOME%\\build\\native" else "\$APP_HOME/build/native"}"

        if (project.file(nativePath).exists()) {
            libArgs += if (windows)
                ";%APP_HOME%\\native"
            else
                ":\$APP_HOME/native"
        }

        return libArgs
    }

    fun linuxLDLibraryPath(project: Project): String {
        var ldLibraryPath = "\\\$APP_HOME/build/native"

        if (project.file(nativePath).exists()) {
            val pathSeparator = ":"

            ldLibraryPath += "$pathSeparator\\\$APP_HOME/native"
        }
        return ldLibraryPath
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
        jvmArgs += nativeLibArgs(project)
        return jvmArgs
    }

    companion object {
        fun Project.findDistProperty(key: String) =
            (findProperty("dist.${name}.$key") as String?
                ?: findProperty("dist.$key") as String?)


    }
}
