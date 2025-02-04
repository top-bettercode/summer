package top.bettercode.summer.tools.optimal.copt

import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.FileUtil
import top.bettercode.summer.tools.lang.util.Os
import java.io.File
import java.nio.file.Files
import java.util.*

/**
 * native library loader.
 */
object COPTNativeLibLoader {
    private val log = LoggerFactory.getLogger(COPTNativeLibLoader::class.java)
    private const val LIB_NAME = "COPT"
    private val bundle = ResourceBundle.getBundle("copt_version")

    /**
     * Load native library in the user.dir folder.
     */
    @Synchronized
    fun loadNativeLib() {
        val version = bundle.getString("version")
        val tmpDir = FileUtil.userDir + File.separator + "build" + File.separator + "native"
        val targetFolder = File(tmpDir)
        if (!targetFolder.exists()) {
            targetFolder.mkdirs()
        }
        val libraryNames = when {
            Os.isFamily(Os.FAMILY_MAC) -> arrayOf(
                "macos_universal2/libcopt_cpp.dylib",
                "macos_universal2/libcoptjniwrap.dylib"
            )

            Os.isFamily(Os.FAMILY_WINDOWS) -> arrayOf("win64/copt_cpp.dll", "win64/coptjniwrap.dll")
            //判断是否是 arm64 架构
            Os.OS_ARCH.contains("arm64") -> arrayOf(
                "armlinux64/libcopt_cpp.so",
                "armlinux64/libcoptjniwrap.so"
            )

            else -> arrayOf("linux64/libcopt_cpp.so", "linux64/libcoptjniwrap.so")
        }

        for (libraryName in libraryNames) {
            val targetFile = File(targetFolder, libraryName.substringAfter("/")).absoluteFile
            if (targetFile.exists()) {
                targetFile.delete()
            }
            log.info("copy $libraryName to $targetFile")
            Files.copy(
                COPTNativeLibLoader::class.java.getResourceAsStream("/native/$libraryName")!!,
                targetFile.toPath()
            )
        }
        when {
            Os.isFamily(Os.FAMILY_MAC) -> {
                System.load("${targetFolder.absolutePath}/libcopt_cpp.dylib")
                System.load("${targetFolder.absolutePath}/libcoptjniwrap.dylib")
            }

            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                System.load("${targetFolder.absolutePath}/copt_cpp.dll")
                System.load("${targetFolder.absolutePath}/coptjniwrap.dll")
            }

            else -> {
                System.load("${targetFolder.absolutePath}/libcopt_cpp.so")
                System.load("${targetFolder.absolutePath}/libcoptjniwrap.so")
            }
        }
        log.info("OPTIMAL $LIB_NAME $version library is already loaded.")
    }

}
