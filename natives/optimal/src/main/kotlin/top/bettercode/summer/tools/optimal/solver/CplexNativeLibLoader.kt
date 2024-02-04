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
            Os.isFamily(Os.FAMILY_MAC) -> arrayOf()
            Os.isFamily(Os.FAMILY_WINDOWS) -> arrayOf()
            else -> arrayOf("cplex", "cplexamp", "cpxchecklic", "libcplex2211.so")
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
            }

            Os.isFamily(Os.FAMILY_WINDOWS) -> {
            }

            else -> {
                System.load("${targetFolder.absolutePath}/libcplex2211.so")
            }
        }
        log.info("$LIB_NAME library is already loaded.")
    }

}
