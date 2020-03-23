package cn.bestwu.lang.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.*
import java.nio.charset.Charset
import java.util.*
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

/**
 * 字符串工具类
 *
 * @author Peter Wu
 */
object StringUtil {

    @JvmStatic
    var OBJECT_MAPPER = ObjectMapper()
    @JvmStatic
    var INDENT_OUTPUT_OBJECT_MAPPER = ObjectMapper()

    init {
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        INDENT_OUTPUT_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        INDENT_OUTPUT_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT)
    }

    /**
     * @param str 字符
     * @return 是否有长度
     */
    @JvmStatic
    fun hasLength(str: CharSequence?): Boolean {
        return !str.isNullOrEmpty()
    }

    /**
     * @param str 字符
     * @return 是否有字符
     */
    @JvmStatic
    fun hasText(str: CharSequence?): Boolean {
        return !str.isNullOrBlank()
    }

    /**
     * @param s 字符串
     * @return 转换为带下划线的小写字符
     */
    @JvmStatic
    fun addUnderscores(s: String): String {
        val buf = StringBuilder(s.replace('.', '_'))
        var i = 1
        while (i < buf.length - 1) {
            if (Character.isLowerCase(buf[i - 1]) &&
                    Character.isUpperCase(buf[i]) &&
                    Character.isLowerCase(buf[i + 1])) {
                buf.insert(i++, '_')
            }
            i++
        }
        return buf.toString().toLowerCase(Locale.ROOT)
    }

    /**
     * 转换为字符串
     *
     * @param object 对象
     * @param format 是否格式化输出
     * @return 字符串
     */
    @JvmOverloads
    @JvmStatic
    fun valueOf(`object`: Any?, format: Boolean = false): String {
        if (`object` is CharSequence) {
            return `object`.toString()
        }
        return if (format) {
            INDENT_OUTPUT_OBJECT_MAPPER.writeValueAsString(`object`)
        } else {
            OBJECT_MAPPER.writeValueAsString(`object`)
        }
    }

    /**
     * 截取一定长度的字符
     *
     * @param str 字符串
     * @param length 长度
     * @return 截取后的字符串
     */
    @JvmStatic
    fun subString(str: String?, length: Int): String? {
        if (str == null) {
            return null
        }
        val l = str.length
        return if (l > length) {
            str.substring(0, length)
        } else {
            str
        }
    }

    /**
     * 截取一定长度的字符，结果以...结尾
     *
     * @param str 字符串
     * @param length 长度
     * @return 截取后的字符串
     */
    @JvmStatic
    fun subStringWithEllipsis(str: String?, length: Int): String? {
        if (str == null) {
            return null
        }
        val l = str.length
        return if (length in 4..(l - 1)) {
            "${str.substring(0, length - 3)}..."
        } else {
            subString(str, length)
        }
    }

    /**
     * 计算字符串包含子字符串的个数
     *
     * @param str 字符串
     * @param sub 子字符串
     * @return 个数
     */
    @JvmStatic
    fun countSubString(str: String, sub: String): Int {
        return if (str.contains(sub)) {
            splitWorker(str, sub, -1, false)!!.size - 1
        } else {
            0
        }
    }

    /**
     * 分割字符串
     *
     * @param str 字符串
     * @param separatorChars 分隔符
     * @param max 最大数量
     * @param preserveAllTokens preserveAllTokens
     * @return 分割后数组
     */
    private fun splitWorker(str: String?, separatorChars: String?, max: Int,
                            preserveAllTokens: Boolean): Array<String>? {
        // Performance tuned for 2.0 (JDK1.4)
        // Direct code is quicker than StringTokenizer.
        // Also, StringTokenizer uses isSpace() not isWhitespace()

        if (str == null) {
            return null
        }
        val len = str.length
        if (len == 0) {
            return arrayOf()
        }
        val list = ArrayList<String>()
        var sizePlus1 = 1
        var i = 0
        var start = 0
        var match = false
        var lastMatch = false
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str[i])) {
                    if (match || preserveAllTokens) {
                        lastMatch = true
                        if (sizePlus1++ == max) {
                            i = len
                            lastMatch = false
                        }
                        list.add(str.substring(start, i))
                        match = false
                    }
                    start = ++i
                    continue
                }
                lastMatch = false
                match = true
                i++
            }
        } else if (separatorChars.length == 1) {
            // Optimise 1 character case
            val sep = separatorChars[0]
            while (i < len) {
                if (str[i] == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true
                        if (sizePlus1++ == max) {
                            i = len
                            lastMatch = false
                        }
                        list.add(str.substring(start, i))
                        match = false
                    }
                    start = ++i
                    continue
                }
                lastMatch = false
                match = true
                i++
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str[i]) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true
                        if (sizePlus1++ == max) {
                            i = len
                            lastMatch = false
                        }
                        list.add(str.substring(start, i))
                        match = false
                    }
                    start = ++i
                    continue
                }
                lastMatch = false
                match = true
                i++
            }
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i))
        }
        return list.toTypedArray()
    }

    /**
     * 压缩字符
     *
     * @param str 待压缩字符
     * @return 压缩后字符
     */
    @JvmStatic
    fun compress(str: String?): String? {
        if (str == null || str.isEmpty()) {
            return str
        }
        try {
            val out = ByteArrayOutputStream()
            val gzip = DeflaterOutputStream(out)
            gzip.write(str.toByteArray())
            gzip.close()
            return String(out.toByteArray(), Charset.forName("ISO-8859-1"))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    @JvmStatic
    fun decompress(str: String?): String? {
        if (str == null || str.isEmpty()) {
            return str
        }
        try {
            val byteArrayInputStream = ByteArrayInputStream(
                    str.toByteArray(charset("ISO-8859-1")))
            val zipInputStream = InflaterInputStream(byteArrayInputStream)
            return copyToString(zipInputStream, Charset.forName("ISO-8859-1"))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    @Throws(IOException::class)
    @JvmStatic
    fun copyToString(`in`: InputStream, charset: Charset): String {
        val reader = InputStreamReader(`in`, charset)
        return reader.readText()
    }

    /**
     * 格式化
     *
     * @param jsonStr jsonStr
     * @return String
     */
    @JvmStatic
    fun formatJson(jsonStr: String?): String {
        if (null == jsonStr || "" == jsonStr) {
            return ""
        }
        val sb = StringBuilder()
        var last: Char
        var current = '\u0000'
        var indent = 0
        var isInQuotationMarks = false
        for (i in 0 until jsonStr.length) {
            last = current
            current = jsonStr[i]
            when (current) {
                '"' -> {
                    if (last != '\\') {
                        isInQuotationMarks = !isInQuotationMarks
                    }
                    sb.append(current)
                }
                '{', '[' -> {
                    sb.append(current)
                    if (!isInQuotationMarks) {
                        sb.append('\n')
                        indent++
                        addIndentBlank(sb, indent)
                    }
                }
                '}', ']' -> {
                    if (!isInQuotationMarks) {
                        sb.append('\n')
                        indent--
                        addIndentBlank(sb, indent)
                    }
                    sb.append(current)
                }
                ',' -> {
                    sb.append(current)
                    if (last != '\\' && !isInQuotationMarks) {
                        sb.append('\n')
                        addIndentBlank(sb, indent)
                    }
                }
                else -> sb.append(current)
            }
        }

        return sb.toString()
    }

    /**
     * 添加space
     *
     * @param sb sb
     * @param indent indent
     */
    private fun addIndentBlank(sb: StringBuilder, indent: Int) {
        for (i in 0 until indent) {
            sb.append("  ")
        }
    }
}