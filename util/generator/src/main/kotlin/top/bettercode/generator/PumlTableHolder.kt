package top.bettercode.generator

import top.bettercode.generator.puml.PumlConverter
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PumlTableHolder(module: String, files: List<File>) : FileTableHolder(module, files) {

    override fun getTables(file: File) = PumlConverter.toTables(file, module)
}