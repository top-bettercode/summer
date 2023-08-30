package top.bettercode.summer.tools.pay.test

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.decapitalized
import java.io.File
import java.io.StringWriter

/**
 *
 * @author Peter Wu
 */
class CodeGen {

    @Test
    fun gen() {
        //读取 UnifiedorderResponse.txt 内容
        val isRequest = false
        val javaName = "OrderQueryResponse.txt".substringBeforeLast(".")
        val unifiedorderResponse = javaClass.getResource("/$javaName.txt")!!.readText()
        val lines = unifiedorderResponse.lines()
        val printWriter = File("src/main/kotlin/top/bettercode/summer/tools/pay/support/weixin/entity/$javaName.kt").printWriter()
        val stringWriter = StringWriter()
        stringWriter.use { out ->
            out.appendLine("""import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class $javaName(
""")
            lines.forEach {
                val split = it.split("\t")
                val name = split[1].trim()
                //name 下划线格式 转换为驼峰式
                val camelName = name.split("_").joinToString("") { s -> s.capitalized() }.decapitalized()
                val type = split[3].substringBeforeLast("(").trim().capitalized()
                val comment = split[0].trim() + "，" + split[5].trim()
                val code = """
                /**
                * $comment
                */
                @field:JsonProperty("$name")
                var $camelName: $type${if (isRequest && split[2].trim() == "是") "" else "? = null"},
                """.trimIndent()
                out.appendLine(code)
            }
            out.appendLine(")")
        }
        printWriter.use { out ->
            out.append(stringWriter.toString())
        }
    }

}