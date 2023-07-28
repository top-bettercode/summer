package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.puml.PumlConverter
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PumlTableHolder(
        ext: GeneratorExtension,
        module: String,
        files: List<File>
) : FileTableHolder(ext, module, files) {

    override fun getTables(file: File, call: (Table) -> Unit) = PumlConverter.toTables(file, call)

}