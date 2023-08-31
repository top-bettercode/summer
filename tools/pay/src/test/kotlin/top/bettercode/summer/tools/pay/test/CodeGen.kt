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
        val javaName = "TransferInfoResponse.txt".substringBeforeLast(".")
        gen(javaName)
    }

    @Test
    fun all() {
        File("/data/repositories/bettercode/default/summer/tools/pay/src/test/resources").list()?.forEach {
            if (it.endsWith(".txt")) {
                gen(it.substringBeforeLast("."))
            }
        }
    }

    private fun gen(javaName: String) {
        val unifiedorderResponse = javaClass.getResource("/weixin/$javaName.txt")!!.readText()
        val rawLines = unifiedorderResponse.lines()
        val isRequest = javaName.endsWith("Request")

        val names = mutableListOf<String>()
        val codes = mutableListOf<String>()
        val imports = mutableListOf<String>()
        val otherCodes = mutableListOf<String>()
        rawLines.filter { it.isNotBlank() }.forEach {
            val split = it.split("[\t ]+".toRegex())
            try {
                val s0 = split[0]
                val s1 = split[1]
                val s2 = split[2]
                val s3 = split[3]
                val s4 = split[4]
                val name = s1.trim()
                names.add(name)
                val isOther = name.contains("$")
                //name 下划线格式 转换为驼峰式
                val camelName = name.split("_").joinToString("") { s -> s.capitalized() }.decapitalized()
                val isType = s3.substringBefore("(").equals("String", true) || s3.equals("int", true)
                val typeStr = if (isType) s3 else s4
                val exampleStr = if (isType) s4 else s3
                val type = typeStr.substringBeforeLast("(").trim().capitalized()
                val comment = "${s0.trim()}；${if (s2 == "是") "必填" else "非必填"}；${split.subList(5, split.size).joinToString(" ") { s -> s.trim() }.trim().trim('，', '。')}；示例：${exampleStr.trim()}"
                val code = """        /**
         * $comment
         */
        ${if (isOther) "//" else ""}@field:JsonProperty("$name")
        ${if (isOther) "//" else ""}var $camelName: $type? = ${
                    if (isRequest && name == "nonce_str") {
                        imports.add("import top.bettercode.summer.tools.lang.util.RandomUtil")
                        "RandomUtil.nextString2(32)"
                    } else "null"
                },"""
                if (isOther) {
                    otherCodes.add(code)
                } else {
                    codes.add(code)
                }
            } catch (e: Exception) {
                System.err.println(it)
                System.err.println(split)
                throw e
            }
        }
        val printWriter = File("src/main/kotlin/top/bettercode/summer/tools/pay/weixin/entity/$javaName.kt").printWriter()
        printWriter.use { out ->
            if (otherCodes.isNotEmpty()) {
                imports.add("import com.fasterxml.jackson.annotation.JsonAnyGetter")
                imports.add("import com.fasterxml.jackson.annotation.JsonAnySetter")
            }
            if (!isRequest && javaName.endsWith("Response")) {
                imports.add("import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse")
            }
            out.appendLine("""package top.bettercode.summer.tools.pay.weixin.entity

${imports.joinToString("\n")}
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class $javaName(
""")
            codes.forEach { value ->
                out.appendLine(value)
            }
            if (otherCodes.isNotEmpty()) {
                out.appendLine("        /**\n" +
                        "         * 其他\n" +
                        "         */\n" +
                        "        @field:JsonAnyGetter\n" +
                        "        @field:JsonAnySetter\n" +
                        "        var other: Map<String, Any?>? = null")
                otherCodes.forEach { value ->
                    out.appendLine(value)
                }
            }
            if (!isRequest && javaName.endsWith("Response")) {
                out.append(") : WeixinPayResponse()")
                out.appendLine(""" {

    override fun isBizOk(): Boolean {
        return ${
                    if (names.contains("result_code")) {
                        "\"SUCCESS\" == resultCode${if (names.contains("trade_state")) " && \"SUCCESS\" == tradeState" else ""}"
                    } else "true"
                }
    }
}""")
            } else {
                out.appendLine(")")
            }
        }
    }

}