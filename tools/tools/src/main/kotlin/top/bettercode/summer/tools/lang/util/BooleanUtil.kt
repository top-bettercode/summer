package top.bettercode.summer.tools.lang.util

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Boolean 工具类
 *
 * @author Peter Wu
 */
object BooleanUtil {

    /**
     * 转换为Boolean
     * Goes back to ODBC driver compatibility, and VB/Automation Languages/COM, where in Windows "-1" can mean true as well.
     *
     * @param any 字符 支持"true","y","Y","t"...等
     * @return Boolean
     */
    @JvmStatic
    fun toBoolean(any: Any?): Boolean? {
        return when (any) {
            null -> null
            is Boolean -> any
            is Int -> any == -1L || any > 0
            is String -> toBoolean(any)
            is Char -> toBoolean(any.toString())
            is Long -> any == -1L || any > 0
            is Short -> any == -1L || any > 0
            is BigInteger -> any > BigInteger.valueOf(0) || any.compareTo(BigInteger.valueOf(-1)) == 0
            is Float -> any > 0 || any == -1.0f
            is Double -> any > 0 || any == -1.0
            is Byte -> any > 0 || any == ((-1).toByte())
            is BigDecimal -> toBoolean(any)
            else -> null
        }
    }

    @JvmStatic
    fun toBoolean(any: BigDecimal): Boolean {
        return any > BigDecimal.ZERO || any.compareTo(BigDecimal.ONE.negate()) == 0
    }

    @JvmStatic
    fun toBoolean(any: String): Boolean? {
        if (any.equals("Y", ignoreCase = true)
                || any.equals("T", ignoreCase = true)
                || any.equals("yes", ignoreCase = true)
                || any.equals("true", ignoreCase = true)
                || any.equals("是", ignoreCase = true)) {
            return true
        } else if (any.equals("N", ignoreCase = true)
                || any.equals("F", ignoreCase = true)
                || any.equals("no", ignoreCase = true)
                || any.equals("false", ignoreCase = true)
                || any.equals("否", ignoreCase = true)) {
            return false
        } else if (any.contains("e")
                || any.contains("E")
                || any.matches("-?\\d*\\.\\d*".toRegex())
                || any.matches("-?\\d+".toRegex())) {
            // floating point or integer
            return toBoolean(BigDecimal(any))
        }
        return null
    }

}
