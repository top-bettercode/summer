package top.bettercode.summer.tools.lang.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil.objectMapper
import top.bettercode.summer.tools.lang.util.StringUtil.toUnderscore
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * @author Peter Wu
 */
class StringUtilTest {

    @Test
    fun stripTrailingZeros() {
        val stripTrailingZeros = BigDecimal("123.000").stripTrailingZeros()
        System.err.println(stripTrailingZeros)
        Assertions.assertEquals("123", stripTrailingZeros.toString())
    }

    @Test
    fun jsonNode() {
        val objectMapper = objectMapper()
        val json = objectMapper.createObjectNode()
        json.put("name", "peter")
        json.put("age", 18)
        System.err.println(json.toString())
        Assertions.assertEquals("{\"name\":\"peter\",\"age\":18}", json.toString())
    }

    @Test
    fun valueOf() {
        Assertions.assertEquals("null", StringUtil.valueOf(null))
    }

    @Test
    fun subString() {
        Assertions.assertEquals("123", StringUtil.subString("1234", 3))
        Assertions.assertEquals("123...", StringUtil.subStringWithEllipsis("1234", 3))
        Assertions.assertEquals(
            "1234...", StringUtil.subStringWithEllipsis("123477", 4)
        )
    }

    @Test
    fun json() {
        val mapOf = mapOf("a" to "中文", "b" to "123")
        Assertions.assertEquals(
            """{"a":"\u4E2D\u6587","b":"123"}""",
            StringUtil.json(`object` = mapOf, format = false, escapeNonAscii = true)
        )
        Assertions.assertEquals(
            """{"a":"\u4E2D\u6587","b":"123"}""",
            StringUtil.json(`object` = mapOf, format = false, escapeNonAscii = true)
        )
        Assertions.assertEquals(
            """{"a":"中文","b":"123"}""",
            StringUtil.json(`object` = mapOf, format = false, escapeNonAscii = false)
        )
        Assertions.assertEquals(
            """{"a":"中文","b":"123"}""",
            StringUtil.json(`object` = mapOf, format = false, escapeNonAscii = false)
        )
    }

    @Test
    fun jsonDate() {
        System.err.println(StringUtil.json(Date()))
        val localDateJson = StringUtil.json(LocalDate.now())
        System.err.println(localDateJson)
        val localDateTimeJson = StringUtil.json(LocalDateTime.now())
        System.err.println(localDateTimeJson)

        val localDate = StringUtil.readJson(localDateJson, LocalDate::class.java)
        System.err.println(localDate)
        val localDateTime = StringUtil.readJson(localDateTimeJson, LocalDateTime::class.java)
        System.err.println(localDateTime)
    }

    @Test
    fun jsonDateData() {
        val dateData = DateData(Date(), LocalDate.now(), LocalDateTime.now())
        val json = StringUtil.json(dateData)
        System.err.println(json)
        val dateData1 = StringUtil.readJson(json, DateData::class.java)
        System.err.println(StringUtil.json(dateData1))
    }

    @Test
    fun toUnderscore() {
        System.err.println("javaName".toUnderscore())
        System.err.println("JAVA_NAME".toUnderscore())
    }

}