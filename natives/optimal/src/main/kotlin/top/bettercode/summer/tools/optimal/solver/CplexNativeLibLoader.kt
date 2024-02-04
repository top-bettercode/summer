package top.bettercode.summer.tools.optimal.solver

import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.Os
import java.io.File
import java.nio.file.Files

/**
 * native library loader.
 */
object CplexNativeLibLoader {
    private val log = LoggerFactory.getLogger(CplexNativeLibLoader::class.java)
    private const val LIB_NAME = "Cplex"

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
            Os.isFamily(Os.FAMILY_MAC) -> if (Os.OS_ARCH.contains("arm64"))
                arrayOf("arm64/libcplex2211.dylib") else arrayOf("libcplex2211.dylib")

            Os.isFamily(Os.FAMILY_WINDOWS) -> arrayOf("cplex2211.dll")
            else -> arrayOf("libcplex2211.so")
        }

        for (libraryName in libraryNames) {
            val targetFile = File(targetFolder, libraryName.substringAfter("/")).absoluteFile
            if (targetFile.exists()) {
                targetFile.delete()
            }
            log.info("copy $libraryName to $targetFile")
            Files.copy(CplexNativeLibLoader::class.java.getResourceAsStream("/native/cplex/$libraryName")!!,
                    targetFile.toPath())
        }
        when {
            Os.isFamily(Os.FAMILY_MAC) -> {
                System.load("${targetFolder.absolutePath}/libcplex2211.dylib")
            }

            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                System.load("${targetFolder.absolutePath}/cplex2211.dll")
            }

            else -> {
                System.load("${targetFolder.absolutePath}/libcplex2211.so")
            }
        }
        log.info("$LIB_NAME library is already loaded.")
    }

}
