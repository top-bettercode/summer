package top.bettercode.lang.util

import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
class StringUtilTest {

    @Test
    fun valueOf() {
        org.junit.jupiter.api.Assertions.assertEquals("null", StringUtil.valueOf(null))
    }

    @Test
    fun subString() {
        org.junit.jupiter.api.Assertions.assertEquals("123", StringUtil.subString("1234", 3))
        org.junit.jupiter.api.Assertions.assertEquals(
            "123",
            StringUtil.subStringWithEllipsis("1234", 3)
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            "1...",
            StringUtil.subStringWithEllipsis("123477", 4)
        )
    }
}