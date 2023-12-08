package top.bettercode.summer.tools.optimal.solver

import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.Os
import java.io.File
import java.nio.file.Files

/**
 * native library loader.
 */
object COPTNativeLibLoader {
    private val log = LoggerFactory.getLogger(COPTNativeLibLoader::class.java)
    private const val LIB_NAME = "COPT"

    /**
     * Load native library in the user.dir folder.
     */
    @Synchronized
    fun loadNativeLib() {
        val targetFolder = File(
                System.getProperty("user.dir") + File.separator + "build" + File.separator + "native")
        if (!targetFolder.exists()) {
            targetFolder.mkdirs()
        }
        val libraryNames = when {
            Os.isFamily(Os.FAMILY_MAC) -> arrayOf("libcopt_cpp.dylib", "libcoptjniwrap.dylib")
            Os.isFamily(Os.FAMILY_WINDOWS) -> arrayOf("copt_cpp.dll", "coptjniwrap.dll")
            //判断是否是 arm64 架构
            Os.OS_ARCH.contains("arm64") -> arrayOf("arm64/libcopt_cpp.so", "arm64/libcoptjniwrap.so")
            else -> arrayOf("libcopt_cpp.so", "libcoptjniwrap.so")
        }

        for (libraryName in libraryNames) {
            val targetPath = File(targetFolder, libraryName.substringAfter("/")).absoluteFile
            if (!targetPath.exists()) {
                log.info("copy $libraryName to $targetPath")
                Files.copy(COPTNativeLibLoader::class.java.getResourceAsStream("/native/$libraryName")!!,
                        targetPath.toPath())
            }
        }
        when {
            Os.isFamily(Os.FAMILY_MAC) -> System.load("${targetFolder.absolutePath}/libcoptjniwrap.dylib")
            Os.isFamily(Os.FAMILY_WINDOWS) -> System.load("${targetFolder.absolutePath}/coptjniwrap.dll")
            else -> System.load("${targetFolder.absolutePath}/libcoptjniwrap.so")
        }
        log.info("$LIB_NAME library is already loaded.")
    }

}
