package top.bettercode.summer.tools.lang.util

import kotlin.math.pow

object SnUtil {

    fun toNum(sn: String): Long {
        var result = 0L
        val chars = sn.toCharArray()
        for (i in chars.indices) {
            val pow = chars.size - i - 1
            val index = digits.indexOf(chars[i])
            result += (index * radix.toDouble().pow(pow)).toLong()
        }
        return result
    }

    fun toSn(n: Long): String {
        var m = n
        var s = ""
        if (m == 0L) {
            s = "0"
        }

        while (m != 0L) {
            val i = m % radix
            val c = digits[i.toInt()]
            s = c.toString() + s
            m /= radix
        }
        return s
    }

    private val digits = charArrayOf(
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        'a',
        'b',
        'c',
        'd',
        'e',
        'f',
        'g',
        'h',
        'i',
        'j',
        'k',
        'l',
        'm',
        'n',
        'o',
        'p',
        'q',
        'r',
        's',
        't',
        'u',
        'v',
        'w',
        'x',
        'y',
        'z',
        'A',
        'B',
        'C',
        'D',
        'E',
        'F',
        'G',
        'H',
        'I',
        'J',
        'K',
        'L',
        'M',
        'N',
        'O',
        'P',
        'Q',
        'R',
        'S',
        'T',
        'U',
        'V',
        'W',
        'X',
        'Y',
        'Z'
    )


    private val radix = digits.size


}