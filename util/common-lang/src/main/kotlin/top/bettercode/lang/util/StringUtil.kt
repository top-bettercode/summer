package top.bettercode.lang.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import org.springframework.util.StringUtils
import java.io.*
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.InflaterOutputStream


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

        val module: com.fasterxml.jackson.databind.module.SimpleModule =
            com.fasterxml.jackson.databind.module.SimpleModule()
        module.addSerializer(
            LocalDate::class.java,
            object : JsonSerializer<LocalDate>() {
                @Throws(IOException::class)
                override fun serialize(
                    value: LocalDate,
                    gen: JsonGenerator,
                    serializers: SerializerProvider
                ) {
                    gen.writeNumber(TimeUtil.of(value).toMillis())
                }
            })
        module.addSerializer(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>() {
            @Throws(IOException::class)
            override fun serialize(
                value: LocalDateTime, gen: JsonGenerator,
                serializers: SerializerProvider?
            ) {
                gen.writeNumber(TimeUtil.of(value).toMillis())
            }
        })

        module.addDeserializer(
            LocalDate::class.java,
            object : JsonDeserializer<LocalDate?>() {
                override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate? {
                    val valueAsString = p.valueAsString
                    return if (valueAsString.isNullOrBlank())
                        null
                    else
                        TimeUtil.toLocalDate(valueAsString.toLong())
                }
            })

        module.addDeserializer(
            LocalDateTime::class.java,
            object : JsonDeserializer<LocalDateTime?>() {
                override fun deserialize(
                    p: JsonParser,
                    ctxt: DeserializationContext
                ): LocalDateTime? {
                    val valueAsString = p.valueAsString
                    return if (valueAsString.isNullOrBlank())
                        null
                    else
                        return TimeUtil.toLocalDateTime(p.valueAsString.toLong())
                }
            })


        OBJECT_MAPPER.registerModule(module)
        INDENT_OUTPUT_OBJECT_MAPPER.registerModule(module)
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
     * @return 非null字符串
     */
    @JvmStatic
    fun null2empty(str: String?): String {
        return str ?: ""
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
                Character.isLowerCase(buf[i + 1])
            ) {
                buf.insert(i++, '_')
            }
            i++
        }
        return buf.toString().lowercase(Locale.ROOT)
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
        } else if (`object` is Throwable) {
            val stringWriter = StringWriter()
            PrintWriter(stringWriter).use { printWriter ->
                `object`.printStackTrace(printWriter)
                printWriter.flush()
            }
            return stringWriter.toString()
        }
        return try {
            json(`object`, format)
        } catch (e: Exception) {
            `object`.toString()
        }
    }


    @JvmOverloads
    @JvmStatic
    fun prettyJson(json: String?, indentWidth: Int = 2): String? {
        if (json == null) {
            return null
        }
        val chars = json.toCharArray()
        val newline = System.lineSeparator()
        val ret = java.lang.StringBuilder()
        var beginQuotes = false
        var i = 0
        var indent = 0
        loop@ while (i < chars.size) {
            val c = chars[i]
            if (c == '\"') {
                ret.append(c)
                beginQuotes = !beginQuotes
                i++
                continue
            }
            if (!beginQuotes) {
                when (c) {
                    '{', '[' -> {
                        ret.append(c).append(newline)
                            .append(String.format("%" + indentWidth.let { indent += it; indent } + "s",
                                ""))
                        i++
                        continue@loop
                    }

                    '}', ']' -> {
                        ret.append(newline)
                            .append(if (indentWidth.let { indent -= it; indent } > 0) String.format(
                                "%" + indent + "s",
                                ""
                            ) else "")
                            .append(c)
                        i++
                        continue@loop
                    }

                    ':' -> {
                        ret.append(c).append(" ")
                        i++
                        continue@loop
                    }

                    ',' -> {
                        ret.append(c).append(newline)
                            .append(if (indent > 0) String.format("%" + indent + "s", "") else "")
                        i++
                        continue@loop
                    }

                    else -> if (Character.isWhitespace(c)) {
                        i++
                        continue@loop
                    }
                }
            }
            ret.append(c).append(if (c == '\\') "" + chars[++i] else "")
            i++
        }
        return ret.toString()
    }

    @JvmOverloads
    @JvmStatic
    fun json(`object`: Any?, format: Boolean = false): String {
        return if (format) {
            INDENT_OUTPUT_OBJECT_MAPPER.writeValueAsString(`object`)
        } else {
            OBJECT_MAPPER.writeValueAsString(`object`)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun jsonBytes(`object`: Any?, format: Boolean = false): ByteArray {
        return if (format) {
            INDENT_OUTPUT_OBJECT_MAPPER.writeValueAsBytes(`object`)
        } else {
            OBJECT_MAPPER.writeValueAsBytes(`object`)
        }
    }

    @JvmStatic
    fun <T> readJson(`object`: ByteArray, valueType: Class<T>): T {
        return OBJECT_MAPPER.readValue(`object`, valueType)
    }

    @JvmStatic
    fun <T> readJson(`object`: ByteArray, valueType: JavaType): T {
        return OBJECT_MAPPER.readValue(`object`, valueType)
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
        return if (length in 4 until l) {
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
    private fun splitWorker(
        str: String?, separatorChars: String?, max: Int,
        preserveAllTokens: Boolean
    ): Array<String>? {
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
     * 解压字符串,默认utf-8
     */
    @JvmStatic
    fun inflater(data: ByteArray): ByteArray {
        val os = ByteArrayOutputStream()
        InflaterOutputStream(os).use { output -> output.write(data) }
        return os.toByteArray()
    }

    /**
     * 压缩字符串,默认梳utf-8
     * @param data 待压缩
     * @return 压缩后
     */
    @JvmStatic
    fun deflater(data: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        DeflaterOutputStream(out).use { output ->
            output.write(data)
        }
        return out.toByteArray()
    }

    /**
     * 压缩字符
     *
     * @param data 待压缩
     * @return 压缩后
     */
    @JvmStatic
    fun gzip(data: ByteArray): ByteArray {
        if (data.isEmpty()) {
            return data
        }
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use { output ->
            output.write(data)
        }
        return out.toByteArray()
    }

    @JvmStatic
    fun ungzip(data: ByteArray): ByteArray {
        if (data.isEmpty()) {
            return data
        }
        return GZIPInputStream(ByteArrayInputStream(data)).readBytes()
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
        for (element in jsonStr) {
            last = current
            current = element
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

    @JvmStatic
    fun trimMoneyTrailing(value: String): String {
        return if (value.contains(".")) StringUtils.trimTrailingCharacter(
            StringUtils.trimTrailingCharacter(
                value, '0'
            ), '.'
        ) else value
    }

    internal val versionTails = arrayOf("SNAPSHOTS", "ALPHA", "BETA", "M", "RC", "RELEASE")
    internal val versionTailRegex = "^([A-Za-z]+?)(\\d*)$".toRegex()

    /**
     * 比较版本信息

     * @param version1 版本1
     * *
     * @param version2 版本2
     * *
     * @return int
     */
    @JvmStatic
    fun compareVersion(version1: String, version2: String): Int {
        if (version1 == version2) {
            return 0
        }
        val separator = "[.-]"
        val version1s = version1.split(separator.toRegex()).toMutableList()
        val version2s = version2.split(separator.toRegex()).toMutableList()

        if (version1s.size < version2s.size) {
            version1s.addAll(List(version2s.size - version1s.size) { "" })
        } else {
            version2s.addAll(List(version1s.size - version2s.size) { "" })
        }
        val length = version1s.size

        for (i in 0 until length) {
            val toIntOrNull2 = version2s[i].toIntOrNull()
            val toIntOrNull1 = version1s[i].toIntOrNull()
            if (toIntOrNull1 == null && toIntOrNull2 != null)
                return -1
            else if (toIntOrNull1 != null && toIntOrNull2 == null)
                return 1
            var v2 = toIntOrNull2
                ?: versionTails.indexOf(
                    version2s[i].replace(versionTailRegex, "$1").uppercase(Locale.getDefault())
                )
            var v1 = toIntOrNull1
                ?: versionTails.indexOf(
                    version1s[i].replace(versionTailRegex, "$1").uppercase(Locale.getDefault())
                )
            if (v1 != -1 && v1 == v2 && toIntOrNull1 == null && toIntOrNull2 == null) {
                v2 = version2s[i].replace(versionTailRegex, "$2").toIntOrNull() ?: 0
                v1 = version1s[i].replace(versionTailRegex, "$2").toIntOrNull() ?: 0
            }
            if (v1 == -1 || v2 == -1) {
                val result = version1s[i].compareTo(version2s[i])
                if (result != 0) {
                    return result
                }
            }
            if (v2 > v1) {
                return -1
            } else if (v2 < v1) {
                return 1
            }
            // 相等 比较下一组值
        }
        return 0
    }

}