package top.bettercode.summer.logging

import org.junit.jupiter.api.Test
import org.springframework.util.LinkedMultiValueMap
import top.bettercode.summer.tools.lang.operation.Parameters
import top.bettercode.summer.tools.lang.operation.QueryStringParser
import top.bettercode.summer.tools.lang.util.StringUtil
import java.net.URI

/**
 * @author Peter Wu
 */
class JsonPrettyPrinterTest {
    @Test
    fun print() {
        val unformattedJsonString = StringUtil.json(Data(data = StringUtil.json(Data())))
        System.err.println(unformattedJsonString)
        System.err.println("--------------------------------")
        System.err.println(StringUtil.prettyJson(unformattedJsonString))
    }

    @Test
    fun param1() {
        val parameters = LinkedMultiValueMap<String, String>()
        System.err.println(parameters["1"])
    }


    @Test
    fun param2() {
        val parameters = Parameters()
        System.err.println(parameters["1"])
    }


    @Test
    fun param() {
        val parameters =
            QueryStringParser.parse(URI("https://sapi.k780.com/?app=weather.realtime&cityIp=xx&ag=&appkey=xx&sign=xxx&format=json"))
        System.err.println(parameters)
    }

    data class Data(
        var name: String? = "a",
        var age: Int? = 11,
        var data: String? = null
    )
}