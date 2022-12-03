package top.bettercode.summer.tools.lang.util

import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
class HtmlUtilTest {

    private val inputString =
        HtmlUtilTest::class.java.classLoader.getResourceAsStream("test.html")
            ?.let { String(it.readBytes()) }

    @Test
    fun parseHtml() {
        System.err.println(HtmlUtil.parseHtml(inputString))
    }

    @Test
    fun parseHtmlWithoutBlank() {
        System.err.println(HtmlUtil.parseHtmlWithoutBlank(inputString))
    }
}