package top.bettercode.summer.tools.pay.test

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.decapitalized

/**
 *
 * @author Peter Wu
 */
class CodeGen {

    @Test
    fun gen() {
        //读取 UnifiedorderResponse.txt 内容
        val isRequest = false
        val unifiedorderResponse = javaClass.getResource("/UnifiedOrderResponse.txt")!!.readText()
        val lines = unifiedorderResponse.lines()
        lines.forEach {
            //每行内容示例：应用APPID 	appid 	是 	String(32) 	wx8888888888888888 	调用接口提交的应用ID
            //解析为         /**
            //         * 应用APPID,调用接口提交的应用ID
            //         */
            //        @field:JsonProperty("appid")
            //        val appid: String? = null,
            val split = it.split("\t")
            val name = split[1].trim()
            //name 下划线格式 转换为驼峰式
            val camelName = name.split("_").joinToString("") { s -> s.capitalized() }.decapitalized()
            val type = split[3].substringBeforeLast("(").trim()
            val comment = split[0].trim() + "，" + split[5].trim()
            val code = """
                /**
                * $comment
                */
                @field:JsonProperty("$name")
                val $camelName: $type${if (isRequest && split[2].trim() == "是") "" else "? = null"},
                """.trimIndent()
            System.err.println(code)
        }
    }

}