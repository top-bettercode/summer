package top.bettercode.lang.util

import kotlin.experimental.and

/**
 * 字符工具类
 *
 * @author Peter Wu
 */
object CharUtil {

    /**
     * 将字符串转移为ASCII码
     *
     * @param str 字符串
     * @return ASCII码
     */
    @JvmStatic
    fun getCnASCII(str: String): String {
        val sb = StringBuilder()
        val strByte = str.toByteArray()
        for (aStrByte in strByte) {
            sb.append(Integer.toHexString((aStrByte and 0xff.toByte()).toInt()))
        }
        return sb.toString()
    }


    /**
     * 是否为汉字
     *
     * @param c 字符
     * @return 是否为汉字
     */

    @JvmStatic
    fun isCNChar(c: Char): Boolean {
        return c.toString().matches("[\\u4E00-\\u9FA5]+".toRegex())
    }

    /**
     * 根据Unicode编码完美的判断中文汉字和符号
     * @param c 字符
     * @return 是否为汉字和符号
     */
    @JvmStatic
    fun isChinese(c: Char): Boolean {
        val ub = Character.UnicodeBlock.of(c)
        return (ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub === Character.UnicodeBlock.GENERAL_PUNCTUATION)
    }

    /**
     * 是否为大写字母
     *
     * @param capital capital
     * @return 是否为大写字母
     */
    @JvmStatic
    fun isBigCapital(capital: String): Boolean {
        return capital.matches("[\\u0041-\\u005A]+".toRegex())
    }

    /**
     * 是否为汉字字符串(只要包含了一个汉字)
     *
     * @param str 字符
     * @return 是否为汉字字符串
     */
    @JvmStatic
    fun hasCNStr(str: String): Boolean {
        for (c in str.toCharArray()) {
            if (isCNChar(c)) {// 如果有一个为汉字
                return true
            }
        }
        // 如果没有一个汉字，全英文字符串
        return false
    }
}
