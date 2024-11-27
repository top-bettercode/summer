package top.bettercode.summer.tools.feishu

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.lang.util.StringUtil.toCamelCase
import java.io.File

/**
 *
 * @author Peter Wu
 */
class CodeGen {

    //    @Disabled
    @Test
    fun gen() {
        val content = javaClass.getResource("/doc.txt")!!.readText().trimIndent()
        val classType = "${javaClass.`package`.name}.entity.employee.Employee"
//        val response = classType.contains("Response")
        val response = true

        val imports = mutableSetOf<String>()
        imports.add("import com.fasterxml.jackson.annotation.JsonProperty")
        val file = File("src/main/kotlin/${classType.replace(".", "/")}.kt")
        file.parentFile.mkdirs()
        val printWriter =
            file.printWriter()
        printWriter.use { out ->

            out.appendLine(
                """package ${classType.substringBeforeLast(".")}

${imports.sorted().joinToString("\n")}

/**
 * @author Peter Wu
 */
data class ${JavaType(classType).shortName}("""
            )

            content.split("\n").map { it.trim().trim('|') }.forEach { l ->
                l.split("|").let {
                    val name = it[0].trim().trimStart('+')
                    val javaType = when (val type = it[1].trim()) {
                        "string" -> "String"
                        "string[]" -> "Array<Int>"
                        "int" -> "Int"
                        "integer" -> "Int"
                        "integer[]" -> "Array<Int>"
                        "number" -> "Int"
                        "number[]" -> "Array<Int>"
                        "boolean" -> "Boolean"
                        "object" -> "Any"
                        "object[]" -> "List<Any>"
                        else -> throw IllegalArgumentException("不支持的类型：$type")
                    }

                    val required = if(response) it[2].trim() else false
                    val comment = it[if(response) 2 else 3].trim()
                    out.appendLine(
                        """    /**
     * $comment ${if (required == "是") "必填" else ""}
     */
    @JsonProperty("$name")
    var ${name.toCamelCase()}: ${javaType}? = null,"""
                    )
                }
            }
            out.append(")")

        }
    }

}