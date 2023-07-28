package top.bettercode.summer.tools.generator

import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.powerdesigner.PdmReader
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PdmTableHolder(ext: GeneratorExtension, module: String, files: List<File>) :
        FileTableHolder(ext, module, files) {

    override fun getTables(file: File, call: (Table) -> Unit) = PdmReader.read(file, call)
}