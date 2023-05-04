package com.baidu.ueditor

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object PathFormat {
    private const val TIME = "time"
    private const val FULL_YEAR = "yyyy"
    private const val YEAR = "yy"
    private const val MONTH = "mm"
    private const val DAY = "dd"
    private const val HOUR = "hh"
    private const val MINUTE = "ii"
    private const val SECOND = "ss"
    private const val RAND = "rand"
    private var currentDate: Date? = null
    fun parse(input: String): String {
        val pattern = Pattern.compile("\\{([^}]+)}", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(input)
        currentDate = Date()
        val sb = StringBuffer()
        while (matcher.find()) {
            matcher.appendReplacement(sb, getString(matcher.group(1)))
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    /**
     * 格式化路径, 把windows路径替换成标准路径
     *
     * @param input 待格式化的路径
     * @return 格式化后的路径
     */
    fun format(input: String): String {
        return input.replace("\\", "/")
    }

    fun parse(input: String, filename: String): String {
        var filename1 = filename
        val pattern = Pattern.compile("\\{([^}]+)}", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(input)
        var matchStr: String
        currentDate = Date()
        val sb = StringBuffer()
        while (matcher.find()) {
            matchStr = matcher.group(1)
            if (matchStr.contains("filename")) {
                filename1 = filename1.replace("$", "\\$").replace("[/:*?\"<>|]".toRegex(), "")
                matcher.appendReplacement(sb, filename1)
            } else {
                matcher.appendReplacement(sb, getString(matchStr))
            }
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    private fun getString(pattern: String): String {
        var pattern1 = pattern
        pattern1 = pattern1.lowercase(Locale.getDefault())

        // time 处理
        if (pattern1.contains(TIME)) {
            return timestamp
        } else if (pattern1.contains(FULL_YEAR)) {
            return fullYear
        } else if (pattern1.contains(YEAR)) {
            return year
        } else if (pattern1.contains(MONTH)) {
            return month
        } else if (pattern1.contains(DAY)) {
            return day
        } else if (pattern1.contains(HOUR)) {
            return hour
        } else if (pattern1.contains(MINUTE)) {
            return minute
        } else if (pattern1.contains(SECOND)) {
            return second
        } else if (pattern1.contains(RAND)) {
            return getRandom(pattern1)
        }
        return pattern1
    }

    private val timestamp: String
        get() = System.currentTimeMillis().toString() + ""
    private val fullYear: String
        get() = SimpleDateFormat("yyyy").format(currentDate)
    private val year: String
        get() = SimpleDateFormat("yy").format(currentDate)
    private val month: String
        get() = SimpleDateFormat("MM").format(currentDate)
    private val day: String
        get() = SimpleDateFormat("dd").format(currentDate)
    private val hour: String
        get() = SimpleDateFormat("HH").format(currentDate)
    private val minute: String
        get() = SimpleDateFormat("mm").format(currentDate)
    private val second: String
        get() = SimpleDateFormat("ss").format(currentDate)

    private fun getRandom(pattern: String): String {
        var pattern1 = pattern
        pattern1 = pattern1.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }
        val length: Int = pattern1.toInt()
        return (Math.random().toString() + "").replace(".", "").substring(0, length)
    }
}
