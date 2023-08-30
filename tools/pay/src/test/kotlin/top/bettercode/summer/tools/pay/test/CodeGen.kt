package top.bettercode.summer.tools.pay.test

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.decapitalized
import java.io.File

/**
 *
 * @author Peter Wu
 */
class CodeGen {

    @Test
    fun gen() {
        //读取 UnifiedorderResponse.txt 内容
        val isRequest = false
        val javaName = "RefundResponse"
        val unifiedorderResponse = javaClass.getResource("/$javaName.txt")!!.readText()
        val lines = unifiedorderResponse.lines()
        File("src/main/kotlin/top/bettercode/summer/tools/pay/support/weixin/entity/$javaName.kt").printWriter().use { out ->
            out.println("""import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
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
                out.println(code)
            }
            out.println(")")
        }
    }

}