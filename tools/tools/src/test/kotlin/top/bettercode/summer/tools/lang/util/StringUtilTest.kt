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
}