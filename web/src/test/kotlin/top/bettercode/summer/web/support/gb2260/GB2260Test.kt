package top.bettercode.summer.web.support.gb2260

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil

/**
 *
 * @author Peter Wu
 */
class GB2260Test {

    @Test
    fun getProvinces() {
        System.err.println(StringUtil.json(GB2260.provinces, true))
    }

    @Test
    fun getDivision() {
        System.err.println(StringUtil.json(GB2260.getDivision("130700"), true))
        System.err.println("=============================================================")
        System.err.println(StringUtil.json(GB2260.getDivision("130728"), true))
    }

    @Test
    fun getDivisionByName() {
        System.err.println(
            StringUtil.json(
                GB2260.getDivisionByName(
                    listOf(
                        "河北省",
                        "张家口市",
                        "怀安县"
                    )
                ), true
            )
        )
        System.err.println("=============================================================")
        System.err.println(
            StringUtil.json(
                GB2260.getDivisionByName(
                    listOf(
                        "河北省",
                        "张家口市"
                    )
                ), true
            )
        )
    }
}