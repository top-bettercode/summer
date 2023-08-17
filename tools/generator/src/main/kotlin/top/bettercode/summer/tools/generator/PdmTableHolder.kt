package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.powerdesigner.PdmReader
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PdmTableHolder(database: DatabaseConfiguration,
                     files: List<File>) :
        FileTableHolder(database, files) {

    override fun getTables(file: File, call: (Table) -> Unit) = PdmReader.read(file, call)
}