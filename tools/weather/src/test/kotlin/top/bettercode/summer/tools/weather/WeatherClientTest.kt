package top.bettercode.summer.tools.weather

import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.lang.util.StringUtil
import java.io.File

/**
 *
 * @author Peter Wu
 */
@Disabled
class WeatherClientTest : BaseTest() {

    @Autowired
    lateinit var weatherClient: WeatherClient

    @Test
    fun type() {
        val typeList = weatherClient.type()
        System.err.println(StringUtil.json(typeList, true))
    }

    @Test
    fun icons() {
        val typeFile = ClassPathResource("type.json").file
        val typeList = StringUtil.objectMapper().readTree(typeFile)
        val iconsFile = ClassPathResource("icons-list-zh.json").file
        val node = StringUtil.objectMapper().readTree(iconsFile)
        for (value in typeList) {
            value as JsonNode
            val wtNm = value.get("wtNm").asText()
            val icon = value.get("icon").asText()
            findIcon(node, wtNm, icon, false)
            findIcon(node, wtNm, icon, true)
        }
    }

    private fun findIcon(
            node: JsonNode,
            wtNm: String,
            icon: String?,
            night: Boolean
    ) {
        val wt = wtNm + (if (night) "夜" else "")
        var find = node.toList()
                .find { it.get("icon_name").asText() == wt }
        if (night && find == null)
            find = node.toList()
                    .find { it.get("icon_name").asText() == wtNm }
        Assertions.assertNotNull(find, "$wt:未找到")
        val iconCode = find!!.get("icon_code").asText()
        val source = File(ClassPathResource("icons-source").file, "$iconCode.svg")
        val source2 =
                File(ClassPathResource("icons-resources").file, "${if (night) "n" else "d"}/$icon.png")
        val target = File(ClassPathResource("").file, "icons/${if (night) "n" else "d"}/$icon.svg")
        val target2 =
                File(ClassPathResource("").file, "icons-zh/${if (night) "n" else "d"}/$wt.svg")
        val target2Source =
                File(ClassPathResource("").file, "icons-zh/${if (night) "n" else "d"}/$wt-s.png")
        if (!target.exists()) {
            if (!target.parentFile.exists()) {
                target.parentFile.mkdirs()
            }
            source.copyTo(target)
            val replace = target.readText()
                    .replace("fill=\"currentColor\"", "fill=\"white\"")
                    .replace("width=\"16\" height=\"16\"", "width=\"80\" height=\"80\"")
            target.writeText(replace)
        }
        if (!target2.exists()) {
            if (!target2.parentFile.exists()) {
                target2.parentFile.mkdirs()
            }
            source.copyTo(target2)
            val replace = target2.readText()
                    .replace("fill=\"currentColor\"", "fill=\"white\"")
                    .replace("width=\"16\" height=\"16\"", "width=\"80\" height=\"80\"")
            target2.writeText(replace)
        }
        if (source2.exists() && !target2Source.exists()) {
            if (!target2Source.parentFile.exists()) {
                target2Source.parentFile.mkdirs()
            }
            source2.copyTo(target2Source)
        }
    }


    @Test
    fun query1() {
        val result = weatherClient.query("182.148.121.104")
        System.err.println(result)
    }

    @Test
    fun query2() {
        val result = weatherClient.query(104.176376, 30.817039)
        System.err.println(result)
    }

}