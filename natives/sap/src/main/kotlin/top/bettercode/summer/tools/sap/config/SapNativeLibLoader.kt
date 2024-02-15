package top.bettercode.summer.tools.sap.config

import com.sap.conn.jco.rt.JCoRuntimeFactory
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.Os
import java.io.File
import java.nio.file.Files

/**
 * native library loader.
 */
object SapNativeLibLoader {
    private val log = LoggerFactory.getLogger(SapNativeLibLoader::class.java)
    private const val LIB_NAME = "Jco"

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
            Os.isFamily(Os.FAMILY_MAC) -> arrayOf("macos_universal/libsapjco3.jnilib")
            Os.isFamily(Os.FAMILY_WINDOWS) -> arrayOf("win64/sapjco3.dll")
            else -> arrayOf("linux64/libsapjco3.so")
        }
        for (libraryName in libraryNames) {
            val targetFile = File(targetFolder, libraryName.substringAfter("/")).absoluteFile
            if(targetFile.exists()){
                targetFile.delete()
            }
            log.info("copy $libraryName to $targetFile")
            Files.copy(SapNativeLibLoader::class.java.getResourceAsStream("/native/$libraryName")!!,
                    targetFile.toPath())
        }

        val libraryPath = targetFolder.absolutePath
        val nativeSystemProperty = "java.library.path"
        var systemNativePath = System.getProperty(nativeSystemProperty)
        val pathSeparator: String = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            ";"
        } else {
            ":"
        }
        if (!systemNativePath.contains(pathSeparator + libraryPath)
                && !systemNativePath.contains(libraryPath + pathSeparator)) {
            systemNativePath += pathSeparator + libraryPath
            System.setProperty(nativeSystemProperty, systemNativePath)
        }
        log.info(LIB_NAME + " system native path: " + System.getProperty(nativeSystemProperty))
        if (isAlreadyLoaded) {
            log.info("$LIB_NAME library is already loaded.")
        }
    }

    private val isAlreadyLoaded: Boolean
        get() = try {
            val runtime = JCoRuntimeFactory.getRuntime()
            runtime != null
        } catch (e: Exception) {
            log.error("Failed to load $LIB_NAME library", e)
            false
        }
}
