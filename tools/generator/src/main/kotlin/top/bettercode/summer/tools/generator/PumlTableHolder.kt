package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.puml.PumlConverter
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PumlTableHolder(database: DatabaseConfiguration,
                      files: List<File>
) : FileTableHolder(database, files) {

    override fun getTables(file: File, call: (Table) -> Unit) = PumlConverter.toTables(file, call)

}