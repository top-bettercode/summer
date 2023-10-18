package top.bettercode.summer.tools.optimal

import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import top.bettercode.summer.tools.lang.util.Os
import java.io.File
import java.nio.file.Files

/**
 * native library extractor and loader.
 */
object OptimalNativeLibLoader {
    private val log = LoggerFactory.getLogger(OptimalNativeLibLoader::class.java)
    private const val LIB_NAME = "Optimal"

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
        val libraryNames: Array<String> = when {
            Os.isFamily(Os.FAMILY_MAC) -> arrayOf("libcopt_cpp.dylib", "libcoptjniwrap.dylib")
            Os.isFamily(Os.FAMILY_WINDOWS) -> arrayOf("copt_cpp.dll", "coptjniwrap.dll")
            else -> arrayOf("libcopt_cpp.so", "libcoptjniwrap.so")
        }
        libraryNames.forEach {
            val targetFile = File(targetFolder, it).absoluteFile
            if (!targetFile.exists()) {
                val file = if (Os.isArch("arm64") || Os.isArch("aarch64")) "arm64/$it" else it
                Files.copy(ClassPathResource("/native/$file").inputStream,
                        targetFile.toPath())
            }
        }

        log.info(LIB_NAME + " system native path: " + System.getProperty("java.library.path"))
    }


}
