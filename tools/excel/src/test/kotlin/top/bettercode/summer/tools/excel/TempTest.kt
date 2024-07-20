package top.bettercode.summer.tools.excel

import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
class TempTest {

    @Test
    fun name1() {
        System.err.println(Boolean::class.javaPrimitiveType == Boolean::class.java)
        System.err.println(Int::class.java.name)
        System.err.println("0." + String.format("%0" + 2 + "d", 0))
    }

    @Test
    fun name() {
        for (c in 0..199) {
            val x = getString(c)
            System.err.println(x)
        }
    }

    private fun getString(i: Int): String {
        var i1 = i
        val chars = StringBuilder()
        do {
            chars.append(('A'.code + i1 % 26).toChar())
        } while ((i1 / 26 - 1).also { i1 = it } >= 0)
        return chars.reverse().toString()
    }
}
