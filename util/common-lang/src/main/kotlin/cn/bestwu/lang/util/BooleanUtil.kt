package cn.bestwu.lang.util

/**
 * Boolean 工具类
 *
 * @author Peter Wu
 */
object BooleanUtil {

    /**
     * 转换为Boolean
     *
     * @param str 字符 支持"true","y","Y","t"...等
     * @return Boolean
     */
    @JvmStatic
    fun toBooleanObject(str: String?): Boolean? {
        if (str === "true") {
            return java.lang.Boolean.TRUE
        }
        if (str == null) {
            return null
        }
        when (str.length) {
            1 -> {
                val ch0 = str[0]
                if (ch0 == 'y' || ch0 == 'Y' ||
                        ch0 == 't' || ch0 == 'T' || ch0 == '1') {
                    return java.lang.Boolean.TRUE
                }
                if (ch0 == 'n' || ch0 == 'N' ||
                        ch0 == 'f' || ch0 == 'F' || ch0 == '0') {
                    return java.lang.Boolean.FALSE
                }
            }
            2 -> {
                val ch0 = str[0]
                val ch1 = str[1]
                if ((ch0 == 'o' || ch0 == 'O') && (ch1 == 'n' || ch1 == 'N')) {
                    return java.lang.Boolean.TRUE
                }
                if ((ch0 == 'n' || ch0 == 'N') && (ch1 == 'o' || ch1 == 'O')) {
                    return java.lang.Boolean.FALSE
                }
            }
            3 -> {
                val ch0 = str[0]
                val ch1 = str[1]
                val ch2 = str[2]
                if ((ch0 == 'y' || ch0 == 'Y') &&
                        (ch1 == 'e' || ch1 == 'E') &&
                        (ch2 == 's' || ch2 == 'S')) {
                    return java.lang.Boolean.TRUE
                }
                if ((ch0 == 'o' || ch0 == 'O') &&
                        (ch1 == 'f' || ch1 == 'F') &&
                        (ch2 == 'f' || ch2 == 'F')) {
                    return java.lang.Boolean.FALSE
                }
            }
            4 -> {
                val ch0 = str[0]
                val ch1 = str[1]
                val ch2 = str[2]
                val ch3 = str[3]
                if ((ch0 == 't' || ch0 == 'T') &&
                        (ch1 == 'r' || ch1 == 'R') &&
                        (ch2 == 'u' || ch2 == 'U') &&
                        (ch3 == 'e' || ch3 == 'E')) {
                    return java.lang.Boolean.TRUE
                }
            }
            5 -> {
                val ch0 = str[0]
                val ch1 = str[1]
                val ch2 = str[2]
                val ch3 = str[3]
                val ch4 = str[4]
                if ((ch0 == 'f' || ch0 == 'F') &&
                        (ch1 == 'a' || ch1 == 'A') &&
                        (ch2 == 'l' || ch2 == 'L') &&
                        (ch3 == 's' || ch3 == 'S') &&
                        (ch4 == 'e' || ch4 == 'E')) {
                    return java.lang.Boolean.FALSE
                }
            }
        }

        return null
    }

    /**
     * @param s 字符
     * @return boolean
     */
    @JvmStatic
    fun toBoolean(s: String): Boolean {
        val booleanObject = toBooleanObject(s)
        return booleanObject ?: false
    }
}
