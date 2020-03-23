package cn.bestwu.lang.util

import org.junit.Test

/**
 * @author Peter Wu
 */
class HtmlUtilTest {

    private val inputString = String(HtmlUtilTest::class.java.classLoader.getResourceAsStream("test.html").readBytes())

    @Test
    fun parseHtml() {
        System.err.println(HtmlUtil.parseHtml(inputString))
    }

    @Test
    fun parseHtmlWithoutBlank() {
        System.err.println(HtmlUtil.parseHtmlWithoutBlank(inputString))
    }
}