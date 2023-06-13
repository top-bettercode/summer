package top.bettercode.summer.tools.sap.config

import com.sap.conn.jco.rt.JCoRuntimeFactory
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import top.bettercode.summer.tools.lang.util.OS
import java.io.File
import java.nio.file.Files

/**
 * native library extractor and loader.
 */
object SapNativeLibLoader {
    private val log = LoggerFactory.getLogger(SapNativeLibLoader::class.java)
    private const val LIB_NAME = "Jco"

    /**
     * Extract and load native library in the provided folder.
     *
     * @throws Exception The provisioning failure exception.
     */
    @Synchronized
    fun loadNativeLib() {
        val targetFolder = File(
                System.getProperty("user.dir") + File.separator + "build" + File.separator + "native")
        if (!targetFolder.exists()) {
            targetFolder.mkdirs()
        }
        val libraryName: String
        val currentOs = OS.CURRENT_OS
        libraryName = when (currentOs) {
            OS.MAC -> "libsapjco3.jnilib"
            OS.WINDOWS -> "sapjco3.dll"
            else -> "libsapjco3.so"
        }
        val targetPath = File(targetFolder, libraryName).absoluteFile
        if (!targetPath.exists()) {
            Files.copy(ClassPathResource("/native/$libraryName").inputStream,
                    targetPath.toPath())
        }
        val libraryPath = targetFolder.absolutePath
        val nativeSystemProperty = "java.library.path"
        var systemNativePath = System.getProperty(nativeSystemProperty)
        val pathSeparator: String = if (OS.WINDOWS.isCurrentOs) {
            ";"
        } else {
            ":"
        }
        if (!systemNativePath.contains(pathSeparator + libraryPath)
                && !systemNativePath.startsWith(libraryPath + pathSeparator)) {
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
