package plugin

import cn.bestwu.generator.dsl.Generator
import org.apache.tools.ant.util.Native2AsciiUtils
import java.io.File

/**
 * @author Peter Wu
 */
class DicCodeProperties(
    private val propertiesFile: File,
    private val codeTypes: MutableSet<String>
) : Generator() {


    override fun doCall() {
        columns.forEach { col ->
            if (col.isCodeField) {
                val dicCodes = col.dicCodes(extension)!!
                val codeType = dicCodes.type
                if (!codeTypes.contains(codeType)) {
                    codeTypes.add(codeType)

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


