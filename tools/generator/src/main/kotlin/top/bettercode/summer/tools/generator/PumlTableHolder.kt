package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.puml.PumlConverter
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PumlTableHolder(
    database: DatabaseConfiguration,
    files: List<File>,
    securityFile: File = File("${System.getProperty("java.io.tmpdir")}${File.separator}summer${File.separator}security.puml")
) : FileTableHolder(database, if (database.storeToken) (files + listOf(securityFile)) else files) {

    init {
        PumlTableHolder::class.java.getResourceAsStream("/security.puml")
            ?.copyTo(securityFile.apply { parentFile.mkdirs() }.outputStream())
    }

    override fun getTables(file: File) =
        PumlConverter.toTables(database, file)

}