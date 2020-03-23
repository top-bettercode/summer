package cn.bestwu.lang.util

import java.util.*

/**
 * 随机工具类
 *
 * @author Peter Wu
 */
object RandomUtil {

    private val RANDOM = Random()

    /**
     * 随机数字字符串
     *
     * @param num 字符串长度
     * @return 随机数字字符串
     */
    @JvmStatic
    fun nextIntString(num: Int): String {
        return nextString(num, "0123456789")
    }

    /**
     * 随机字符串
     *
     * @param count 字符串长度
     * @param chars 基本字符
     * @return 随机字符串
     */
    @JvmStatic
    fun nextString(count: Int, chars: String?): String {
        return if (chars == null) {
            next(count, 0, 0, false, false, null, RANDOM)
        } else nextString(count, chars.toCharArray())
    }

    /**
     * 随机字符串
     *
     * @param count 字符串长度
     * @param chars 基本字符
     * @return 随机字符串
     */
    @JvmStatic
    fun nextString(count: Int, chars: CharArray?): String {
        return if (chars == null) {
            next(count, 0, 0, false, false, null, RANDOM)
        } else next(count, 0, chars.size, false, false, chars, RANDOM)
    }

    /**
     * 随机字符串
     *
     * @param count 字符串长度
     * @param start 起始索引
     * @param end 结束索引
     * @param letters 是否包含字母
     * @param numbers 是否包含数字
     * @param chars 随机字符范围
     * @param random 随机
     * @return 随机字符串
     */
    @JvmStatic
    fun next(count: Int, start: Int, end: Int, letters: Boolean,
             numbers: Boolean, chars: CharArray?, random: Random): String {
        var c = count
        var s = start
        var e = end
        if (c == 0) {
            return ""
        } else if (c < 0) {
            throw IllegalArgumentException(
                    "Requested next string length " + c
                            + " is less than 0.")
        }
        if (s == 0 && e == 0) {
            e = 'z'.toInt() + 1
            s = ' '.toInt()
            if (!letters && !numbers) {
                s = 0
                e = Integer.MAX_VALUE
            }
        }

        val buffer = CharArray(c)
        val gap = e - s

        while (c-- != 0) {
            val ch: Char
            if (chars == null) {
                ch = (random.nextInt(gap) + s).toChar()
            } else {
                ch = chars[random.nextInt(gap) + s]
            }
            if (letters && Character.isLetter(ch)
                    || numbers && Character.isDigit(ch)
                    || !letters && !numbers) {
                if (ch.toInt() in 56320..57343) {
                    if (c == 0) {
                        c++
                    } else {
                        // low surrogate, insert high surrogate after putting it
                        // in
                        buffer[c] = ch
                        c--
                        buffer[c] = (55296 + random.nextInt(128)).toChar()
                    }
                } else if (ch.toInt() in 55296..56191) {
                    if (c == 0) {
                        c++
                    } else {
                        // high surrogate, insert low surrogate before putting
                        // it in
                        buffer[c] = (56320 + random.nextInt(128)).toChar()
                        c--
                        buffer[c] = ch
                    }
                } else if (ch.toInt() in 56192..56319) {
                    // private high surrogate, no effing clue, so skip it
                    c++
                } else {
                    buffer[c] = ch
                }
            } else {
                c++
            }
        }
        return String(buffer)
    }

    /**
     * 随机数字
     *
     * @param num 长度
     * @return 随机数字
     */
    @JvmStatic
    fun nextInt(num: Int): Int {
        return Integer.parseInt(nextIntString(num))
    }

    /**
     * 随机字符串
     *
     * @param num 长度
     * @return 随机字符串
     */
    @JvmStatic
    fun nextString(num: Int): String {
        return nextString(num, "abcdefghigklmnopqrstuvwxyz")
    }

    /**
     * 随机字符串
     *
     * @param num 长度
     * @return 随机字符串
     */
    @JvmStatic
    fun nextString2(num: Int): String {
        return nextString(num, "abcdefghigklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789")
    }
}
