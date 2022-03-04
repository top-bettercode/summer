package top.bettercode.generator

import top.bettercode.generator.powerdesigner.PdmReader
import java.io.File

/**
 *
 * @author Peter Wu
 */
class PdmTableHolder(module: String, files: List<File>) : FileTableHolder(module, files) {

    override fun getTables(file: File) = PdmReader.read(file, module)
}