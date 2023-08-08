package top.bettercode.summer.tools.lang.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
class StringUtilTest {

    @Test
    fun valueOf() {
        Assertions.assertEquals("null", StringUtil.valueOf(null))
    }

    @Test
    fun subString() {
        Assertions.assertEquals("123", StringUtil.subString("1234", 3))
        Assertions.assertEquals("123...", StringUtil.subStringWithEllipsis("1234", 3))
        Assertions.assertEquals("1234...", StringUtil.subStringWithEllipsis("123477", 4)
        )
    }

    @Test
    fun json() {
        val mapOf = mapOf("a" to "中文", "b" to "123")
        Assertions.assertEquals("""{
  "a" : "\u4E2D\u6587",
  "b" : "123"
}""", StringUtil.json(`object` = mapOf, format = true, escapeNonAscii = true))
        Assertions.assertEquals("""{"a":"\u4E2D\u6587","b":"123"}""", StringUtil.json(`object` = mapOf, format = false, escapeNonAscii = true))
        Assertions.assertEquals("""{"a":"中文","b":"123"}""", StringUtil.json(`object` = mapOf, format = false, escapeNonAscii = false))
        Assertions.assertEquals("""{
  "a" : "中文",
  "b" : "123"
}""", StringUtil.json(`object` = mapOf, format = true, escapeNonAscii = false))
    }

}