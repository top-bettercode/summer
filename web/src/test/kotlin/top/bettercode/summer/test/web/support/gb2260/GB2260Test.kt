package top.bettercode.summer.test.web.support.gb2260

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.web.support.gb2260.GB2260

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
        System.err.println(StringUtil.json(GB2260.getDivision("411523"), true))
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

    @Test
    fun info() {
        var division = GB2260.getDivision("130000")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        System.err.println("==========================")
        division = GB2260.getDivision("130700")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        System.err.println("==========================")
        division = GB2260.getDivision("130728")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        division = GB2260.getDivision("110000")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        division = GB2260.getDivision("110101")
        System.err.println(division.province)
        System.err.println(division.prefecture)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
    }
}