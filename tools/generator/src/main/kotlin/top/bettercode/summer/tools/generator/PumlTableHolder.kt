package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.puml.PumlConverter
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PumlTableHolder(database: DatabaseConfiguration,
                      files: List<File>,
                      securityFile: File = File("${System.getProperty("java.io.tmpdir")}/summer/security.puml")
) : FileTableHolder(database, if (database.isDefault) (files + listOf(securityFile)) else files) {

    init {
        PumlTableHolder::class.java.getResourceAsStream("/security.puml")?.copyTo(securityFile.apply { parentFile.mkdirs() }.outputStream())
    }

    override fun getTables(file: File, call: (Table) -> Unit) = PumlConverter.toTables(file, call)

}