package top.bettercode.summer.web.support.code

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil

/**
 *
 * @author Peter Wu
 */
class DicCodesTest {

    @Test
    fun name() {
        val dicCodes = DicCodes("test", "test", mapOf("a" to "b"))
        val json = StringUtil.json(dicCodes, true)
        val readJson = StringUtil.readJson(json, DicCodes::class.java)
        System.err.println(readJson)
    }
}