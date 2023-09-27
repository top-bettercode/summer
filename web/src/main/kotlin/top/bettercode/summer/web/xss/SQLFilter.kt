package top.bettercode.summer.web.xss

import java.util.*

/**
 * SQL过滤
 */
object SQLFilter {
    /**
     * SQL注入过滤
     *
     * @param str 待验证的字符串
     * @return str
     */
    fun sqlInject(str: String): String? {
        var s = str
        if (s.isBlank()) {
            return null
        }
        //去掉'|"|;|\字符
        s = s.replace("'", "")
        s = s.replace("\"", "")
        s = s.replace(";", "")
        s = s.replace("\\", "")

        //转换成小写
        s = s.lowercase(Locale.getDefault())

        //非法字符
        val keywords = arrayOf("master", "truncate", "insert", "select", "delete", "update", "declare",
                "alter", "drop")

        //判断是否包含非法字符
        for (keyword in keywords) {
            require(!s.contains(keyword)) { "包含非法字符" }
        }
        return s
    }
}
