package top.bettercode.summer.tools.optimal.cplex

import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.Os
import java.io.File
import java.nio.file.Files
import java.util.*

/**
 * native library loader.
 */
object CplexNativeLibLoader {
    private val log = LoggerFactory.getLogger(CplexNativeLibLoader::class.java)
    private const val LIB_NAME = "Cplex"
    private val bundle = ResourceBundle.getBundle("cplex_version")

    /**
     * Load native library in the user.dir folder.
     */
    @Synchronized
    fun loadNativeLib() {
        val version = bundle.getString("version")
        val tmpPath = System.getProperty("java.io.tmpdir")
        val targetFolder = File("$tmpPath${File.separator}summer-cplex-$version")
        if (!targetFolder.exists()) {
            targetFolder.mkdirs()
        }
        val libraryNames = when {
            Os.isFamily(Os.FAMILY_MAC) -> if (Os.OS_ARCH.contains("arm64"))
                arrayOf("macos_arm64/libcplex2211.dylib")
            else
                arrayOf("macos_universal/libcplex2211.dylib")

            Os.isFamily(Os.FAMILY_WINDOWS) -> arrayOf("win64/cplex2211.dll")
            else -> arrayOf("linux64/libcplex2211.so")
        }

        for (libraryName in libraryNames) {
            val targetFile = File(targetFolder, libraryName.substringAfter("/")).absoluteFile
            if (targetFile.exists()) {
                targetFile.delete()
            }
            log.info("copy $libraryName to $targetFile")
            Files.copy(CplexNativeLibLoader::class.java.getResourceAsStream("/native/$libraryName")!!,
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
        log.info("OPTIMAL $LIB_NAME $version library is already loaded.")
    }

}
