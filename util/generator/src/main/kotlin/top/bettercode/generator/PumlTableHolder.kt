package top.bettercode.generator

import top.bettercode.generator.database.entity.Table
import top.bettercode.generator.puml.PumlConverter
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