package top.bettercode.summer.test.web.serializer

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil

/**
 *
 * @author Peter Wu
 */
class JsonTypeTest {


    @Test
    fun test() {
        val data1 = Data1(1, "2")
        val data2 = Data2("1", 2)
        val data1Str = "{\"a\":1,\"b\":\"2\"}"
        Assertions.assertEquals(data1Str, StringUtil.json(data1))
        val data2Str = "{\"a\":\"1\",\"b\":2}"
        Assertions.assertEquals(data2Str, StringUtil.json(data2))

        val data11 = StringUtil.readJson(data2Str, Data1::class.java)
        val data22 = StringUtil.readJson(data1Str, Data2::class.java)
        System.err.println(StringUtil.json(data11))
        Assertions.assertEquals(data1, data11)
        System.err.println(StringUtil.json(data22))
        Assertions.assertEquals(data2, data22)
    }

    data class Data1(val a: Long? = null, val b: String? = null)
    data class Data2(val a: String? = null, val b: Long? = null)
}