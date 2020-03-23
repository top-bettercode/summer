package cn.bestwu.lang.util

/**
 * Escape工具
 *
 * @author Peter Wu
 */
object EscapeUtil {

    /**
     * escape
     *
     * @param src 源
     * @return 结果
     */
    @JvmStatic
    fun escape(src: String): String {
        var i = 0
        var j: Char
        val tmp = StringBuilder()
        tmp.ensureCapacity(src.length * 6)
        while (i < src.length) {
            j = src[i]
            if (Character.isDigit(j) || Character.isLowerCase(j)
                    || Character.isUpperCase(j)) {
                tmp.append(j)
            } else if (j.toInt() < 256) {
                tmp.append("%")
                if (j.toInt() < 16) {
                    tmp.append("0")
                }
                tmp.append(Integer.toString(j.toInt(), 16))
            } else {
                tmp.append("%u")
                tmp.append(Integer.toString(j.toInt(), 16).toUpperCase())
            }
            i++
        }
        return tmp.toString()
    }

    /**
     * unescape
     *
     * @param src 源
     * @return 结果
     */
    @JvmStatic
    fun unescape(src: String): String {
        val tmp = StringBuilder()
        tmp.ensureCapacity(src.length)
        var lastPos = 0
        var pos: Int
        var ch: Char
        while (lastPos < src.length) {
            pos = src.indexOf("%", lastPos)
            if (pos == lastPos) {
                if (src[pos + 1] == 'u') {
                    ch = Integer.parseInt(src
                            .substring(pos + 2, pos + 6), 16).toChar()
                    tmp.append(ch)
                    lastPos = pos + 6
                } else {
                    ch = Integer.parseInt(src
                            .substring(pos + 1, pos + 3), 16).toChar()
                    tmp.append(ch)
                    lastPos = pos + 3
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos))
                    lastPos = src.length
                } else {
                    tmp.append(src.substring(lastPos, pos))
                    lastPos = pos
                }
            }
        }
        return tmp.toString()
    }

}