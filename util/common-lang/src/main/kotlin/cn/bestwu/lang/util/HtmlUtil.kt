package cn.bestwu.lang.util

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist
import java.io.IOException

/**
 * HTML 工具类
 */
object HtmlUtil {

    /**
     * 截取纯文本内容
     *
     * @param inputString 输入HTML内容
     * @param length 截取长度
     * @return 纯文本内容
     */
    @JvmStatic
    fun subParseHtml(inputString: String?, length: Int): String? {
        if (inputString == null) {
            return null
        }
        val subHtml = parseHtml(inputString)
        return StringUtil.subString(subHtml, length)// 返回文本字符串
    }

    /**
     * 截取纯文本内容
     *
     * @param inputString 输入HTML内容
     * @param length 截取长度
     * @return 纯文本内容
     */
    @JvmStatic
    fun subParseHtmlWithoutBlank(inputString: String?, length: Int): String? {
        if (inputString == null) {
            return null
        }
        val subHtml = parseHtmlWithoutBlank(inputString)
        return StringUtil.subString(subHtml, length)// 返回文本字符串
    }

    /**
     * 截取纯文本内容
     *
     * @param inputString 输入HTML内容
     * @param length 截取长度
     * @return 纯文本内容
     */
    @JvmStatic
    fun subParseHtmlWithEllipsis(inputString: String?, length: Int): String? {
        if (inputString == null) {
            return null
        }
        val subHtml = parseHtmlWithoutBlank(inputString)
        return StringUtil.subStringWithEllipsis(subHtml, length)// 返回文本字符串
    }

    /**
     * @param inputString 输入HTML内容
     * @return 纯文本内容
     */
    @JvmStatic
    fun parseHtml(inputString: String?): String? {
        if (inputString == null) {
            return null
        }
        return try {
            // html过滤
            var content = Jsoup.clean(inputString, Whitelist().addTags("br", "p"))
            val elements = Jsoup.parse(content).body()
            elements.select("br").append("\\n")
            elements.select("p").append("\\n")
            val html = elements.html()
            val clean = Jsoup.clean(html, Whitelist.none())
            content = Parser.unescapeEntities(clean, false)
            val split = content.split("\\\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val contentBuilder = StringBuilder("")
            split.filterNot { org.jsoup.helper.StringUtil.isBlank(it.trim()) }
                    .forEach { contentBuilder.append("\t").append(it.trim()).append("\n") }

            contentBuilder.toString()
        } catch (e: IOException) {
            inputString
        }

    }

    /**
     * @param inputString 输入HTML内容
     * @return 去除空白内容的纯文本内容
     */
    @JvmStatic
    fun parseHtmlWithoutBlank(inputString: String?): String? {
        return parseHtml(inputString)?.replace(Regex("[\r\t\n ]"), "")
    }
}
