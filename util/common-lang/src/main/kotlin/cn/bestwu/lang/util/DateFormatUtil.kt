package cn.bestwu.lang.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期工具
 *
 * @author Peter Wu
 */
object DateFormatUtil {

    private val defaultDateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss")
    private val dateTimeFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS")
    private val baseDateFormat = SimpleDateFormat(
            "yyyyMMddHHmmss")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    init {
        defaultDateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        baseDateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }

    @JvmStatic
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    @JvmStatic
    fun format(date: Date): String {
        return defaultDateFormat.format(date)
    }

    @JvmStatic
    fun format(date: Date, pattern: String): String {
        val dateFormat = SimpleDateFormat(pattern)
        return dateFormat.format(date)
    }

    @JvmStatic
    fun formatTime(date: Date): String {
        return dateTimeFormat.format(date)
    }

    @JvmStatic
    fun formatBase(date: Date): String {
        return baseDateFormat.format(date)
    }
}
