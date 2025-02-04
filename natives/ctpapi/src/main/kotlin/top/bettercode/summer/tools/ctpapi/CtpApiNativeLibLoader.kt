package top.bettercode.summer.tools.ctpapi

import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.FileUtil
import top.bettercode.summer.tools.lang.util.Os
import java.io.File
import java.nio.file.Files

/**
 *
 * https://github.com/nicai0609/JAVA-CTPAPI
 *
 *
 *
 * native library loader.
 */
object CtpApiNativeLibLoader {
    private val log = LoggerFactory.getLogger(CtpApiNativeLibLoader::class.java)
    private const val LIB_NAME = "CTP"
    private val version = CtpApiNativeLibLoader::class.java.`package`.implementationVersion

    /**
     * Load native library in the user.dir folder.
     */
    @Synchronized
    @JvmStatic
    fun loadNativeLib() {
        val tmpPath = FileUtil.userDir + File.separator + "build" + File.separator + "native"
        val targetFolder = File(tmpPath)

        if (!targetFolder.exists()) {
            targetFolder.mkdirs()
        }
        val libraryNames = when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> arrayOf(
                "win64/thostmduserapi_se.dll",
                "win64/thosttraderapi_se.dll",
                "win64/thostapi_wrap.dll"
            )

            else -> arrayOf(
                "linux64/libthostmduserapi_se.so",
                "linux64/libthosttraderapi_se.so",
                "linux64/libthostapi_wrap.so"
            )
        }

        for (libraryName in libraryNames) {
            val targetFile = File(targetFolder, libraryName.substringAfter("/")).absoluteFile
            if (targetFile.exists()) {
                targetFile.delete()
            }
            log.info("copy $libraryName to $targetFile")
            Files.copy(
                CtpApiNativeLibLoader::class.java.getResourceAsStream("/native/$libraryName")!!,
                targetFile.toPath()
            )
        }
        when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                System.load("${targetFolder.absolutePath}/thostmduserapi_se.dll")
                System.load("${targetFolder.absolutePath}/thosttraderapi_se.dll")
                System.load("${targetFolder.absolutePath}/thostapi_wrap.dll")
            }

            else -> {
                System.load("${targetFolder.absolutePath}/libthostmduserapi_se.so")
                System.load("${targetFolder.absolutePath}/libthosttraderapi_se.so")
                System.load("${targetFolder.absolutePath}/libthostapi_wrap.so")
            }
        }
        log.info("$LIB_NAME $version library is already loaded.")
    }

}
