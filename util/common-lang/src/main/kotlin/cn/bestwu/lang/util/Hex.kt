package cn.bestwu.lang.util

/**
 * @author Peter Wu
 */
object Hex {

    private val HEX = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    @JvmStatic
    fun encode(bytes: ByteArray): CharArray {
        val nBytes = bytes.size
        val result = CharArray(2 * nBytes)

        var j = 0
        for (aByte in bytes) {
            // Char for top 4 bits
            result[j++] = HEX[(0xF0 and aByte.toInt()).ushr(4)]
            // Bottom 4
            result[j++] = HEX[0x0F and aByte.toInt()]
        }

        return result
    }

    @JvmStatic
    fun decode(s: CharSequence): ByteArray {
        val nChars = s.length

        if (nChars % 2 != 0) {
            throw IllegalArgumentException(
                    "Hex-encoded string must have an even number of characters")
        }

        val result = ByteArray(nChars / 2)

        var i = 0
        while (i < nChars) {
            val msb = Character.digit(s[i], 16)
            val lsb = Character.digit(s[i + 1], 16)

            if (msb < 0 || lsb < 0) {
                throw IllegalArgumentException("Non-hex character in input: $s")
            }
            result[i / 2] = (msb shl 4 or lsb).toByte()
            i += 2
        }
        return result
    }

}
