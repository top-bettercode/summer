package top.bettercode.logging

import org.junit.jupiter.api.Test
import top.bettercode.lang.util.StringUtil

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

    data class Data(
        var name: String? = "a",
        var age: Int? = 11,
        var data: String? = null
    )
}