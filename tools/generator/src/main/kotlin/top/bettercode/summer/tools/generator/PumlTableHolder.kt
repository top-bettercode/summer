package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.puml.PumlConverter
import top.bettercode.summer.tools.lang.util.FileUtil
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PumlTableHolder(
    database: DatabaseConfiguration,
    files: List<File>,
    securityFile: File = File(FileUtil.tmpDir, "puml${File.separator}security.puml")
) : FileTableHolder(database, if (database.storeToken) (files + listOf(securityFile)) else files) {

    init {
        PumlTableHolder::class.java.getResourceAsStream("/security.puml")
            ?.copyTo(securityFile.apply { parentFile.mkdirs() }.outputStream())
    }

    override fun getTables(file: File) =
        PumlConverter.toTables(database, file)

}