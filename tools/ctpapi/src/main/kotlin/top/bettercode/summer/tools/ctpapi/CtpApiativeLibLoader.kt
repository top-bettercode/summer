package top.bettercode.summer.tools.ctpapi

import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.Os
import java.io.File
import java.nio.file.Files

/**
 * native library loader.
 */
object CtpApiativeLibLoader {
    private val log = LoggerFactory.getLogger(CtpApiativeLibLoader::class.java)
    private const val LIB_NAME = "CTP"

    /**
     * Load native library in the user.dir folder.
     */
    @Synchronized
    @JvmStatic
    fun loadNativeLib() {
        val targetFolder = File(
                System.getProperty("user.dir") + File.separator + "build" + File.separator + "native")
        if (!targetFolder.exists()) {
            targetFolder.mkdirs()
        }
        val libraryNames = when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> arrayOf("thostmduserapi_se.dll", "thosttraderapi_se.dll", "thostapi_wrap.dll")
            else -> arrayOf("libthostmduserapi_se.so", "libthosttraderapi_se.so", "libthostapi_wrap.so")
        }

        for (libraryName in libraryNames) {
            val targetPath = File(targetFolder, libraryName).absoluteFile
            log.info("copy $libraryName to $targetPath")
            Files.copy(CtpApiativeLibLoader::class.java.getResourceAsStream("/native/$libraryName")!!,
                    targetPath.toPath())
        }
        when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> System.load("${targetFolder.absolutePath}/thostapi_wrap.dll")
            else -> System.load("${targetFolder.absolutePath}/libthostapi_wrap.so")
        }
        log.info("$LIB_NAME library is already loaded.")
    }

}
