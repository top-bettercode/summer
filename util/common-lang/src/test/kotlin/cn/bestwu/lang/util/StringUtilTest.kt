package cn.bestwu.lang.util

import org.junit.Assert
import org.junit.Test

/**
 * @author Peter Wu
 */
class StringUtilTest {

    @Test
    fun valueOf() {
        Assert.assertEquals("null", StringUtil.valueOf(null))
    }
    @Test
    fun subString() {
        Assert.assertEquals("123", StringUtil.subString("1234",3))
        Assert.assertEquals("123", StringUtil.subStringWithEllipsis("1234",3))
        Assert.assertEquals("1...", StringUtil.subStringWithEllipsis("123477",4))
    }
}