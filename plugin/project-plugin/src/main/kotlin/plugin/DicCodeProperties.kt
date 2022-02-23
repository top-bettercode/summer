package plugin

import org.apache.tools.ant.util.Native2AsciiUtils
import top.bettercode.generator.dsl.Generator
import java.io.File

/**
 * @author Peter Wu
 */
class DicCodeProperties(
    private val propertiesFile: File
) : Generator() {
    private val codeTypes: MutableMap<String, Int> = mutableMapOf()

    override fun call() {
        columns.forEach { col ->
            if (col.isCodeField) {
                val dicCodes = col.dicCodes(extension)!!
                val codeType = dicCodes.type
                val size = dicCodes.codes.size
                if (!codeTypes.contains(codeType) || (codeTypes[codeType] ?: 0) > size) {
                    codeTypes[codeType] = size
                    val escapeTypeName = escape(dicCodes.name)
                    propertiesFile.appendText("#----------------------------------------------------------\n")
                    propertiesFile.appendText("#$escapeTypeName\n")
                    propertiesFile.appendText("$codeType=$escapeTypeName\n")
                    propertiesFile.appendText("$codeType|TYPE=${if (dicCodes.isInt) "Int" else "String"}\n")
                    dicCodes.codes.forEach {
                        propertiesFile.appendText("$codeType.${it.key}=${escape(it.value)}\n")
                    }
                }
            }
        }
    }

    private fun escape(src: String): String {
        return Native2AsciiUtils.native2ascii(src)
    }
}


