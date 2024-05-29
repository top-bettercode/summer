package top.bettercode.summer.test.web.support.division

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.web.support.division.DivisionData

/**
 *
 * @author Peter Wu
 */
class DivisionDataTest {

    @Test
    fun getProvinces() {
        System.err.println(StringUtil.json(DivisionData.provinces, true))
    }

    @Test
    fun getDivision() {
        System.err.println(StringUtil.json(DivisionData.getDivision("411523"), true))
        System.err.println("=============================================================")
        System.err.println(StringUtil.json(DivisionData.getDivision("130728"), true))
    }

    @Test
    fun getDivisionByName() {
        System.err.println(
            StringUtil.json(
                DivisionData.getDivisionByName(
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
                DivisionData.getDivisionByName(
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
        var division = DivisionData.getDivision("130000")
        System.err.println(division.province)
        System.err.println(division.city)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        System.err.println("==========================")
        division = DivisionData.getDivision("130700")
        System.err.println(division.province)
        System.err.println(division.city)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        System.err.println("==========================")
        division = DivisionData.getDivision("130728")
        System.err.println(division.province)
        System.err.println(division.city)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        division = DivisionData.getDivision("110000")
        System.err.println(division.province)
        System.err.println(division.city)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
        division = DivisionData.getDivision("110101")
        System.err.println(division.province)
        System.err.println(division.city)
        System.err.println(division.county)
        System.err.println(division.codes(true))
        System.err.println(division.codes(false))
        System.err.println(division.names)
    }
}